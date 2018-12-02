package org.yq.open.openerp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.yq.open.openerp.security.CustomTokenBasedRememberMeServices;
import org.yq.open.openerp.security.CustomUserDetailsService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public TokenBasedRememberMeServices tokenBasedRememberMeServices() {
        CustomTokenBasedRememberMeServices tbrms = new CustomTokenBasedRememberMeServices("openerp_allkey", customUserDetailsService);
        tbrms.setTokenValiditySeconds(60 * 60 * 24 * 30);
        // 设置checkbox的参数名为rememberMe（默认为remember-me），注意如果是ajax请求，参数名不是checkbox的name而是在ajax的data里
        return tbrms;
    }




    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/login.html", "/assets/**","/api/proInventory/downDesc","/h2-console/**").permitAll();
        http.authorizeRequests().anyRequest().authenticated();
        http.formLogin().
                loginPage("/login.html").
                loginProcessingUrl("/api/user/login").
                defaultSuccessUrl("/").
                usernameParameter("account").passwordParameter("password").successHandler(new AuthenticationSuccessHandler() {

            @Override
            public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
                httpServletResponse.setContentType("application/json;charset=utf-8");
                PrintWriter out = httpServletResponse.getWriter();
                out.write("{\"code\":\"0\",\"msg\":\"登录成功\"}");
                out.flush();
                out.close();

            }
        }).failureHandler(new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                httpServletResponse.setContentType("application/json;charset=utf-8");
                PrintWriter out = httpServletResponse.getWriter();
                out.write("{\"code\":\"-999\",\"msg\":\"" + e.getMessage() + "\"}");
                out.flush();
                out.close();

            }
        }).and().logout().logoutUrl("/api/user/logout").permitAll().invalidateHttpSession(true).deleteCookies("JSESSIONID","remember-me").
                logoutSuccessHandler(new LogoutSuccessHandler() {
                    @Override
                    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
                        httpServletResponse.sendRedirect("/login.html");
                    }
                }).and().rememberMe().tokenValiditySeconds(2592000).key("openerp_allkey")
                .rememberMeServices(tokenBasedRememberMeServices()).userDetailsService(customUserDetailsService).and().csrf().disable().headers().frameOptions().disable();

    }
}
