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
package com.ovea.tajin.framework.templating;

import com.ovea.tajin.framework.core.Resource
import com.ovea.tajin.framework.core.Settings

import javax.inject.Inject;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@javax.inject.Singleton
public final class ResourceTemplateResolver extends TemplateResolverSkeleton {

    ClassLoader classLoader = Thread.currentThread().contextClassLoader
    File baseDir = new File('.')

    @Inject
    void setSettings(Settings settings) {
        super.setSettings(settings)
        baseDir = settings.getFile('tajin.templating.baseDir', baseDir)
    }

    @Override
    protected Resource tryPath(String path) {
        try {
            Resource r = Resource.parse(path, baseDir, classLoader)
            if (r.isExist()) {
                return r;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

}
