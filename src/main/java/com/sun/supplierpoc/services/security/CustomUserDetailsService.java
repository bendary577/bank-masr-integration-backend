package com.sun.supplierpoc.services.security;


import com.sun.supplierpoc.models.auth.MongoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Created by jeebb on 11/8/14.
 */
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        MongoUser user =
                mongoTemplate.findOne(query, MongoUser.class);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("Username %s not found", username));
        }

       // String[] roles = new String[user.getAuthorities().size()];
        return new MongoUser(user.getId(),user.getUsername(),user.getPassword(),user.getAuthorities(),user.isEnabled(),user.isAccountNonExpired(),user.isAccountNonLocked(),user.isCredentialsNonExpired());
    }
}
