package com.telegram.ecommerce.application.service.ticket;

import com.telegram.ecommerce.application.api.exception.InvalidTicketException;
import com.telegram.ecommerce.application.api.exception.SendTicketTimeLimitNotExceededException;
import com.telegram.ecommerce.application.api.exception.TicketValidationBlockException;
import com.telegram.ecommerce.application.config.properties.dto.LoginProperties;
import com.telegram.ecommerce.application.invoker.sms.SmsService;
import com.telegram.ecommerce.application.service.ticket.dto.TicketGenerateRequestDto;
import com.telegram.ecommerce.application.util.DateUtil;
import com.telegram.ecommerce.persistence.cache.BlockedMobileNumbersCacheService;
import com.telegram.ecommerce.persistence.cache.LoginTicketCacheService;
import com.telegram.ecommerce.persistence.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author AmirHossein ZamanZade
 * @since 5/29/26
 */
@Service
public class LoginTicketService extends AbstractTicketService {

    private final LoginProperties loginProperties;

    public LoginTicketService(DateUtil dateUtil, SmsService smsService, LoginProperties loginProperties,
            LoginTicketCacheService ticketCacheService,
            BlockedMobileNumbersCacheService blockedMobileNumbersCacheService,
            AppUserRepository appUserRepository) {
        super(dateUtil, smsService, ticketCacheService, blockedMobileNumbersCacheService, appUserRepository);
        this.loginProperties = loginProperties;
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
        return loginProperties.getTicket().getBlockDuration();
    }

    @Override
    protected Integer getMaxFailureCount() {
        return loginProperties.getTicket().getMaxFailureCount();
    }

    @Override
    protected int getTicketLength(TicketGenerateRequestDto ticketGenerateRequestDto) {
        return loginProperties.getTicket().getLength();
    }
}
