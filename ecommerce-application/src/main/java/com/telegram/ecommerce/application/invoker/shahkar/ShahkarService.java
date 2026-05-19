package com.telegram.ecommerce.application.invoker.shahkar;

import com.telegram.ecommerce.application.api.exception.EcommerceServiceException;
import com.telegram.ecommerce.application.api.exception.InvalidNationalCodeException;
import com.telegram.ecommerce.application.api.exception.MismatchNationalCodeWithMobileNumberException;
import com.telegram.ecommerce.application.invoker.shahkar.client.ShahkarClient;
import com.telegram.ecommerce.application.invoker.shahkar.dto.ShahkarRequestDto;
import com.telegram.ecommerce.application.invoker.shahkar.dto.ShahkarResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@Service
@RequiredArgsConstructor
public class ShahkarService {

    private final ShahkarClient shahkarClient;

    public void match(String nationalCode, String mobileNumber)
            throws MismatchNationalCodeWithMobileNumberException, InvalidNationalCodeException {
        ShahkarResponseDto responseDto;
        try {
            responseDto = shahkarClient.match(new ShahkarRequestDto(mobileNumber, nationalCode));
        } catch (HttpClientErrorException exception) {
            if (HttpStatus.BAD_REQUEST.equals(exception.getStatusCode())) {
                throw new InvalidNationalCodeException("Invalid national code");
            }
            throw new EcommerceServiceException(exception.getMessage(), exception);
        }

        if (!isMatched(responseDto)) {
            throw new MismatchNationalCodeWithMobileNumberException("National code and mobile number are mismatched");
        }
    }

    private boolean isMatched(ShahkarResponseDto responseDto) {
        if (responseDto == null || responseDto.getResponseBody() == null
                || responseDto.getResponseBody().getData() == null) {
            throw new EcommerceServiceException("The Shahkar provider returned an invalid response.");
        }
        return Boolean.TRUE.equals(responseDto.getResponseBody().getData().getMatched());
    }
}
