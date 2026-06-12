package com.ecommerce.application.aspect;

import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author AmirHossein ZamanZade
 * @since 3/24/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Aspect
@Order(30)
public class ValidationAspect {

    private final Validator validator;

    @Around(value = "execution(* (@org.springframework.web.bind.annotation.RequestMapping *).*(..))")
    public Object validate(ProceedingJoinPoint pjp) throws Throwable {
        log.debug("ecommerce validation started.");
        Object[] parameters = pjp.getArgs();
        validate(parameters);
        log.debug("ecommerce validation finished.");
        return pjp.proceed();
    }

    private void validate(Object[] parameters) {
        Map<String, List<String>> validationErrors = new HashMap<>();
        if (parameters != null) {
            for (Object parameter : parameters) {
                if (parameter != null) {
                    Set<ConstraintViolation<Object>> violations = validator.validate(parameter);
                    if (!violations.isEmpty()) {
                        Map<String, List<String>> errors = violations.stream()
                                .collect(Collectors.groupingBy(
                                        v -> v.getPropertyPath().toString(),
                                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                                ));
                        validationErrors.putAll(errors);
                    }
                }
            }
        }
        if (!validationErrors.isEmpty()) {
            log.debug("Validation errors: {}", validationErrors);
            throw new EcommerceException(ECOMErrorType.VALIDATION_ERROR, validationErrors);
        }
    }
}
