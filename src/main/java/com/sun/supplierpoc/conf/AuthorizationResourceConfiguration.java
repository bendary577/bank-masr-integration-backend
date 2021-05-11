package com.sun.supplierpoc.conf;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class AuthorizationResourceConfiguration  extends
        ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "@ENTREPREWARE";

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID).stateless(false);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests().antMatchers("/**").permitAll();
        http.
                anonymous().disable()
                .authorizeRequests()
                .antMatchers("/zeal/**").permitAll()
                .antMatchers("/addAccount").authenticated()
                .antMatchers("/opera/**").authenticated()
                .antMatchers("/**").authenticated()
                .antMatchers("/activity/**").authenticated()
                .antMatchers("/amazon/**").authenticated()
                .antMatchers("/paymentTest").authenticated()

                .and().exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }


    }