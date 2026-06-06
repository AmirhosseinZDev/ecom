package com.ecommerce.application.service.ticket;

import com.ecommerce.application.api.exception.InvalidTicketException;
import com.ecommerce.application.api.exception.SendTicketTimeLimitNotExceededException;
import com.ecommerce.application.api.exception.TicketValidationBlockException;
import com.ecommerce.application.config.properties.dto.LoginProperties;
import com.ecommerce.application.invoker.sms.SmsService;
import com.ecommerce.application.service.ticket.dto.TicketGenerateRequestDto;
import com.ecommerce.application.util.DateUtil;
import com.ecommerce.persistence.cache.BlockedMobileNumbersCacheService;
import com.ecommerce.persistence.cache.LoginTicketCacheService;
import com.ecommerce.persistence.repository.AppUserRepository;
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
