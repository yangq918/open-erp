package org.yq.open.openerp.security;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.yq.open.openerp.entity.UseRecord;
import org.yq.open.openerp.entity.User;
import org.yq.open.openerp.repository.UserRepository;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationProvider.class);


    @Autowired
    private HttpSession session;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        LOGGER.info("name:{},password:{}",name,password);
        User d = new User();
        d.setAccount(name);
        Example<User> ex = Example.of(d);
        Optional<User> optional = userRepository.findOne(ex);

        if (optional.isPresent()) {
            User u = optional.get();
            if (!StringUtils.equals(u.getPassword(), password)) {
                throw new BadCredentialsException("用户名或密码错误");
            }

            session.setAttribute("user", u);
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            Authentication auth = new UsernamePasswordAuthenticationToken(name, password, grantedAuthorities);
            return auth;
        } else {
            throw new AuthenticationCredentialsNotFoundException("用户不存在");
        }

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
