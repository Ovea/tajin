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

import com.google.inject.Binder
import com.google.inject.Injector
import com.google.inject.Provider
import com.google.inject.servlet.RequestScoped
import com.google.inject.servlet.ServletModule
import com.ovea.tajin.framework.util.PropertySettings
import com.ovea.tajin.framework.security.TokenBuilder
import com.ovea.tajin.framework.support.guice.*
import com.ovea.tajin.framework.support.jersey.GzipEncoder
import com.ovea.tajin.framework.support.jersey.SecurityResourceFilterFactory
import com.ovea.tajin.framework.support.shiro.*
import com.ovea.tajin.framework.web.CookieLocaleManager
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.pam.AllSuccessfulStrategy
import org.apache.shiro.authc.pam.ModularRealmAuthenticator
import org.apache.shiro.codec.Hex
import org.apache.shiro.io.DefaultSerializer
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.apache.shiro.web.mgt.WebSecurityManager
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager
import org.eclipse.jetty.servlets.CrossOriginFilter

import javax.inject.Inject

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-26
 */
class InternalWebModule extends ServletModule {

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
        bind(Locale).toProvider(CookieLocaleManager).in(RequestScoped)

        // bind TokenBuilder if needed
        settings.getString('token.key', null)?.with { String key ->
            bind(TokenBuilder).toInstance(new TokenBuilder(Hex.decode(key)))
        }

        // bind filters
        bind(GuiceContainer).in(javax.inject.Singleton)
        bind(CrossOriginFilter).in(javax.inject.Singleton)

        // support method expanders
        TajinGuice.in(binder()).handleMethodAfterInjection(Expand, ExpandHandler)

        // configure discovered applications
        WebBinder webBinder = proxy(binder())
        applications.each {
            it.onInit(webBinder, settings)
            bind(it.class).toInstance(it)
        }

        // configure CORS filter if desired
        settings.getString('cors.allowedOrigins', null)?.with { String origin ->
            filter('/*').through(CrossOriginFilter, [
                allowedMethods: 'GET,POST,HEAD,PUT,DELETE',
                allowedOrigins: origin
            ])
        }

        // important filter to manage HTTP contextual scopes
        filter('/*').through(HttpContextFilter)

        // setup security layer if required
        if (settings.has('security.filter')) {
            bind(org.apache.shiro.mgt.SecurityManager).to(WebSecurityManager)
            bind(WebSecurityManager).toProvider(new Provider<WebSecurityManager>() {
                @Inject
                Injector injector

                @Override
                WebSecurityManager get() {
                    def realms = [UsernamePasswordRealm, PassthroughRealm].collect { injector.getInstance(it) }
                    DefaultWebSecurityManager manager = new DefaultWebSecurityManager(
                        rememberMeManager: !settings.has("rememberme.cookie.name") ? null : new VersionedRememberMeManager(
                            version: settings.getInt('rememberme.cookie.version', 1),
                            serializer: new DefaultSerializer<>(),
                            cipherKey: Hex.decode(settings.getString('rememberme.cookie.key')),
                            cookie: new com.ovea.tajin.framework.web.HttpCookie(
                                name: settings.getString("rememberme.cookie.name"),
                                httpOnly: true,
                                maxAge: settings.getInt("rememberme.cookie.days", 365) * DAY_SEC
                            )
                        ),
                        realms: realms,
                        sessionManager: new ServletContainerSessionManager(),
                        authenticator: new ModularRealmAuthenticator(
                            authenticationStrategy: new AllSuccessfulStrategy(),
                            realms: realms
                        )
                    )
                    SecurityUtils.securityManager = manager
                    return manager
                }
            }).in(javax.inject.Singleton)
            filter('/*').through(GuiceShiroFilter)
            filter('/*').through(SecurityFilter)
        }

        // setup REST API
        bind(RootPath)
        serve("/*").with(GuiceContainer, [
            "com.sun.jersey.spi.container.ContainerResponseFilters": GzipEncoder.name,
            "com.sun.jersey.spi.container.ResourceFilters": SecurityResourceFilterFactory.name
        ])
    }

    private static WebBinder proxy(Binder binder) {
        def proxy = new groovy.util.Proxy() {
            void configure(Closure<?> c) {
                c.delegate = binder
                c()
            }
        }
        proxy.adaptee = binder
        return proxy as WebBinder
    }

}
