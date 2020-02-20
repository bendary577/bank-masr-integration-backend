package com.sun.supplierpoc.conf;


import com.sun.supplierpoc.services.security.CustomTokenStore;
import com.sun.supplierpoc.services.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Created by jeebb on 19/11/2014.
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    CustomTokenStore tokenStore;
    @Override
    @Bean
    protected UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
     /*   PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser("username")
                .password(encoder.encode("password"))
                .roles("USER");  */  }
/*    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authBuilder) throws Exception {
        authBuilder.inMemoryAuthentication().withUser("user").password(passwordEncoder().encode("user")).roles("ADMIN");
        authBuilder.inMemoryAuthentication().withUser("web-client").password(passwordEncoder().encode("web-client-secret")).roles("ADMIN");
       *//* authBuilder.inMemoryAuthentication().withUser("logan").password("Wolverine").roles("USER");
        authBuilder.inMemoryAuthentication().withUser("scott").password("Cyclops").roles("USER");
        authBuilder.inMemoryAuthentication().withUser("ororo").password("Storm").roles("USER");
 *//*   }*/




    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
/*    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests (). antMatchers ("/ oauth / token", "/ oauth / authorize **", "/ publishes"). permitAll ();

        http.requestMatchers().and().authorizeRequests().antMatchers("/**").access("hasRole('ADMIN')");
    }
/* @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests().antMatchers("/**").permitAll().antMatchers("/getAccount").authenticated()
  *//*              .antMatchers("/login", "/logout.do").permitAll()
                .antMatchers("/**").authenticated()
            .and()
                .formLogin()
                //.loginProcessingUrl("/login.do") // default spring oauth login url
                .usernameParameter("uid")
                .passwordParameter("pwd")
               // .loginPage("/login")
            .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout.do"))
            *//*.and()
                .userDetailsService(userDetailsService());
    }*/
}
