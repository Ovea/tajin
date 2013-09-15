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
package com.ovea.tajin.framework.app

import com.google.inject.Injector
import com.google.inject.Provider
import com.google.inject.servlet.RequestScoped
import com.google.inject.servlet.ServletModule
import com.mycila.guice.ext.web.HttpContextFilter
import com.ovea.tajin.framework.security.TokenBuilder
import com.ovea.tajin.framework.support.guice.WebBinder
import com.ovea.tajin.framework.support.jersey.AuditResourceFilterFactory
import com.ovea.tajin.framework.support.jersey.JSONP
import com.ovea.tajin.framework.support.jersey.PermissionResourceFilterFactory
import com.ovea.tajin.framework.support.jersey.SecurityResourceFilterFactory
import com.ovea.tajin.framework.support.shiro.GuiceShiroFilter
import com.ovea.tajin.framework.support.shiro.MemoryCacheManager
import com.ovea.tajin.framework.support.shiro.SecurityFilter
import com.ovea.tajin.framework.support.shiro.VersionedRememberMeManager
import com.ovea.tajin.framework.util.PropertySettings
import com.ovea.tajin.framework.util.PropertySettingsMBean
import com.ovea.tajin.framework.web.CookieCleaner
import com.ovea.tajin.framework.web.CookieLocaleManager
import com.ovea.tajin.framework.web.PerfLog
import com.sun.jersey.api.core.DefaultResourceConfig
import com.sun.jersey.api.core.ResourceConfig
import com.sun.jersey.guice.JerseyServletModule
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import com.sun.jersey.spi.container.servlet.WebConfig
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy
import org.apache.shiro.authc.pam.ModularRealmAuthenticator
import org.apache.shiro.authz.ModularRealmAuthorizer
import org.apache.shiro.cache.CacheManager
import org.apache.shiro.codec.Hex
import org.apache.shiro.io.DefaultSerializer
import org.apache.shiro.realm.Realm
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.apache.shiro.web.mgt.WebSecurityManager
import org.apache.shiro.web.servlet.SimpleCookie
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager
import org.eclipse.jetty.servlets.CrossOriginFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import javax.servlet.ServletException

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-26
 */
class InternalWebModule extends ServletModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tajin)

    private static final int DAY_SEC = 60 * 60 * 24

    private final List<Application> applications = new LinkedList<>()
    private final PropertySettings settings

    InternalWebModule(PropertySettings settings, Collection<Application> applications) {
        this.applications.addAll(applications)
        this.settings = settings
    }

    @Override
    protected void configureServlets() {
        bind(PropertySettings).toInstance(settings)
        bind(PropertySettingsMBean)
        bind(Locale).toProvider(CookieLocaleManager).in(RequestScoped)

        // bind TokenBuilder if needed
        settings.getString('token.key', null)?.with { String key ->
            LOGGER.info(" + TokenBuilder support")
            bind(TokenBuilder).toProvider(new Provider<TokenBuilder>() {
                TokenBuilder get() { new TokenBuilder(Hex.decode(key)) }
            }).in(javax.inject.Singleton)
        }

        // bind filters
        bind(GuiceContainer).in(javax.inject.Singleton)
        bind(CrossOriginFilter).in(javax.inject.Singleton)

        // configure CORS filter if desired
        settings.getString('cors.allowedOrigins', null)?.with { String origin ->
            LOGGER.info(" + CORS support")
            filter('/*').through(CrossOriginFilter, [
                allowedMethods: 'GET,POST,HEAD,PUT,DELETE',
                allowedOrigins: origin
            ])
        }

        // important filter to manage HTTP contextual scopes
        filter('/*').through(HttpContextFilter)

        // setup cookie cleaner if required
        if (!settings.getList('cookies.delete').empty) {
            LOGGER.info(" + Cookie cleaner support")
            filter('/*').through(CookieCleaner)
        }

        // setup security layer if required
        boolean secured = settings.getBoolean('security.enabled', false)
        if (secured) {
            LOGGER.info(" + Securtity support")
            bind(org.apache.shiro.mgt.SecurityManager).to(WebSecurityManager)
            bind(WebSecurityManager).toProvider(new Provider<WebSecurityManager>() {
                @Inject Injector injector

                @Override
                WebSecurityManager get() {
                    List<Realm> realms = settings.getStrings('security.realms').collect { it.empty ? null : injector.getInstance(Thread.currentThread().contextClassLoader.loadClass(it)) as Realm }
                    DefaultWebSecurityManager manager = new DefaultWebSecurityManager(
                        // WARNNG: order of setters is important
                        sessionManager: new ServletContainerSessionManager(),
                        rememberMeManager: !settings.has("rememberme.cookie.name") ? null : new VersionedRememberMeManager(
                            version: settings.getInt('rememberme.cookie.version', 1),
                            serializer: new DefaultSerializer<>(),
                            cipherKey: Hex.decode(settings.getString('rememberme.cookie.key')),
                            cookie: new SimpleCookie(settings.getString("rememberme.cookie.name")).with {
                                it.httpOnly = true
                                it.maxAge = settings.getInt("rememberme.cookie.days", 365) * DAY_SEC
                                if (settings.has("rememberme.cookie.domain")) {
                                    it.domain = settings.getString("rememberme.cookie.domain")
                                }
                                if (settings.has("rememberme.cookie.path")) {
                                    it.path = settings.getString("rememberme.cookie.path")
                                }
                                return it
                            }
                        ),
                        authenticator: new ModularRealmAuthenticator(
                            authenticationStrategy: new FirstSuccessfulStrategy(),
                        ),
                        authorizer: new ModularRealmAuthorizer(),
                        realms: realms,
                        cacheManager: settings.getString('security.cache', MemoryCacheManager.name).with { it.trim().length() == 0 ? null : injector.getInstance(Thread.currentThread().contextClassLoader.loadClass(it)) as CacheManager },
                    )
                    SecurityUtils.securityManager = manager
                    return manager
                }
            }).in(javax.inject.Singleton)
            filter('/*').through(GuiceShiroFilter)
            filter('/*').through(SecurityFilter)
        }

        // setup performance logger
        if (settings.getBoolean('logging.perf', false)) {
            LOGGER.info(" + Performance logging support")
            filter('/*').through(PerfLog, [
                secured: secured as String,
                ignores: settings.getString('logging.perf.ignores', '')
            ])
        }

        // setup REST API
        install(new JerseyServletModule())
        bind(ResourceConfig).to(DefaultResourceConfig).in(javax.inject.Singleton)
        bind(RootPath)
        def initParams = [
            (ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS): [JSONP.RequestFilter].name.join(';'),
            (ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS): [JSONP.ResponseFilter].name.join(';'),
            ((ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES)): [AuditResourceFilterFactory]*.name.join(';')
        ]
        if (secured) {
            initParams << [
                (ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES): [AuditResourceFilterFactory, SecurityResourceFilterFactory, PermissionResourceFilterFactory]*.name.join(';')
            ]
        }
        serve("/*").with(JerseyContainer, initParams)

        // configure discovered applications
        WebBinder webBinder = new WebBinder(binder())
        applications.each {
            LOGGER.info("Configuring application: ${it.class.simpleName}")
            it.onInit(webBinder, settings)
            bind(it.class).toInstance(it)
        }

    }

    @javax.inject.Singleton
    static class JerseyContainer extends GuiceContainer {

        @Inject
        Provider<ResourceConfig> config

        @Inject
        JerseyContainer(Injector injector) {
            super(injector)
        }

        @Override
        protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props, WebConfig webConfig) throws ServletException {
            return config.get()
        }
    }

}
