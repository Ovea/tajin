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
package com.ovea.tajin.util

import java.lang.reflect.Method

public final class IsolatedClassLoader extends URLClassLoader {

    private final boolean childFirst;
    private final boolean childFirstResources;
    private final List<File> classpath = new LinkedList<File>();
    private final Set<String> excludedResources = new HashSet<String>();

    private IsolatedClassLoader(ClassLoader parent, boolean childFirst, boolean childFirstResources) {
        super(new URL[0], parent == null ? ClassLoader.getSystemClassLoader().getParent() : parent);
        this.childFirst = childFirst && parent != null;
        this.childFirstResources = childFirstResources && parent != null;
    }

    public List<File> getPaths() {
        return Collections.unmodifiableList(classpath);
    }

    public IsolatedClassLoader add(File... files) {
        return add(Arrays.asList(files));
    }

    public IsolatedClassLoader add(Iterable<File> files) {
        for (File file : files)
            classpath.add(file);
        for (URL url : toURLs(files))
            addURL(url);
        return this;
    }

    public IsolatedClassLoader add(URL... urLs) {
        for (URL url : urLs) {
            addURL(url);
        }
        return this;
    }

    public IsolatedClassLoader excludeResource(String res) {
        excludedResources.add(res);
        return this;
    }

    public <T> Class<T> load(String className) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this);
        try {
            return (Class<T>) loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = findLoadedClass(name);
        if (clazz != null) {
            if (resolve) resolveClass(clazz);
            return (clazz);
        }
        // if not child first, check the parent first
        if (!childFirst)
            try {
                return checkParent(name, resolve);
            } catch (ClassNotFoundException ignored) {
            }
        // if not found, check this classloader
        try {
            return checkMe(name, resolve);
        } catch (ClassNotFoundException ignored) {
        }
        // then check the parent if we first checked this classloader
        if (childFirst)
            try {
                return checkParent(name, resolve);
            } catch (ClassNotFoundException ignored) {
            }
        throw new ClassNotFoundException(name);
    }


    @Override
    public URL getResource(String name) {
        if (excludedResources.contains(name))
            return null;
        URL url;
        // (1) Delegate to parent if requested
        if (!childFirstResources) {
            url = getParent().getResource(name);
            if (url != null) return url;
        }
        // (2) Search local repositories
        url = findResource(name);
        if (url != null)
            return url;
        // (3) Delegate to parent unconditionally if not already attempted
        if (childFirstResources)
            url = getParent().getResource(name);
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        final List<URL> urls = new LinkedList<URL>();
        if (!excludedResources.contains(name)) {
            // (1) Delegate to parent if requested
            if (!childFirstResources) {
                Enumeration<URL> enums = getParent().getResources(name);
                while (enums.hasMoreElements())
                    urls.add(enums.nextElement());
            }
            // (2) Search local repositories
            Enumeration<URL> enums = findResources(name);
            while (enums.hasMoreElements())
                urls.add(enums.nextElement());
            // (3) Delegate to parent unconditionally if not already attempted
            if (childFirstResources) {
                enums = getParent().getResources(name);
                while (enums.hasMoreElements())
                    urls.add(enums.nextElement());
            }
        }
        return new Enumeration<URL>() {
            final Iterator<URL> it = urls.iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public URL nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (excludedResources.contains(name))
            return null;
        InputStream is = null;
        // (1) Delegate to parent if requested
        if (!childFirstResources) {
            is = getParent().getResourceAsStream(name);
            if (is != null) return is;
        }
        // (2) Search local repositories
        URL url = findResource(name);
        if (url != null)
            try {
                return url.openStream();
            } catch (IOException ignored) {
            }
        // (3) Delegate to parent unconditionally if not already attempted
        if (childFirstResources)
            is = getParent().getResourceAsStream(name);
        return is;
    }

    private Class<?> checkMe(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findClass(name);
        if (clazz != null)
            if (resolve) resolveClass(clazz);
        return (clazz);
    }

    private Class<?> checkParent(String name, boolean resolve) throws ClassNotFoundException {
        ClassLoader loader = getParent();
        Class<?> clazz = loader.loadClass(name);
        if (clazz != null)
            if (resolve) resolveClass(clazz);
        return clazz;
    }

    public <T> T runMain(String mainClass, String... args) throws Exception {
        Class<?> c = load(mainClass);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this);
        try {
            Method main = c.getDeclaredMethod("main", [String[].class] as Class<?>[]);
            main.invoke(null, [args] as Object[]);
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public <T> T instanciate(String className) {
        try {
            return this.<T> load(className).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ClassLoader " + getClass().getName());
        for (File jar : classpath)
            sb.append("\n - ").append(jar);
        if (getParent() instanceof IsolatedClassLoader)
            for (File file : ((IsolatedClassLoader) getParent()).getPaths())
                sb.append("\n - ").append(file);
        return sb.toString();
    }

    private static URL[] toURLs(Iterable<File> libs) {
        List<URL> urls = new ArrayList<URL>();
        for (File lib : libs)
            try {
                urls.add(lib.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        return urls.toArray(new URL[urls.size()]);
    }

    public static IsolatedClassLoader create(ClassLoader parent, boolean childFirst, boolean childFirstResources) {
        return new IsolatedClassLoader(parent, childFirst, childFirstResources);
    }

    public static IsolatedClassLoader isolated() {
        return create(null, false, true);
    }

}
