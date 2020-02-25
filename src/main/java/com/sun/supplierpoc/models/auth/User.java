package com.sun.supplierpoc.models.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Created by jeebb on 11/8/14.
 */
@Document(collection = "users")
public class User implements UserDetails  {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;

    private String username ;

    private String password;

    private Collection<? extends GrantedAuthority> authorities;
    private Boolean enabled;

    private Boolean accountNonExpired;

    private Boolean accountNonLocked;

    private boolean credentialsNonExpired;


    public User() {
    }

    public User(String username, String password, Collection<? extends GrantedAuthority> authorities, Boolean enabled, Boolean accountNonExpired, Boolean accountNonLocked, boolean credentialsNonExpired) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public User(String id, String username, String password, Collection<? extends GrantedAuthority> authorities, Boolean enabled, Boolean accountNonExpired, Boolean accountNonLocked, boolean credentialsNonExpired) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }


    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }



    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}
