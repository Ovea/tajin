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
package com.ovea.tajin.resources

import com.ovea.tajin.TajinConfig
import com.ovea.tajin.io.FileWatcher
import com.yahoo.platform.yui.compressor.CssCompressor
import com.yahoo.platform.yui.compressor.JavaScriptCompressor
import org.apache.tools.ant.DirectoryScanner
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException

import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_DELETE
import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_MODIFY

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class Minifier implements ResourceBuilder {

    final TajinConfig config
    Collection<File> watchables = []

    Minifier(TajinConfig config) {
        this.config = config
        Class<?> c = getClass()
        config.onConfig {
            config.log("[%s] Tajin configuration changed", c.simpleName)
            Collection<String> w = new TreeSet((config.minify?.includes ?: []).findAll { String path -> !path.contains('*') })
            DirectoryScanner scanner = new DirectoryScanner(
                basedir: config.webapp,
                includes: (config.minify?.includes ?: []).findAll { String path -> path.contains('*') },
                excludes: config.minify?.excludes
            )
            scanner.scan()
            w.addAll(scanner.includedFiles.collect {
                // skip XXX-min.ext and XXX.min.ext
                int pos = it.lastIndexOf('.')
                if (pos != -1) {
                    String part = it.substring(0, pos)
                    if (!part.endsWith('.min') && !part.endsWith('-min')) {
                        return it
                    }
                }
                return ''
            })
            // get non nulls and tranform to files
            watchables = w.findAll { it }.collect { new File(config.webapp, it) }
        }
    }

    @Override
    Work build() {
        return complete(Work.incomplete(this, watchables))
    }

    @Override
    Work complete(Work work) {
        Collection<File> incompletes = work.data
        def missing = []
        incompletes.each { if (!minify(it)) missing << it }
        return missing ? Work.incomplete(this, missing) : Work.COMPLETED
    }

    @Override
    boolean modified(FileWatcher.Event e) {
        if (e.kind in [ENTRY_DELETE, ENTRY_MODIFY] && e.target in watchables) {
            if (e.kind == ENTRY_DELETE) {
                File min = minFile(e.target)
                if (min) {
                    min.delete()
                    config.log('[%s] Removed: %s', getClass().simpleName, min.name)
                }
            } else {
                minify(e.target)
            }
        }
        // do not need a client-json regeneration
        return false
    }

    boolean minify(File src) {
        if (src.exists()) {
            File min = minFile(src)
            if (!min) {
                return false
            }
            if (src.name.endsWith('.css')) {
                def out = new StringWriter()
                src.withInputStream { InputStream is ->
                    CssCompressor compressor = new CssCompressor(new InputStreamReader(is, 'UTF-8'))
                    compressor.compress(out, -1)
                }
                config.log('[%s] + %s', getClass().simpleName, min.name)
                min.bytes = out.toString().getBytes('UTF-8')
                return true
            } else if (src.name.endsWith('.js')) {
                def error = [:]
                def out = new StringWriter()
                src.withInputStream { InputStream is ->
                    JavaScriptCompressor compressor = new JavaScriptCompressor(new InputStreamReader(is, 'UTF-8'), new ErrorReporter() {
                        @Override
                        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                            if (line < 0) {
                                config.trace('[%s] WARN %s : %s', getClass().simpleName, src, message)
                            } else {
                                config.trace('[%s] WARN %s (%s,%s) : %s', getClass().simpleName, src, line, lineOffset, message)
                            }
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
                            if (line < 0) {
                                config.log('[%s] ERROR %s : %s', getClass().simpleName, src, message)
                            } else {
                                config.log('[%s] ERROR %s (%s,%s) : %s', getClass().simpleName, src, line, lineOffset, message)
                            }
                        }

                        @Override
                        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
                            return new EvaluatorException(message, src.absolutePath, line, lineSource, lineOffset);
                        }
                    })
                    compressor.compress(out, -1, false, true, false, false)
                }
                config.log('[%s] + %s', getClass().simpleName, min.name)
                if (error) {
                    throw new EvaluatorException(error.message as String, src.absolutePath, error.line as int, error.lineSource as String, error.lineOffset as int);
                } else {
                    min.bytes = out.toString().getBytes('UTF-8')
                }
                return true
            }
        } else {
            config.log('[%s] File not found: %s', getClass().simpleName, src)
        }
        return false
    }

    private static File minFile(File src) {
        int pos = src.name.lastIndexOf('.')
        return pos == -1 ? null : new File(src.parentFile, "${src.name.substring(0, pos)}.min${src.name.substring(pos)}")
    }
}
