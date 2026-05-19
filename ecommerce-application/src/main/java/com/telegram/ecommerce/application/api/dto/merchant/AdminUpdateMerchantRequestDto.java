package com.telegram.ecommerce.application.api.dto.merchant;

import com.tosan.validation.constraints.ValidBase64;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateMerchantRequestDto {

    @NotEmpty
    private String title;

    private String description;

    @NotNull
    private Boolean isActive;

    @NotNull
    private Long ownerUserId;

    private String storeUrl;

    private String botToken;

    @ValidBase64(maxBytesSize = 1024)
    private String base64EncodedLogo;
}
