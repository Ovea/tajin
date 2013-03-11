package com.ovea.tajin.io

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-03-11
 */
interface ResourceResolver {
    Resource resolve(Resource resource)

    ResourceResolver DEFAULT = new ResourceResolver() {
        @Override
        Resource resolve(Resource resource) {
            return resource
        }
    }
}
