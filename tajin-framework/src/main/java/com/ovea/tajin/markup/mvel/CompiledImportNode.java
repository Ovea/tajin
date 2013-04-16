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
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.mvel2.templates.res.Node;
import org.mvel2.templates.util.TemplateOutputStream;
import org.mvel2.templates.util.io.StringAppenderStream;
import org.mvel2.util.StringAppender;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import static org.mvel2.templates.util.TemplateTools.captureToEOS;
import static org.mvel2.util.ParseTools.subset;

public class CompiledImportNode extends Node {

    private static final ThreadLocal<TemplateMergingCallback> currentCallBack = new ThreadLocal<TemplateMergingCallback>();

    private Serializable cIncludeExpression;
    private Serializable cPreExpression;
    private long fileDateStamp;
    private CompiledTemplate cFileCache;

    @Override
    public void setContents(char[] contents) {
        super.setContents(contents);
        int mark;
        cIncludeExpression = MVEL.compileExpression(subset(contents, 0, mark = captureToEOS(contents, 0)));
        if (mark != contents.length)
            cPreExpression = MVEL.compileExpression(subset(contents, ++mark, contents.length - mark));
    }

    @Override
    public Object eval(final TemplateRuntime runtime, final TemplateOutputStream appender, final Object ctx, final VariableResolverFactory factory) {
        final String file = MVEL.executeExpression(cIncludeExpression, ctx, factory, String.class);
        TemplateMergingCallback cb = CompiledImportNode.currentCallBack.get();
        Execution<TemplateOutputStream> execution = new Execution<TemplateOutputStream>() {
            @Override
            public TemplateOutputStream proceed() {
                if (cPreExpression != null) {
                    MVEL.executeExpression(cPreExpression, ctx, factory);
                }
                if (next != null) {
                    return appender.append(String.valueOf(TemplateRuntime.eval(readFile(runtime, file, ctx, factory), ctx, factory)));
                } else {
                    return appender.append(String.valueOf(MVEL.eval(readFile(runtime, file, ctx, factory), ctx, factory)));
                }
            }
        };
        TemplateOutputStream templateOutputStream = cb == null ? execution.proceed() : cb.onImport(getFile(runtime, file), ctx, execution);
        if (next != null) {
            return next.eval(runtime, templateOutputStream, ctx, factory);
        } else {
            return templateOutputStream;
        }
    }

    @Override
    public boolean demarcate(Node terminatingNode, char[] template) {
        return false;
    }

    private String readFile(TemplateRuntime runtime, String fileName, Object ctx, VariableResolverFactory factory) {
        File file = getFile(runtime, fileName);
        try {
            runtime.getRelPath().push(file.getParentFile().getPath());
            if (fileDateStamp == 0 || fileDateStamp != file.lastModified()) {
                fileDateStamp = file.lastModified();
                if (file.getPath().startsWith("classpath:")) {
                    String t = Resource.file(file.getPath().replace('\\', '/')).getText();
                    cFileCache = TemplateCompiler.compileTemplate(t, MVEL2TemplateCompiler.customNodes());
                } else {
                    String t = Resource.file(file).getText();
                    cFileCache = TemplateCompiler.compileTemplate(t, MVEL2TemplateCompiler.customNodes());
                }
            }
            TemplateRuntime r = new TemplateRuntime(
                cFileCache.getTemplate(),
                null,
                cFileCache.getRoot(),
                file.getParentFile().getAbsolutePath());
            Object o = r.execute(
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
                ctx,
                factory);
            return o == null ? null : o.toString();
        } finally {
            runtime.getRelPath().pop();
        }
    }

    private File getFile(TemplateRuntime runtime, String fileName) {
        return new File(String.valueOf(runtime.getRelPath().peek()) + "/" + fileName);
    }

    static void setCurrentCallBack(TemplateMergingCallback currentCallBack) {
        CompiledImportNode.currentCallBack.set(currentCallBack);
    }

    static void clearCurrentCallBack() {
        CompiledImportNode.currentCallBack.remove();
    }

}