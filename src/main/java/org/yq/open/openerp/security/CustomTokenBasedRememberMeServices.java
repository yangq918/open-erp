package org.yq.open.openerp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomTokenBasedRememberMeServices extends TokenBasedRememberMeServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomTokenBasedRememberMeServices.class);

    protected  String customCookieDomain;

    public CustomTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }


    @Override
    public void setCookieDomain(String cookieDomain) {
        super.setCookieDomain(cookieDomain);
        this.customCookieDomain = cookieDomain;
    }

    @Override
    protected Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user) {
        Authentication successfulAuthentication = super.createSuccessfulAuthentication(request, user);
        MyUserDetails details = (MyUserDetails)successfulAuthentication.getPrincipal();
        LOGGER.info("init Session,user:{}",details.getUsername());
        request.getSession().setAttribute("user",details.getUser());
        return successfulAuthentication;
    }

    @Override
    protected void cancelCookie(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Cancelling cookie");
        Cookie cookie = new Cookie(this.getCookieName(), "nologin");
        cookie.setMaxAge(0);
        cookie.setPath(this.getCustomCookiePath(request));
        if (customCookieDomain != null) {
            cookie.setDomain(customCookieDomain);
        }
        response.addCookie(cookie);
    }

    private String getCustomCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : "/";
    }
}
