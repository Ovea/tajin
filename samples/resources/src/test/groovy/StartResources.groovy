import org.testatoo.container.ContainerConfiguration
import org.testatoo.container.TestatooContainer

ContainerConfiguration.create()
    .buildContainer(TestatooContainer.JETTY9)
    .start()
