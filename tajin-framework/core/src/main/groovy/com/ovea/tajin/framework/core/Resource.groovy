/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.tajin.framework.core

import org.codehaus.groovy.runtime.IOGroovyMethods

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-01-08
 */
abstract class Resource {

    abstract URL getAsUrl()

    abstract File getAsFile()

    abstract List<Resource> getAlikes();

    abstract InputStream getInput()

    abstract OutputStream getOutput()

    abstract String getText()

    abstract String getText(String charset)

    abstract byte[] getBytes()

    abstract byte[] getBytes(String charset)

    abstract boolean isFile()

    abstract boolean isUrl()

    abstract boolean isExist()

    @Override
    abstract String toString()

    @Override
    abstract boolean equals(Object obj)

    @Override
    abstract int hashCode()

    public <T> T withOutput(Closure<T> closure) { IOGroovyMethods.withStream(output, closure) }

    public <T> T withInput(Closure<T> closure) { IOGroovyMethods.withStream(input, closure) }

    void copyTo(File dest) {
        dest.parentFile.mkdirs()
        dest.withOutputStream { os ->
            withInput { is ->
                os << is
            }
        }
    }

    static Resource valueOf(File file) { new FileResource(file) }

    static Resource valueOf(URL url) {
        File f = urlToFile(url)
        return f ? new FileResource(f) : new UrlResource(url)
    }

    static Resource valueOf(char[] data) { new CharResource(data) }

    static Resource valueOf(byte[] data) { new ByteResource(data) }

    static Resource valueOf(CharSequence data) { new CharSequenceResource(data) }

    static Resource classpath(ClassLoader classloader, String pattern) {
        if (!pattern)
            throw new IllegalArgumentException("Missing parameter")
        if (pattern.startsWith("/"))
            pattern = pattern.substring(1)
        return new ClassPathResource(classloader, pattern)
    }

    static Resource parse(String pattern) { parse(pattern, new File('.'), Thread.currentThread().contextClassLoader) }

    static Resource parse(String pattern, ClassLoader classLoader) { parse(pattern, new File('.'), classLoader) }

    static Resource parse(String pattern, File baseDir) { parse(pattern, baseDir, Thread.currentThread().contextClassLoader) }

    static Resource parse(String pattern, File webapp, ClassLoader classLoader) {
        if (!pattern)
            throw new IllegalArgumentException("Missing parameter")
        if (pattern.startsWith("classpath:") && !pattern.startsWith("classpath://"))
            return classpath(classLoader, pattern.substring(10))
        if (pattern.startsWith("web:") && !pattern.startsWith("web://"))
            return new FileResource(new File(webapp, pattern.substring(4)))
        if (pattern.startsWith("file:") && !pattern.startsWith("file://")) {
            return new FileResource(new File(pattern.substring(5)))
        }
        if (pattern =~ /(?i)[a-z]:\/\/.+/) {
            try {
                return new UrlResource(new URL(pattern))
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid resource " + pattern + " : " + e.message, e)
            }
        }
        File f = new File(pattern)
        return new FileResource(f.absolute ? f : new File(webapp, pattern))
    }

    private static abstract class ReadOnlyResource<V> extends Resource {

        private int hashCode = -1
        private V buffer

        @Override
        boolean isExist() { true }

        @Override
        final boolean isFile() { false }

        @Override
        final boolean isUrl() { false }

        @Override
        final File getAsFile() { throw new UnsupportedOperationException() }

        @Override
        final URL getAsUrl() { throw new UnsupportedOperationException() }

        @Override
        final OutputStream getOutput() { throw new UnsupportedOperationException() }

        @Override
        public List<Resource> getAlikes() { [this] }

        @Override
        String toString() { text }

        @Override
        InputStream getInput() { new ByteArrayInputStream(bytes) }

        @Override
        final int hashCode() {
            if (hashCode == -1) hashCode = hash()
            return hashCode
        }

        abstract int hash();
    }

    private static class ByteResource extends ReadOnlyResource<byte[]> {

        private ByteResource(byte[] data) {
            buffer = data
        }

        @Override
        byte[] getBytes() { buffer }

        @Override
        byte[] getBytes(String charset) { buffer }

        @Override
        String getText() { getText('UTF-8') }

        @Override
        String getText(String charset) { new String(buffer, charset) }

        @Override
        boolean equals(Object o) {
            if (this.is(o)) return true
            if (o == null || getClass() != o.getClass()) return false
            ByteResource that = (ByteResource) o
            return Arrays.equals(buffer, that.buffer)
        }

        @Override
        int hash() { Arrays.hashCode(buffer) }

    }

    private static class CharResource extends ReadOnlyResource<char[]> {

        private CharResource(char[] data) {
            buffer = data
        }

        @Override
        byte[] getBytes() { getBytes('UTF-8') }

        @Override
        byte[] getBytes(String charset) { text.getBytes(charset) }

        @Override
        String getText() { new String(buffer) }

        @Override
        String getText(String charset) { new String(buffer) }

        @Override
        boolean equals(Object o) {
            if (this.is(o)) return true
            if (o == null || getClass() != o.getClass()) return false
            CharResource that = (CharResource) o
            return Arrays.equals(buffer, that.buffer)
        }

        @Override
        int hash() { Arrays.hashCode(buffer) }

    }

    private static class CharSequenceResource extends ReadOnlyResource<CharSequence> {

        private CharSequenceResource(CharSequence data) {
            buffer = data
        }

        @Override
        byte[] getBytes() { getBytes('UTF-8') }

        @Override
        byte[] getBytes(String charset) { text.getBytes(charset) }

        @Override
        String getText() { buffer.toString() }

        @Override
        String getText(String charset) { buffer.toString() }

        @Override
        boolean equals(Object o) {
            if (this.is(o)) return true
            if (o == null || getClass() != o.getClass()) return false
            CharSequenceResource that = (CharSequenceResource) o
            return buffer == that.buffer
        }

        @Override
        int hash() { buffer.hashCode() }

    }

    private static abstract class UrlBasedResource extends Resource {

        @Override
        public List<Resource> getAlikes() {
            return [this]
        }

        @Override
        InputStream getInput() {
            URLConnection connection = asUrl.openConnection()
            connection.setUseCaches(false)
            connection.setDoInput(true)
            connection.setDoOutput(false)
            connection.connect()
            return new BufferedInputStream(connection.getInputStream())
        }

        @Override
        final OutputStream getOutput() {
            URLConnection connection = asUrl.openConnection()
            connection.setUseCaches(false)
            connection.setDoInput(false)
            connection.setDoOutput(true)
            connection.connect()
            return new BufferedOutputStream(connection.getOutputStream())
        }

        @Override
        File getAsFile() { throw new UnsupportedOperationException() }

        @Override
        boolean isUrl() { true }

        @Override
        final boolean isFile() { false }

        @Override
        final String getText() { getText('UTF-8') }

        @Override
        String getText(String charset) { new String(bytes, charset) }

        @Override
        final byte[] getBytes() { input.bytes }

        @Override
        byte[] getBytes(String charset) { input.bytes }
    }

    private static final class ClassPathResource extends UrlBasedResource {

        private final String path
        private final ClassLoader classloader

        private ClassPathResource(ClassLoader classloader, String path) {
            this.path = path
            this.classloader = classloader
        }

        @Override
        public List<Resource> getAlikes() {
            try {
                List<Resource> urls = new LinkedList<Resource>();
                Enumeration<URL> e = classloader.getResources(path);
                while (e.hasMoreElements()) {
                    urls.add(valueOf(e.nextElement()));
                }
                return urls.toArray(new Resource[urls.size()]);
            } catch (IOException e1) {
                throw new IllegalStateException(e1.message, e1);
            }
        }

        @Override
        URL getAsUrl() {
            URL url = classloader.getResource(path)
            if (url == null) {
                throw new IllegalStateException(path + " cannot be found on classpath")
            }
            return url
        }

        @Override
        String toString() { "classpath:" + path }

        @Override
        boolean equals(Object o) {
            if (this.is(o)) return true
            if (o == null || getClass() != o.getClass()) return false
            ClassPathResource that = (ClassPathResource) o
            return path.equals(that.path)
        }

        @Override
        int hashCode() { path.hashCode() }

        @Override
        boolean isExist() { classloader.getResource(path) != null }
    }

    private static final class UrlResource extends UrlBasedResource {
        private final URL url

        private UrlResource(URL url) {
            this.url = url
        }

        @Override
        URL getAsUrl() { url }

        @Override
        String toString() { url.toString() }

        @Override
        boolean equals(Object o) {
            if (this.is(o)) return true
            if (o == null || getClass() != o.getClass()) return false
            UrlResource that = (UrlResource) o
            return url.equals(that.url)
        }

        @Override
        int hashCode() { url.hashCode() }

        @Override
        boolean isExist() {
            try {
                input.close()
                return true
            } catch (ignored) {
                return false
            }
        }
    }

    private static final class FileResource extends Resource {
        private final File file

        private FileResource(File file) {
            this.file = file
        }

        @Override
        URL getAsUrl() { file.toURI().toURL() }

        @Override
        String toString() { file.toString() }

        @Override
        boolean equals(Object o) {
            if (this.is(o)) return true
            if (o == null || getClass() != o.getClass()) return false
            FileResource that = (FileResource) o
            return file.equals(that.file)
        }

        @Override
        int hashCode() { file.hashCode() }

        @Override
        boolean isExist() { file.exists() }

        @Override
        public List<Resource> getAlikes() { [this] }

        @Override
        InputStream getInput() { file.newInputStream() }

        @Override
        final OutputStream getOutput() { file.newOutputStream() }

        @Override
        File getAsFile() { file }

        @Override
        boolean isUrl() { true }

        @Override
        final String getText() { getText('UTF-8') }

        @Override
        String getText(String charset) { file.getText(charset) }

        @Override
        final byte[] getBytes() { file.bytes }

        @Override
        byte[] getBytes(String charset) { file.bytes }

        @Override
        final boolean isFile() { true }
    }

    private static File urlToFile(URL url) {
        try {
            return new File(url.toURI())
        } catch (ignored) {
            return null
        }
    }

}
