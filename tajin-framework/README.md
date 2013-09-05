# Tajin Framework #

## Modules ##

### tajin-i18n ###

Tajin I18N support.

__Features__

 - Automatic bundle reloading
 - Caching
 - JSON bundle support
 - Javascript bundle support
 - ResourceBundle support
 - Google Guice integration

__Usage__

Add a field annotated with `@Bundle` in your class with type `I18NBundlerProvider`, with the wanted bundle. Then access the bundle for a locale and translate.

```
static class MyClass {

    @Bundle('classpath:bundle.js')
    I18NBundlerProvider i18n

    void aMethod() {
        i18n.getBundle(Locale.CANADA_FRENCH).getValue('mykey', ['10', 'seconds'])
    }
}
```

__Settings__

 - `tajin.i18n.miss`: Specify behavior on key not found.
    - Default: `THROW_EXCEPTION`
    - Possible values: `THROW_EXCEPTION`, `RETURN_NULL`, `RETURN_KEY`
 - `tajin.i18n.cache.maximumSize`: Specify the maximum cache size of localized bundles. Bundles are a set of merges properties for a given locale. In an application handling mainly 2 or 3 locales, you can reduce this value.
    - Default: `100`
    - Set it to `-1` for no maximum size (i.e. when locale values are controled)
    - Set it to `0` for no cache at all
 - `tajin.i18n.cache.expirationSeconds`: Specify the maximum of time a bundle will remain in cache before being reloaded.
    - Default: `3600`
    - Set it to `-1` for no eviction
    - Set it to `0` for no cache at all

### tajin-async ###

Adds asynchronous Event support in your project. This module contains a `Dispatcher` based on Google Guava `EventBus`.

__Settings__

 - `tajin.async.dispatcher.minPoolSize`: Specify the minimum pool size for async events
    - Default: `0`
 - `tajin.async.dispatcher.maxPoolSize`: Specify the maximum pool size for async events. If the pool size is reached, events will be dispatched on the same thread firing them.
    - Default: `100`

### tajin-all ###

This is a bundle containing all Tajin modules at once.
