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
package com.ovea.tajin.framework.template;

import com.ovea.tajin.framework.core.Resource;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ResourceTemplateResolver extends TemplateResolverSkeleton {

    public ResourceTemplateResolver(TemplateCompiler compiler) {
        super(compiler);
    }

    @Override
    protected Resource tryPath(String path) {
        try {
            Resource r = Resource.resource(path);
            if (r.isExist()) {
                return r;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
