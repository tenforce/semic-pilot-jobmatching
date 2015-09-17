package eu.esco.demo.jobcvmatching.web.filter;

import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import org.apache.commons.collections15.iterators.IteratorEnumeration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

public class LanguageFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String language = request.getParameter("language");
    if (StringUtils.isBlank(language)) language = getLanguageCookie(request);
    if (StringUtils.isBlank(language)) language = HardcodedConfiguration.defaultLanguage;

    setLanguageCookie(response, language);
    filterChain.doFilter(new LocaleFixHttpServletRequest(request, new Locale(language)), response);
  }

  private void setLanguageCookie(HttpServletResponse httpResponse, String newLanguage) {
    Cookie languageCookie = new Cookie("semicLanguage", newLanguage);
    languageCookie.setMaxAge(3600 * 24 * 7); //1 week
    languageCookie.setPath("/");
    languageCookie.setComment("Used to remember language selected by the user");
    languageCookie.setSecure(false);
    httpResponse.addCookie(languageCookie);
  }

  private String getLanguageCookie(HttpServletRequest httpRequest) {
    Cookie[] cookies = httpRequest.getCookies();
    if (null == cookies) return null;
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals("semicLanguage")) return cookie.getValue();
    }
    return null;
  }

  private class LocaleFixHttpServletRequest extends HttpServletRequestWrapper {
    private final Locale locale;

    private LocaleFixHttpServletRequest(HttpServletRequest request, Locale locale) {
      super(request);
      this.locale = locale;
    }

    @Override
    public Enumeration getLocales() {
      return new IteratorEnumeration<>(Collections.singletonList(locale).iterator());
    }

    @Override
    public Locale getLocale() {
      return locale;
    }

  }
}
