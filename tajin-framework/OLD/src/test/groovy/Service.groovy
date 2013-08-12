import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-07-22
 */

@javax.inject.Singleton
class Service {

    @PostConstruct
    void init() {
        println "AAAAAAAAAAAAAAAAAA"
    }

    @PreDestroy
    void close() {
        println "BBBBBBBBBBBBBBBBBBB"
    }

}
