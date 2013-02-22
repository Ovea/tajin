/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class Merger implements ResourceBuilder {

    final TajinConfig config

    Merger(TajinConfig config) {
        this.config = config
    }

    @Override
    Work build() {
        return complete(Work.incomplete(this, (config.merge ?: [:]).keySet()))
    }

    @Override
    Work complete(Work work) {
        Collection<File> incompletes = work.data
        def missing = []
        incompletes.each { if (!merge(it)) missing << it }
        return missing ? Work.incomplete(this, missing) : Work.COMPLETED
    }

    @Override
    Collection<File> getWatchables() { (config.merge ?: [:]).values().collect { String m, Collection<String> files -> files }.flatten().unique().collect { new File(config.webapp, it) } }

    @Override
    boolean modified(FileWatcher.Event e) {
        (config.merge ?: [:]).each { String m, Collection<String> files ->
            if (files.find { e.target == new File(config.webapp, it) }) {

            }
        }

        merge(e.target)
        // do not need a client-json regeneration
        return false
    }

    boolean merge(File dest) {
        if (src.exists()) {
            config.log('[%s] Processing: %s', getClass().simpleName, src)
            int pos = src.name.lastIndexOf('.')
            if (pos == -1) {
                // invalid extension
                return false
            }
            File min = new File(src.parentFile, "${src.name.substring(0, pos)}.min${src.name.substring(pos)}")
            if (src.name.endsWith('.css')) {
                min.withWriter { Writer w ->
                    src.withReader { Reader r ->
                        CssCompressor compressor = new CssCompressor(r)
                        compressor.compress(w, -1)
                    }
                }
                return true
            } else if (src.name.endsWith('.js')) {
                boolean error = [:]
                min.withWriter { Writer w ->
                    src.withReader { Reader r ->
                        JavaScriptCompressor compressor = new JavaScriptCompressor(r, new ErrorReporter() {
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
                        compressor.compress(w, -1, false, true, false, false)
                    }
                }
                if (error) {
                    throw new EvaluatorException(error.message as String, src.absolutePath, error.line as int, error.lineSource as String, error.lineOffset as int);
                }
                return true
            }
        } else {
            config.log('[%s] File not found: %s', getClass().simpleName, src)
        }
        return false
    }
}
