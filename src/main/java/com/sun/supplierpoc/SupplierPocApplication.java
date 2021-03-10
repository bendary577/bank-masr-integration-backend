package com.sun.supplierpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class SupplierPocApplication {

    public static void main(String[] args) {
        ApplicationContext context =SpringApplication.run(SupplierPocApplication.class, args);

/*        if (args .length > 0 && "init".equalsIgnoreCase(args[0])) {
          //  LOGGER.info("Start initializing the sample oauth data");

            MongoTemplate mongoTemplate = (MongoTemplate) context.getBean(MongoTemplate.class);

            // init the users
            User mongoUser = new User();
            mongoUser.setUsername("user");
            mongoUser.setPassword("user");
            mongoUser.setRoles(Sets.newHashSet("ROLE_USER"));
            mongoTemplate.save(mongoUser);

            // init the client details
            OauthClientDetails clientDetails = new OauthClientDetails();
            clientDetails.setClientId("web-client");
            clientDetails.setClientSecret("web-client-secret");
            clientDetails.setSecretRequired(true);
            clientDetails.setResourceIds(Sets.newHashSet("project-man"));
            clientDetails.setScope(Sets.newHashSet("call-services"));
            clientDetails.setAuthorizedGrantTypes(Sets.newHashSet("authorization_code", "refresh_token"));
            clientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://localConstants.HOST:8080"));
            clientDetails.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
            clientDetails.setAccessTokenValiditySeconds(60);
            clientDetails.setRefreshTokenValiditySeconds(14400);
            clientDetails.setAutoApprove(false);
            mongoTemplate.save(clientDetails);
            c
           // LOGGER.info("Finish initializing the sample oauth data");
       }*/
    }

}
