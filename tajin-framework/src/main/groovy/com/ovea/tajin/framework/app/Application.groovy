package com.ovea.tajin.framework.app

import com.google.inject.Binder
import com.ovea.tajin.framework.prop.PropertySettings

public interface Application {
    /**
     * Called at initialization time
     */
    void onInit(Binder binder, PropertySettings settings)

    /**
     * Called when web application is started and injections are completed
     */
    void onStart()

    /**
     * Called when application is stopped
     */
    void onstop()
}
