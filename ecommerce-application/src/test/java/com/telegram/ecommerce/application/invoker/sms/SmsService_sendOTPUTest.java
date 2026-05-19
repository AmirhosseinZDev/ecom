package com.telegram.ecommerce.application.invoker.sms;

import com.telegram.ecommerce.application.api.exception.EcommerceServiceException;
import com.telegram.ecommerce.application.invoker.sms.client.SmsClient;
import com.telegram.ecommerce.application.invoker.sms.dto.SmsRequestDto;
import com.telegram.ecommerce.application.invoker.sms.dto.SmsResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsService_sendOTPUTest {

    @Mock
    private SmsClient smsClient;

    @InjectMocks
    private SmsService smsService;

    @Test
    void successful_provider_response_sends_expected_payload() {
        SmsResponseDto responseDto = new SmsResponseDto();
        responseDto.setStatus(1);
        when(smsClient.sendMessage(org.mockito.ArgumentMatchers.any())).thenReturn(responseDto);

        smsService.sendOTP(10, "09123456789", "1234", 2);

        ArgumentCaptor<SmsRequestDto> captor = ArgumentCaptor.forClass(SmsRequestDto.class);
        verify(smsClient).sendMessage(captor.capture());
        SmsRequestDto requestDto = captor.getValue();
        assertEquals("09123456789", requestDto.getMobile());
        assertEquals(10, requestDto.getTemplateId());
        assertEquals("OTP", requestDto.getParameters().getFirst().getName());
        assertEquals("1234", requestDto.getParameters().getFirst().getValue());
        assertEquals("TIME", requestDto.getParameters().get(1).getName());
        assertEquals("2", requestDto.getParameters().get(1).getValue());
    }

    @Test
    void unsuccessful_provider_response_throws_service_exception() {
        SmsResponseDto responseDto = new SmsResponseDto();
        responseDto.setStatus(0);
        when(smsClient.sendMessage(org.mockito.ArgumentMatchers.any())).thenReturn(responseDto);

        assertThrows(EcommerceServiceException.class,
                () -> smsService.sendOTP(10, "09123456789", "1234", 2));
    }

    @Test
    void null_provider_response_throws_service_exception() {
        when(smsClient.sendMessage(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        assertThrows(EcommerceServiceException.class,
                () -> smsService.sendOTP(10, "09123456789", "1234", 2));
    }
}
