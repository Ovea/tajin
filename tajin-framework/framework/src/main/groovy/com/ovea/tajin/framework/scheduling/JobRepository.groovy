package com.ovea.tajin.framework.scheduling

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
interface JobRepository {
    void save(Job job)
    List<Job> listPendingJobs()
}
