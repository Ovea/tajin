import com.ovea.tajin.server.ContainerConfiguration
import com.ovea.tajin.server.Server

ContainerConfiguration.create()
    .webappRoot('src/test/webapp')
    .buildContainer(Server.JETTY9)
    .start();
