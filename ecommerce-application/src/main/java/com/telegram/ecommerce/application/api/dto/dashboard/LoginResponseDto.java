package com.telegram.ecommerce.application.api.dto.dashboard;

import com.telegram.ecommerce.application.api.dto.dashboard.enumeration.Role;
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
