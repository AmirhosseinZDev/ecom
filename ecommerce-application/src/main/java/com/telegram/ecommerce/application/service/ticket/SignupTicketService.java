package com.telegram.ecommerce.application.service.ticket;


import com.telegram.ecommerce.application.api.exception.InvalidTicketException;
import com.telegram.ecommerce.application.api.exception.SendTicketTimeLimitNotExceededException;
import com.telegram.ecommerce.application.api.exception.TicketValidationBlockException;
import com.telegram.ecommerce.application.config.properties.dto.SignupProperties;
import com.telegram.ecommerce.application.invoker.sms.SmsService;
import com.telegram.ecommerce.application.service.ticket.dto.TicketGenerateRequestDto;
import com.telegram.ecommerce.application.util.DateUtil;
import com.telegram.ecommerce.persistence.cache.BlockedMobileNumbersCacheService;
import com.telegram.ecommerce.persistence.cache.TicketCacheService;
import com.telegram.ecommerce.persistence.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@Service
public class SignupTicketService extends AbstractTicketService {

    private final SignupProperties signupProperties;

    public SignupTicketService(DateUtil dateUtil,
            SmsService smsService, SignupProperties signupProperties, TicketCacheService ticketCacheService,
            BlockedMobileNumbersCacheService blockedMobileNumbersCacheService, AppUserRepository appUserRepository) {
        super(dateUtil, smsService, ticketCacheService, blockedMobileNumbersCacheService,
                appUserRepository);
        this.signupProperties = signupProperties;
    }

    public void sendTicket(TicketGenerateRequestDto ticketGenerateRequestDto) throws
            SendTicketTimeLimitNotExceededException, TicketValidationBlockException {
        sendTicketMessage(ticketGenerateRequestDto, prepareTicket(ticketGenerateRequestDto));
    }

    @Override
    public void validateTicket(String cacheKey, String ticket, String mobileNumber) throws InvalidTicketException,
            TicketValidationBlockException {
        super.validateTicket(cacheKey, ticket, mobileNumber);
        deleteTicket(cacheKey, null);
    }

    @Override
    protected Duration getBlockDuration() {
        return signupProperties.getTicket().getBlockDuration();
    }

    @Override
    protected Integer getMaxFailureCount() {
        return signupProperties.getTicket().getMaxFailureCount();
    }

    @Override
    protected int getTicketLength(TicketGenerateRequestDto ticketGenerateRequestDto) {
        return signupProperties.getTicket().getLength();
    }
}
