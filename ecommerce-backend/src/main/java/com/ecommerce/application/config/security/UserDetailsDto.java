package com.ecommerce.application.config.security;

import com.ecommerce.persistence.entity.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * @author AmirHossein ZamanZade
 * @since 3/23/25
 */
public class UserDetailsDto implements UserDetails {

    @Getter
    private final Long id;
    private final String username;
    private final String password;

    @Getter
    private final Boolean enabled;
    private final List<GrantedAuthority> authorities;

    public UserDetailsDto(AppUser appUser) {
        this.id = appUser.getId();
        this.username = appUser.getUsername();
        this.password = appUser.getPassword();
        this.authorities = List.of(new SimpleGrantedAuthority(appUser.getRole().name()));
        this.enabled = appUser.getIsEnabled();
    }


    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
