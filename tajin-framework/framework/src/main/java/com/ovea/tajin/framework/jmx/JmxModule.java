package com.ovea.tajin.framework.jmx;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.ovea.tajin.framework.jmx.annotation.JmxBean;
import com.ovea.tajin.framework.support.guice.ClassToTypeLiteralMatcherAdapter;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-08
 */
public class JmxModule extends AbstractModule {
    @Override
    protected void configure() {
        bindListener(ClassToTypeLiteralMatcherAdapter.adapt(Matchers.annotatedWith(JmxBean.class)), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                final Provider<JmxExporter> exporter = encounter.getProvider(JmxExporter.class);
                encounter.register(new InjectionListener<I>() {
                    @Override
                    public void afterInjection(I injectee) {
                        exporter.get().register(injectee);
                    }
                });
            }
        });
    }

    @Provides
    @Singleton
    JmxExporter jmxExporter(MBeanServer server) {
        MycilaJmxExporter exporter = new MycilaJmxExporter(server);
        exporter.setExportBehavior(ExportBehavior.FAIL_ON_EXISTING);
        exporter.setEnsureUnique(false);
        return exporter;
    }

    @Provides
    @Singleton
    MBeanServer mBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
}
