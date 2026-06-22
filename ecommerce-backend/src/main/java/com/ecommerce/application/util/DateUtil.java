package com.ecommerce.application.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author AmirHossein ZamanZade
 * @since 12/25/2022
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DateUtil {

    public long getDateDiffInMillis(Date from, Date to) {
        return Math.abs(to.getTime() - from.getTime());
    }
}
