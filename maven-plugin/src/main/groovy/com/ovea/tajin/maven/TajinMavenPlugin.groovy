package com.ovea.tajin.maven

import com.ovea.tajin.tools.TajinResources
import org.apache.maven.plugin.AbstractMojo

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 * @threadSafe
 * @goal generate-resources
 * @phase generate-resources
 */
class TajinMavenPlugin extends AbstractMojo {

    /**
     * Base directory used to find resources defined in JSON config
     *
     * @parameter expression="${tajin.basedir}" default-value="${basedir}"
     * @required
     */
    File basedir;

    /**
     * JSON configuration
     *
     * @parameter expression="${tajin.config}" default-value="${basedir}/src/main/resources/META-INF/tajin.json"
     * @required
     */
    File config;

    /**
     * Keep the plugin running to watch for file changes and regenrate resources
     *
     * @parameter expression="${tajin.watch}" default-value="false"
     * @required
     */

    boolean watch;

    @Override
    void execute() {
        TajinResources.main('-d', basedir.absolutePath, '-c', config.absolutePath, watch ? '-watch' : '')
    }
}
