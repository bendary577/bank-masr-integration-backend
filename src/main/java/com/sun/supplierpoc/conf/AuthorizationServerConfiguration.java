package com.sun.supplierpoc.conf;


import com.sun.supplierpoc.services.security.CustomApprovalStore;
import com.sun.supplierpoc.services.security.CustomAuthorizationCodeServices;
import com.sun.supplierpoc.services.security.CustomTokenStore;
import com.sun.supplierpoc.services.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.web.client.RestTemplate;

/**
 * Created by jeebb on 19/11/2014.
 */
@Configuration
@EnableAuthorizationServer

public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {
/*    private AuthenticationManager authenticationManagerBean;
    @Autowired
    public void setAuthenticationManagerBean(AuthenticationManager authenticationManagerBean) {
        this.authenticationManagerBean = authenticationManagerBean;
    }*/

    @Bean
    public CustomClientDetailsService clientDetailsService() {
        return new CustomClientDetailsService();
    }
    @Bean
    public CustomUserDetailsService clientUsersService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public CustomTokenStore tokenStore() {
        return new CustomTokenStore();
    }

    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        return new CustomAuthorizationCodeServices();
    }

    @Bean
    public CustomApprovalStore approvalStore() {
        return new CustomApprovalStore();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsService());

    }
    @Override
    public void configure(
            AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");
    }

    @Autowired
    private AuthenticationManager authenticationManager;

/*    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager);
    }

/*    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("web-client")
                .secret("web-client-secret")
                .authorizedGrantTypes("client_credentials", "password", "refresh_token")
                .scopes("all")
                .resourceIds("oauth2-resource")
                .accessTokenValiditySeconds(1200)
                .refreshTokenValiditySeconds(50000);

    }*/
        @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.approvalStore(approvalStore())
                .authorizationCodeServices(authorizationCodeServices()).userDetailsService(clientUsersService())
                .tokenStore(tokenStore()).authenticationManager(authenticationManager);

    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
