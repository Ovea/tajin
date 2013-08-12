package com.ovea.tajin.framework.core

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-08-12
 */
class MissingPropertySettingException extends RuntimeException {
    public MissingPropertySettingException(String key) {
        super("Property '" + key + "' is missing");
    }
}
