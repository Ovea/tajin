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

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-03-11
 */
class CacheResourceResolver implements ResourceResolver {

    final File cacheFolder

    CacheResourceResolver(File cacheFolder) {
        this.cacheFolder = cacheFolder
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs()
        }
    }

    @Override
    Resource resolve(Resource r) {
        if (!r.file && r.url) {
            String path = r.asUrl.path
            File cached = new File(cacheFolder, path.substring(path.lastIndexOf('/') + 1))
            if (!cached.exists()) {
                cached.withOutputStream { OutputStream os -> os << r.bytes }
            }
            return Resource.file(cached)
        }
        return r
    }
}
