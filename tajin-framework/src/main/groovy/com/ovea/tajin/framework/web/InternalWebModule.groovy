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
package com.ovea.tajin.framework.web

import com.google.inject.Provides
import com.google.inject.servlet.ServletModule
import com.ovea.tajin.framework.app.Application
import com.ovea.tajin.framework.prop.PropertySettings
import com.ovea.tajin.framework.support.guice.*
import com.ovea.tajin.framework.support.jersey.GzipEncoder
import com.ovea.tajin.framework.support.jersey.SecurityResourceFilterFactory
import com.ovea.tajin.framework.support.shiro.PassthroughRealm
import com.ovea.tajin.framework.support.shiro.SecurityFilter
import com.ovea.tajin.framework.support.shiro.UsernamePasswordRealm
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.pam.AllSuccessfulStrategy
import org.apache.shiro.authc.pam.ModularRealmAuthenticator
import org.apache.shiro.codec.Hex
import org.apache.shiro.io.DefaultSerializer
import org.apache.shiro.realm.Realm
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.apache.shiro.web.mgt.WebSecurityManager
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager
import org.eclipse.jetty.servlets.CrossOriginFilter

import javax.inject.Named
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-26
 */
class InternalWebModule extends ServletModule implements ServletContextListener {

    final List<Application> applications = ServiceLoader.load(Application) as List
    private final PropertySettings settings

    InternalWebModule(PropertySettings settings) {
        this.settings = settings
    }

    @Override
    protected void configureServlets() {
        bind(GuiceContainer).in(javax.inject.Singleton)
        bind(CrossOriginFilter).in(javax.inject.Singleton)
        bind(Locale).toProvider(CookieLocaleManager)
        bind(PropertySettings).toInstance(settings)
        bind(org.apache.shiro.mgt.SecurityManager).to(WebSecurityManager)
        TajinGuice.in(binder()).handleMethodAfterInjection(Expand, ExpandHandler)
        applications.each {
            it.onInit(binder(), settings)
            bind(it.class).toInstance(it)
        }
        settings.getString('cors.allowedOrigins', null)?.with { String origin ->
            filter('/*').through(CrossOriginFilter, [
                allowedMethods: 'GET,POST,HEAD,PUT,DELETE',
                allowedOrigins: origin
            ])
        }
        filter('/*').through(HttpContextFilter)
        filter('/*').through(GuiceShiroFilter)
        filter('/*').through(SecurityFilter)
        serve("/*").with(GuiceContainer, [
            "com.sun.jersey.spi.container.ContainerResponseFilters": GzipEncoder.name,
            "com.sun.jersey.spi.container.ResourceFilters": SecurityResourceFilterFactory.name
        ])
    }

    @Provides
    @Named("loc")
    Cookie localeCookie() {
        return new HttpCookie(
            name: "loc",
            maxAge: 60 * 60 * 24 * 365,
            httpOnly: false
        )
    }

    @Provides
    @javax.inject.Singleton
    WebSecurityManager securityManager(Collection<Realm> realms, PropertySettings settings) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager(
            rememberMeManager: !settings.has("rememberme.cookie.name") ? null : new VersionedRememberMeManager(
                version: settings.getInt('rememberme.cookie.version', 1),
                serializer: new DefaultSerializer<>(),
                cipherKey: Hex.decode(settings.getString('rememberme.cookie.key')),
                cookie: new HttpCookie(
                    name: settings.getString("rememberme.cookie.name"),
                    httpOnly: true,
                    maxAge: settings.getInt("rememberme.cookie.days", 365) * 24 * 60 * 60
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

    @Provides
    @javax.inject.Singleton
    Collection<Realm> realms(UsernamePasswordRealm defaultRealm, PassthroughRealm passthroughRealm) {
        return [passthroughRealm, defaultRealm]
    }

    @Override
    void contextDestroyed(ServletContextEvent sce) {
        applications*.onstop()
    }

    @Override
    void contextInitialized(ServletContextEvent sce) {
        applications*.onStart()
    }

}
