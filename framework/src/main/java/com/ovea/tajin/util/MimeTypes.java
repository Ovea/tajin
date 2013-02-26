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
package com.ovea.tajin.util;

import java.io.IOException;
import java.util.Properties;

public final class MimeTypes {

    public static final String UNKNOWN = "application/octet-stream";
    private static final Properties MIMES = new Properties();

    private MimeTypes() {
    }

    static {
        try {
            MIMES.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("mime.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String getContentTypeForExtension(String ext) {
        if (ext == null)
            return UNKNOWN;
        String type = MIMES.getProperty(ext.toLowerCase());
        return type == null ? UNKNOWN : type;
    }

    public static String getContentTypeForPath(String path) {
        int pos = path.lastIndexOf('.');
        if (pos == -1)
            return UNKNOWN;
        String type = MIMES.getProperty(path.substring(pos + 1));
        return type == null ? UNKNOWN : type;
    }
}
