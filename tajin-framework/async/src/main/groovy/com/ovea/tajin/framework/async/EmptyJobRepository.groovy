package com.ovea.tajin.framework.async

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-06
 */
 class EmptyJobRepository implements JobRepository {
     @Override
     void delete(List<ScheduledJobTriggeredEvent> jobs) {

     }

     @Override
     void insert(ScheduledJobTriggeredEvent job) {

     }

     @Override
     void update(ScheduledJobTriggeredEvent job) {

     }

     @Override
     List<ScheduledJobTriggeredEvent> listPendingJobs() {
         return []
     }
 }
