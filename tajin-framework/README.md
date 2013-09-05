# Tajin Framework #

## Modules ##

### tajin-i18n ###

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
