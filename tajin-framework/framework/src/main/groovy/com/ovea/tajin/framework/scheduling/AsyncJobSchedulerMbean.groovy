package com.ovea.tajin.framework.scheduling

import org.eclipse.jetty.jmx.ObjectMBean

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-07
 */
class AsyncJobSchedulerMbean extends ObjectMBean {
    final AsyncJobScheduler scheduler

    AsyncJobSchedulerMbean(AsyncJobScheduler scheduler) {
        super(scheduler)
    }
}
