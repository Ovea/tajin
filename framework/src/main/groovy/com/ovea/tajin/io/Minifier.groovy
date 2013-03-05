/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.tajin.io

import com.yahoo.platform.yui.compressor.CssCompressor
import com.yahoo.platform.yui.compressor.JavaScriptCompressor
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class Minifier {

    static String minifyCSS(String src) {
        return minifyCSS(new StringReader(src))
    }

    static String minifyCSS(Reader r) {
        def out = new StringWriter()
        minifyCSS(r, out)
        return out.toString()
    }

    static void minifyCSS(Reader r, Writer w) {
        CssCompressor compressor = new CssCompressor(r)
        compressor.compress(w, -1)
    }

    static String minifyJS(String path, String src) {
        return minifyJS(path, new StringReader(src))
    }

    static String minifyJS(String path, Reader r) {
        def out = new StringWriter()
        minifyJS(path, r, out)
        return out.toString()
    }

    static void minifyJS(String path, Reader r, Writer w) {
        def error = [:]
        JavaScriptCompressor compressor = new JavaScriptCompressor(r, new ErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            }

            @Override
            public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
                if (!error) {
                    error << [
                        message: message,
                        line: line,
                        lineSource: lineSource,
                        lineOffset: lineOffset
                    ]
                }
            }

            @Override
            public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
                return new EvaluatorException(message, path, line, lineSource, lineOffset);
            }
        })
        compressor.compress(w, -1, false, true, false, false)
        if (error) {
            throw new EvaluatorException(error.message as String, path, error.line as int, error.lineSource as String, error.lineOffset as int)
        }
    }

    static File minify(File src) {
        if (src.exists()) {
            File min = getFilename(src)
            if (!min) {
                return null
            }
            if (src.name.endsWith('.css')) {
                src.withInputStream { min.bytes = minifyCSS(new InputStreamReader(it, 'UTF-8')).getBytes('UTF-8') }
                return min
            } else if (src.name.endsWith('.js')) {
                src.withInputStream { min.bytes = minifyJS(src.absolutePath, new InputStreamReader(it, 'UTF-8')).getBytes('UTF-8') }
                return min
            }
        }
        return null
    }

    static File getFilename(File src) {
        int pos = src.name.lastIndexOf('.')
        return pos == -1 ? null : new File(src.parentFile, "${src.name.substring(0, pos)}.min${src.name.substring(pos)}")
    }
}
