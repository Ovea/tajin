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
package com.ovea.tajin.maven;

import com.ovea.tajin.TajinConfig;
import com.ovea.tajin.io.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-27
 */
public abstract class TajinMavenPlugin extends AbstractMojo {

    /**
     * Base directory used to find resources defined in JSON config
     *
     * @parameter expression="${tajin.webapp}" default-value="${basedir}/src/main/webapp"
     * @required
     */
    protected File webapp;

    /**
     * JSON configuration
     *
     * @parameter expression="${tajin.config}" default-value="${basedir}/src/main/webapp/WEB-INF/tajin.json"
     * @required
     */
    protected File config;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project = new MavenProject();

    /**
     * Optional settings (properties) to pass to goals
     *
     * @parameter
     */
    protected Properties settings = new Properties();

    @Override
    public final void execute() {
        Properties p = new Properties();
        p.putAll(project.getProperties());
        p.putAll(settings);
        Map<String, Object> ctx = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            ctx.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        execute(new TajinConfig(webapp, Resource.file(config), ctx));
    }

    abstract void execute(TajinConfig config);
}
