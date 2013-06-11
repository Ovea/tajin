package com.ovea.tajin.framework.support.groovy
/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-05-31
 */
class GroovyObjectExtension {

    static <T> T deepClone(T t) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ObjectOutputStream oos = new ObjectOutputStream(bos)
        oos.writeObject(t)
        oos.flush()
        ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray())
        ObjectInputStream ois = new ObjectInputStream(bin)
        return ois.readObject() as T
    }

}
