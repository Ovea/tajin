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
package com.ovea.tajin.markup.web;

import com.ovea.tajin.markup.*;
import com.ovea.tajin.markup.mvel.MVEL2TemplateCompiler;
import com.ovea.tajin.markup.util.ExceptionUtil;
import com.ovea.tajin.markup.util.MimeTypes;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MarkupServlet extends HttpServlet {

    private static Logger LOGGER = Logger.getLogger(MarkupServlet.class.getName());
    private static final long serialVersionUID = -9052484265570140129L;

    private MarkupOptions markupOptions;
    private LocaleProvider localeProvider;
    private ContextProvider contextProvider;
    private MarkupDataBuilder markupDataBuilder;
    private CachingFixture cachingFixture;

    public void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    public void setContextProvider(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        markupOptions = MarkupOptions.from(config);
        TemplateCompiler compiler = !markupOptions.debug && markupOptions.compilerCache ? new CachingTemplateCompiler(new MVEL2TemplateCompiler(markupOptions.defaultCharset)) : new MVEL2TemplateCompiler(markupOptions.defaultCharset);
        TemplateResolver resolver = !markupOptions.debug && markupOptions.resolverCache ? new CachingTemplateResolver(new FileSystemTemplateResolver(markupOptions.webappDir)) : new FileSystemTemplateResolver(markupOptions.webappDir);
        localeProvider = nullSafe(localeProvider != null ? localeProvider : new LocaleProvider() {
            @Override
            public Locale get(HttpServletRequest request) {
                return null;
            }
        });
        contextProvider = nullSafe(contextProvider != null ? contextProvider : new ContextProvider() {
            @Override
            public Map<String, Object> build(HttpServletRequest request, HttpServletResponse response) {
                return null;
            }
        });
        cachingFixture = new CachingFixture(markupOptions);
        markupDataBuilder = new DynamicMarkupDataBuilder(compiler, contextProvider, localeProvider, markupOptions, resolver);
        if (!markupOptions.debug && !markupOptions.dynamic) {
            markupDataBuilder = new StaticMarkupDataBuilder(this.markupDataBuilder, localeProvider);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith(request.getContextPath()))
            path = path.substring(request.getContextPath().length());
        if (path != null) {
            int pos = path.lastIndexOf('.');
            if (pos != -1) {
                path = path.toLowerCase();
                int end = path.indexOf(";", pos);
                if (end != -1)
                    path = path.substring(0, end);
                final String extension = path.substring(pos + 1);
                if (markupOptions.markups.contains(extension)) {
                    final MarkupData markupData = markupDataBuilder.build(request, response, path);
                    if (markupData.hasError()) {
                        RuntimeException e = markupData.error;
                        int status = e instanceof TemplateResolverException ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        if (markupOptions.debug)
                            response.sendError(status, ExceptionUtil.getStackTrace(e));
                        else
                            response.sendError(status);
                        if (status == HttpServletResponse.SC_NOT_FOUND) {
                            LOGGER.log(Level.WARNING, "Error serving markup " + path + " : " + e.getMessage(), e);
                        } else {
                            LOGGER.log(Level.SEVERE, "Error serving markup " + path + " : " + e.getMessage(), e);
                        }
                        return;
                    }
                    if (cachingFixture.isModified(request, response, markupData)) {
                        response.setContentType(MimeTypes.getContentTypeForExtension(extension));
                        response.setCharacterEncoding(markupOptions.defaultCharset);
                        if (markupOptions.gzip && acceptsGZipEncoding(request)) {
                            response.setHeader("Content-Encoding", "gzip");
                            response.setContentLength(markupData.gzip.length);
                            response.getOutputStream().write(markupData.gzip);
                        } else {
                            response.setContentLength(markupData.data.length);
                            response.getOutputStream().write(markupData.data);
                        }
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                    return;
                }
            }
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private static LocaleProvider nullSafe(final LocaleProvider localeProvider) {
        return new LocaleProvider() {
            @Override
            public Locale get(HttpServletRequest request) {
                Locale locale = localeProvider.get(request);
                if (locale == null) {
                    locale = request.getLocale();
                }
                return locale != null ? locale : Locale.US;
            }
        };
    }

    private static ContextProvider nullSafe(final ContextProvider contextProvider) {
        return new ContextProvider() {
            @Override
            public Map<String, Object> build(HttpServletRequest request, HttpServletResponse response) {
                Map<String, Object> ctx = contextProvider.build(request, response);
                if (ctx == null) {
                    ctx = new HashMap<String, Object>();
                } else {
                    ctx = new HashMap<String, Object>(ctx);
                }
                if (!ctx.containsKey("req")) {
                    ctx.put("req", request);
                }
                if (!ctx.containsKey("res")) {
                    ctx.put("res", response);
                }
                if (!ctx.containsKey("sess")) {
                    Map<String, Object> attributes = new LinkedHashMap<String, Object>();
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        Enumeration<String> e = session.getAttributeNames();
                        while (e.hasMoreElements()) {
                            String key = e.nextElement();
                            attributes.put(key, session.getAttribute(key));
                        }
                    }
                    ctx.put("sess", attributes);
                }
                if (!ctx.containsKey("attr")) {
                    Map<String, Object> attributes = new LinkedHashMap<String, Object>();
                    Enumeration<String> e = request.getAttributeNames();
                    while (e.hasMoreElements()) {
                        String key = e.nextElement();
                        attributes.put(key, request.getAttribute(key));
                    }
                    ctx.put("attr", attributes);
                }
                if (!ctx.containsKey("param")) {
                    Map<String, Object> attributes = new LinkedHashMap<String, Object>();
                    Enumeration<String> e = request.getParameterNames();
                    while (e.hasMoreElements()) {
                        String key = e.nextElement();
                        attributes.put(key, request.getParameter(key));
                    }
                    ctx.put("param", attributes);
                }
                return ctx;
            }
        };
    }

    private static boolean acceptsGZipEncoding(HttpServletRequest request) {
        String acceptEncoding = request.getHeader("Accept-Encoding");
        return acceptEncoding != null && acceptEncoding.contains("gzip");
    }
}

