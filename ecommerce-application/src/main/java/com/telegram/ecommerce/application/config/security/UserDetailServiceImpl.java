package com.telegram.ecommerce.application.config.security;

import com.telegram.ecommerce.persistence.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @author AmirHossein ZamanZade
 * @since 3/23/25
 */
@Component
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var optionalUser = appUserRepository.findByUsername(username);
        if (optionalUser.isPresent() && Boolean.TRUE.equals(optionalUser.get().getIsEnabled())
                && Boolean.TRUE.equals(optionalUser.get().getIsRegistered())) {
            return new UserDetailsDto(optionalUser.get());
        }
        throw new BadCredentialsException("Invalid user with username: " + username);
    }
}
