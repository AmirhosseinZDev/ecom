package com.telegram.ecommerce.application.api.dto.user;

import com.telegram.ecommerce.application.api.dto.user.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 3/24/25
 */
@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private Role role;
}
