package com.ovea.tajin.framework.template;

import com.ovea.tajin.framework.io.Resource;
import groovy.text.GStringTemplateEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-29
 */
public class GroovyTemplateCompiler implements TemplateCompiler {
    private final String defaultCharset;

    public GroovyTemplateCompiler(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    @Override
    public Template compile(Resource resource) throws TemplateCompilerException {
        final groovy.text.Template tmpl;
        try {
            tmpl = new GStringTemplateEngine(Thread.currentThread().getContextClassLoader()).createTemplate(resource.getText(defaultCharset));
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return new Template(resource) {
            @Override
            public String merge(Object context) {
                try {
                    StringWriter sw = new StringWriter();
                    tmpl.make((Map) context).writeTo(sw);
                    return sw.toString();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };
    }
}
