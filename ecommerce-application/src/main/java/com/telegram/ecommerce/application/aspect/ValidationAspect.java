package com.telegram.ecommerce.application.aspect;

import com.telegram.ecommerce.application.api.exception.ValidationException;
import com.tosan.validation.util.ValidationViolationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    static {
        System.setProperty("org.apache.el.parser.SKIP_IDENTIFIER_CHECK", "true");
    }

    private final com.tosan.validation.Validation validator;

    @Around(value = "execution(* (@org.springframework.web.bind.annotation.RequestMapping *).*(..))")
    public Object validate(ProceedingJoinPoint pjp) throws Throwable {
        log.debug("ecommerce validation started.");
        Object[] parameters = pjp.getArgs();
        validate(parameters);
        log.debug("ecommerce validation finished.");
        return pjp.proceed();
    }

    private void validate(Object[] parameters) {
        Map<String, List<ValidationViolationInfo>> validationViolationInfo = new HashMap<>();
        StringBuilder message = new StringBuilder();
        if (parameters != null) {
            for (Object parameter : parameters) {
                if (parameter != null) {
                    validationViolationInfo.putAll(validator.validate(parameter));
                }
            }
        }
        if (MapUtils.isNotEmpty(validationViolationInfo)) {
            message.append("{").append(validationViolationInfo).append("}");
            if (log.isDebugEnabled()) {
                log.debug(message.toString());
            }
            throw new ValidationException(message.toString(), validationViolationInfo);
        }
    }

}
