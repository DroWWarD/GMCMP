package ru.acs.grandmap

/**
 * Патчит window.fetch так, чтобы по умолчанию всегда было { credentials: "include" }.
 * Возвращает true, если патч применён (или уже был применён), и false, если fetch недоступен.
 */
@JsFun(
    """
    () => {
      if (typeof window === 'undefined' || !window.fetch) return false;
      if (window.__gma_fetch_patched__) return true;

      const orig = window.fetch.bind(window);
      window.fetch = (input, init) => {
        init = init ?? {};
        if (init.credentials === undefined) init.credentials = 'include';
        return orig(input, init);
      };

      window.__gma_fetch_patched__ = true;
      return true;
    }
    """
)
external fun patchFetchCredsInclude(): Boolean

fun enableFetchCredentialsInclude() {
    patchFetchCredsInclude()
}
