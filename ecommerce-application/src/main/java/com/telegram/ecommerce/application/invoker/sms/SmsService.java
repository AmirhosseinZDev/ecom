package com.telegram.ecommerce.application.invoker.sms;

import com.telegram.ecommerce.application.api.exception.EcommerceServiceException;
import com.telegram.ecommerce.application.invoker.sms.client.SmsClient;
import com.telegram.ecommerce.application.invoker.sms.dto.SmsRequestDto;
import com.telegram.ecommerce.application.invoker.sms.dto.SmsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@Service
@RequiredArgsConstructor
public class SmsService {

    private static final String OTP = "OTP";
    private static final String TIME = "TIME";
    private final SmsClient smsClient;

    public void sendOTP(Integer templateId, String mobileNumber, String otp, Integer otpTtl) {
        SmsResponseDto responseDto = smsClient.sendMessage(new SmsRequestDto(mobileNumber, templateId,
                List.of(new SmsRequestDto.Parameter(OTP, otp),
                        new SmsRequestDto.Parameter(TIME, otpTtl.toString()))));

        if (responseDto == null || responseDto.getStatus() == null || responseDto.getStatus() != 1) {
            throw new EcommerceServiceException("The SMS provider returned an unsuccessful response.");
        }
    }
}
