package org.yq.open.openerp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomTokenBasedRememberMeServices extends TokenBasedRememberMeServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomTokenBasedRememberMeServices.class);

    public CustomTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }



    @Override
    protected Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user) {
        Authentication successfulAuthentication = super.createSuccessfulAuthentication(request, user);
        MyUserDetails details = (MyUserDetails)successfulAuthentication.getPrincipal();
        LOGGER.info("init Session,user:{}",details.getUsername());
        request.getSession().setAttribute("user",details.getUser());
        return successfulAuthentication;
    }
}
