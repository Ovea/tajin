package com.ovea.tajin.framework.scheduling

import com.ovea.tajin.framework.util.Uuid
import groovy.transform.ToString

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
@ToString(includes = ['id', 'name', 'start'])
class Job {
    String id = Uuid.generate()
    Date createdDate = new Date()
    Date updatedDate = createdDate
    Date start = createdDate
    String name
    Date end
    Map<String, ?> data = [:]
}
