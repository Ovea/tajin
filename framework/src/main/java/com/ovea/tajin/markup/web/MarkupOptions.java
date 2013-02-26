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

import javax.servlet.ServletConfig;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class MarkupOptions {

    public static final String DEBUG = "debug";
    public static final String COMPILER_CACHE = "compilerCache";
    public static final String RESOLVER_CACHE = "resolverCache";
    public static final String DYNAMIC_MARKUPS = "dynamic";
    public static final String GZIP = "gzip";
    public static final String DEFAULT_CHARSET = "defaultCharset";
    public static final String CLIENT_CACHING = "clientCaching";
    public static final String MARKUPS = "markups";

    final ServletConfig config;
    final boolean debug;
    final long clientCaching;
    final boolean compilerCache;
    final boolean resolverCache;
    final boolean dynamic;
    final boolean gzip;
    final String defaultCharset;
    final File webappDir;
    final Collection<String> markups;

    private MarkupOptions(ServletConfig config) {
        this.config = config;
        this.debug = getBool(DEBUG, false);
        this.compilerCache = getBool(COMPILER_CACHE, true);
        this.resolverCache = getBool(RESOLVER_CACHE, true);
        this.dynamic = getBool(DYNAMIC_MARKUPS, true);
        this.gzip = getBool(GZIP, true);
        this.defaultCharset = get(DEFAULT_CHARSET, "UTF-8");
        this.clientCaching = getLong(CLIENT_CACHING, 60 * 60 * 24 * 30); // 1-month
        this.webappDir = new File(config.getServletContext().getRealPath(""));
        String[] exts = get(MARKUPS, "html").split(",|;|\\s|-|\\|");
        HashSet<String> set = new HashSet<String>();
        for (String ext : exts) {
            if (ext != null && ext.length() > 0) {
                set.add(ext);
            }
        }
        this.markups = Collections.unmodifiableSet(set);
    }

    private boolean getBool(String opt, boolean def) {
        return Boolean.parseBoolean(get(opt, String.valueOf(def)));
    }

    private long getLong(String opt, long def) {
        return Long.parseLong(get(opt, String.valueOf(def)));
    }

    private String get(String opt, String def) {
        String val = config.getInitParameter(opt);
        if (val == null) {
            val = System.getProperty("ovea.markup." + opt);
        }
        return val == null ? def : val;
    }

    private String opt(String opt) {
        return get(opt, null);
    }

    static MarkupOptions from(ServletConfig config) {
        return new MarkupOptions(config);
    }
}
