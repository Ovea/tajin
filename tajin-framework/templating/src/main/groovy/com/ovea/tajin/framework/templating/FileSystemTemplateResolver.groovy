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
package com.ovea.tajin.framework.templating

import com.ovea.tajin.framework.core.Resource
import com.ovea.tajin.framework.core.Settings

import javax.inject.Inject

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@javax.inject.Singleton
public final class FileSystemTemplateResolver extends TemplateResolverSkeleton {

    File baseDir = new File('.')

    @Inject
    void setSettings(Settings settings) {
        super.setSettings(settings)
        baseDir = settings.getFile('tajin.templating.baseDir', baseDir)
    }

    @Override
    protected Resource tryPath(String path) {
        File file = new File(baseDir, path);
        if (file.exists()) {
            try {
                if (!file.getCanonicalFile().getAbsolutePath().startsWith(baseDir.getAbsolutePath())) {
                    throw new TemplateResolverException("Web template folder " + baseDir + " cannot serve template " + path);
                }
                return Resource.valueOf(file.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new TemplateResolverException("Error getting URL from file " + file + " : " + e.getMessage(), e);
            } catch (IOException e) {
                throw new TemplateResolverException("Error accessing file path " + file + " : " + e.getMessage(), e);
            }
        }
        return null;
    }

}
