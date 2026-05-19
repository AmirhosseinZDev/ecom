package com.telegram.ecommerce.application.invoker.shahkar;

import com.telegram.ecommerce.application.api.exception.EcommerceServiceException;
import com.telegram.ecommerce.application.api.exception.InvalidNationalCodeException;
import com.telegram.ecommerce.application.api.exception.MismatchNationalCodeWithMobileNumberException;
import com.telegram.ecommerce.application.invoker.shahkar.client.ShahkarClient;
import com.telegram.ecommerce.application.invoker.shahkar.dto.ShahkarRequestDto;
import com.telegram.ecommerce.application.invoker.shahkar.dto.ShahkarResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShahkarService_matchUTest {

    @Mock
    private ShahkarClient shahkarClient;

    @InjectMocks
    private ShahkarService shahkarService;

    @Test
    void matched_response_finishes_without_exception() throws Exception {
        when(shahkarClient.match(org.mockito.ArgumentMatchers.any())).thenReturn(response(true));

        shahkarService.match("0021111112", "09121111118");

        ArgumentCaptor<ShahkarRequestDto> captor = ArgumentCaptor.forClass(ShahkarRequestDto.class);
        verify(shahkarClient).match(captor.capture());
        assertEquals("09121111118", captor.getValue().getMobile());
        assertEquals("0021111112", captor.getValue().getNationalCode());
    }

    @Test
    void unmatched_response_throws_mismatch_exception() {
        when(shahkarClient.match(org.mockito.ArgumentMatchers.any())).thenReturn(response(false));

        assertThrows(MismatchNationalCodeWithMobileNumberException.class,
                () -> shahkarService.match("0021111112", "09121111118"));
    }

    @Test
    void bad_request_from_provider_throws_invalid_national_code_exception() {
        when(shahkarClient.match(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(InvalidNationalCodeException.class,
                () -> shahkarService.match("0021111112", "09121111118"));
    }

    @Test
    void invalid_provider_response_throws_service_exception() {
        when(shahkarClient.match(org.mockito.ArgumentMatchers.any())).thenReturn(new ShahkarResponseDto());

        assertThrows(EcommerceServiceException.class,
                () -> shahkarService.match("0021111112", "09121111118"));
    }

    private ShahkarResponseDto response(boolean matched) {
        ShahkarResponseDto responseDto = new ShahkarResponseDto();
        ShahkarResponseDto.ResponseBody responseBody = new ShahkarResponseDto.ResponseBody();
        ShahkarResponseDto.Data data = new ShahkarResponseDto.Data();
        data.setMatched(matched);
        responseBody.setData(data);
        responseDto.setResponseBody(responseBody);
        return responseDto;
    }
}
