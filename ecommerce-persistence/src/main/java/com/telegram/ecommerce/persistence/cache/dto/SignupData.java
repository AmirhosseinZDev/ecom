package com.telegram.ecommerce.persistence.cache.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Hadi Zahedian
 * @since 7/22/2023
 */
@Getter
@AllArgsConstructor
public class SignupData implements Serializable {

    @Serial
    private static final long serialVersionUID = -8268149114780824900L;

    private String nationalCode;

    private String mobile;
}
