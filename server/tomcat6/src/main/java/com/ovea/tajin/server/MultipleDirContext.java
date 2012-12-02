package com.ovea.tajin.server;

import org.apache.naming.resources.FileDirContext;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedHashSet;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-12-01
 */
public class MultipleDirContext extends FileDirContext {

    private LinkedHashSet<FileDirContextAdapter> virtualContexts = new LinkedHashSet<>();

    public MultipleDirContext() {
        super();
    }

    public MultipleDirContext(Hashtable env) {
        super(env);
    }

    public void setVirtualDocBase(String[] virtualDocBase) {
        for (String base : virtualDocBase) {
            FileDirContextAdapter currentContext = new FileDirContextAdapter(env);
            currentContext.setDocBase(base);
            virtualContexts.add(currentContext);
        }
    }

    @Override
    protected File file(String arg0) {
        File file = super.file(arg0);
        if (file == null) {
            for (FileDirContextAdapter virtualContext : virtualContexts) {
                file = virtualContext.file(arg0);
                if (file != null) {
                    return file;
                }
            }
        }
        return file;
    }

    @Override
    public void setDocBase(String arg0) {
        super.setDocBase(arg0);
    }

    @Override
    public void release() {
        super.release();
        for (FileDirContextAdapter virtualContext : virtualContexts) {
            virtualContext.release();
        }
    }

    @Override
    public void allocate() {
        super.allocate();
        for (FileDirContextAdapter virtualContext : virtualContexts) {
            virtualContext.setCached(this.isCached());
            virtualContext.setCacheTTL(this.getCacheTTL());
            virtualContext.setCacheMaxSize(this.getCacheMaxSize());
            virtualContext.setCaseSensitive(this.isCaseSensitive());
            virtualContext.setAllowLinking(this.getAllowLinking());
            virtualContext.allocate();
        }
    }

    private static final class FileDirContextAdapter extends FileDirContext {
        public FileDirContextAdapter() {
        }

        public FileDirContextAdapter(Hashtable env) {
            super(env);
        }

        protected File file(String arg0) {
            return super.file(arg0);
        }
    }
}

