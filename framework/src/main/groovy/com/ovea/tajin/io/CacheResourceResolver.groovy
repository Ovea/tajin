package com.ovea.tajin.io

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-03-11
 */
class CacheResourceResolver implements ResourceResolver {

    final File cacheFolder

    CacheResourceResolver(File cacheFolder) {
        this.cacheFolder = cacheFolder
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs()
        }
    }

    @Override
    Resource resolve(Resource r) {
        if (!r.file && r.url) {
            String path = r.asUrl.path
            File cached = new File(cacheFolder, path.substring(path.lastIndexOf('/') + 1))
            if (!cached.exists()) {
                cached.withOutputStream { OutputStream os -> os << r.bytes }
            }
            return Resource.file(cached)
        }
        return r
    }
}
