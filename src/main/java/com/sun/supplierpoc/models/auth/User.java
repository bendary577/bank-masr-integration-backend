package com.sun.supplierpoc.models.auth;

import com.sun.supplierpoc.models.roles.UserAccess;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * Created by jeebb on 11/8/14.
 */
@Document(collection = "user")
public class User implements UserDetails  {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    public String name;
    private String username ;
    private String password;
    private String accountId;
    private ArrayList<UserAccess> userAccesses;
    private Date creationDate;
    private Date updateDate;

    private boolean deleted;
    private Collection<? extends GrantedAuthority> authorities;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private boolean credentialsNonExpired;

    public User() {
    }

    public User(String name, String accountId,
            String username, String password, Collection<? extends GrantedAuthority> authorities, Boolean enabled,
                Boolean accountNonExpired, Boolean accountNonLocked, boolean credentialsNonExpired) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.creationDate = new Date();
        this.accountId = accountId;
        this.deleted = false;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public User(String name, String accountId,
                String id, String username, String password, Collection<? extends GrantedAuthority> authorities,
                Boolean enabled, Boolean accountNonExpired, Boolean accountNonLocked, boolean credentialsNonExpired) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.creationDate = new Date();
        this.accountId = accountId;
        this.deleted = false;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }

    public ArrayList<UserAccess> getUserAccesses() {
        return userAccesses;
    }

    public void setUserAccesses(ArrayList<UserAccess> userAccesses) {
        this.userAccesses = userAccesses;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }

}
