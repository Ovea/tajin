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
package com.ovea.tajin.markup.mvel;

import com.ovea.tajin.io.Resource;
import com.ovea.tajin.markup.Template;
import com.ovea.tajin.markup.TemplateCompiler;
import com.ovea.tajin.markup.TemplateCompilerException;
import org.mvel2.ParserContext;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateRuntime;
import org.mvel2.templates.res.Node;
import org.mvel2.templates.util.TemplateOutputStream;
import org.mvel2.templates.util.io.StringAppenderStream;
import org.mvel2.util.StringAppender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MVEL2TemplateCompiler implements TemplateCompiler {

    private String charset;

    public MVEL2TemplateCompiler() {
        this("UTF-8");
    }

    public MVEL2TemplateCompiler(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public Template compile(Resource location) throws TemplateCompilerException {
        try {
            return load(location);
        } catch (Exception e) {
            throw new TemplateCompilerException("Unable to compile template " + location + " : " + e.getMessage(), e);
        }
    }

    private Template load(final Resource location) throws IOException {
        final CompiledTemplate compiledTemplate = new org.mvel2.templates.TemplateCompiler(location.getText(getCharset()), customNodes(), true, ParserContext.create()).compile();

        return new Template(location) {
            @Override
            public String merge(Object context, TemplateMergingCallback callback) {
                if (location.isUrl()) {
                    final String basedir;
                    if (location.isFile()) {
                        basedir = location.getAsFile().getParentFile().getPath();
                    } else {
                        String path = location.getAsUrl().getPath();
                        path = path.substring(path.indexOf("!/") + 1);
                        basedir = "classpath:" + new File(path).getParentFile().getPath().replace('\\', '/');
                    }
                    TemplateRuntime runtime = new TemplateRuntime(
                        compiledTemplate.getTemplate(),
                        null,
                        compiledTemplate.getRoot(),
                        basedir);
                    Object o = runtime.execute(
                        new StringAppenderStream(new StringAppender()) {
                            @Override
                            public TemplateOutputStream append(char[] c) {
                                return Arrays.equals(c, new char[]{'n', 'u', 'l', 'l'}) ? this : super.append(c);
                            }

                            @Override
                            public TemplateOutputStream append(CharSequence c) {
                                return "null".equals(c.toString()) ? this : super.append(c);
                            }
                        },
                        context,
                        new MapVariableResolverFactory());
                    return o == null ? null : o.toString();
                }
                Object o = TemplateRuntime.execute(compiledTemplate, context, new MapVariableResolverFactory());
                return o == null ? null : o.toString();
            }
        }

            ;
    }

    private CompiledTemplate buildCompiledTemplate(InputStream in) throws IOException {
        try {
            return org.mvel2.templates.TemplateCompiler.compileTemplate(in, customNodes());
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }

    static Map<String, Class<? extends Node>> customNodes() {
        Map<String, Class<? extends Node>> map = new HashMap<String, Class<? extends Node>>();
        map.put("import", CompiledImportNode.class);
        return map;
    }

}
