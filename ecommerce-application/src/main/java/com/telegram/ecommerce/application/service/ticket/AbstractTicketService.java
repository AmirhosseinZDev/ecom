package com.telegram.ecommerce.application.service.ticket;


import com.telegram.ecommerce.application.api.exception.InvalidTicketException;
import com.telegram.ecommerce.application.api.exception.SendTicketTimeLimitNotExceededException;
import com.telegram.ecommerce.application.api.exception.TicketValidationBlockException;
import com.telegram.ecommerce.application.invoker.sms.SmsService;
import com.telegram.ecommerce.application.service.ticket.dto.TicketGenerateRequestDto;
import com.telegram.ecommerce.application.util.DateUtil;
import com.telegram.ecommerce.persistence.cache.AbstractTicketCacheService;
import com.telegram.ecommerce.persistence.cache.BlockedMobileNumbersCacheService;
import com.telegram.ecommerce.persistence.cache.dto.TicketInfoCacheDto;
import com.telegram.ecommerce.persistence.entity.AppUser;
import com.telegram.ecommerce.persistence.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Date;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@RequiredArgsConstructor
public abstract class AbstractTicketService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DateUtil dateUtil;
    private final SmsService smsService;
    private final AbstractTicketCacheService ticketCacheService;
    private final BlockedMobileNumbersCacheService blockedMobileNumbersCacheService;
    private final AppUserRepository appUserRepository;

    protected abstract Duration getBlockDuration();

    protected abstract Integer getMaxFailureCount();

    protected abstract int getTicketLength(TicketGenerateRequestDto ticketGenerateRequestDto);

    public void validateTicket(String cacheKey, String ticket, String mobileNumber) throws
            TicketValidationBlockException, InvalidTicketException {
        TicketInfoCacheDto result = ticketCacheService.getTicketInfoDto(cacheKey);
        if (result == null || result.getTicket() == null || !result.getTicket().equals(ticket)) {
            handleFailureCount(result, cacheKey, mobileNumber);
            throw new InvalidTicketException("Ticket is not valid");
        }
    }

    public void deleteTicket(String cacheKey, Long userId) {
        ticketCacheService.deleteTicket(cacheKey, userId);
    }

    public void deleteLastSentDate(String cacheKey) {
        ticketCacheService.deleteLastSentTicketDate(cacheKey);
    }

    protected String prepareTicket(TicketGenerateRequestDto ticketGenerateRequestDto) throws
            SendTicketTimeLimitNotExceededException, TicketValidationBlockException {
        if (ticketCacheService.getTicketInfoDto(ticketGenerateRequestDto.getCacheKey()) != null) {
            throw new SendTicketTimeLimitNotExceededException(
                    "Request is not permitted, because previous ticket is not expired yet.");
        }
        if (blockedMobileNumbersCacheService.isMobileNumberExistInBlockedMobileNumbers(ticketGenerateRequestDto
                .getMobileNumber())) {
            throw new TicketValidationBlockException("This mobile number has been blocked.");
        }
        Date currentDate = new Date();
        long ticketTimeToLiveInMillis = (long) ticketGenerateRequestDto.getTicketTimeToLive() * 1000;
        Date lastSentTicketDate = ticketCacheService.getLastSentTicketDate(
                ticketGenerateRequestDto.getLastSentCacheKey());
        if (lastSentTicketDate != null && dateUtil.getDateDiffInMillis(lastSentTicketDate,
                currentDate) < ticketTimeToLiveInMillis) {
            throw new SendTicketTimeLimitNotExceededException(
                    "Send ticket time limit for this mobile number is not exceeded yet.");
        }
        Date expireDate = new Date(currentDate.getTime() + ticketTimeToLiveInMillis);
        String ticket = generateTicket(getTicketLength(ticketGenerateRequestDto));
        Long userId = appUserRepository.findByMobile(ticketGenerateRequestDto.getMobileNumber())
                .map(AppUser::getId).orElse(null);
        ticketCacheService.addTicket(ticketGenerateRequestDto.getCacheKey(), userId,
                new TicketInfoCacheDto(ticket), expireDate);
        ticketCacheService.setLastSentTicketDate(ticketGenerateRequestDto.getLastSentCacheKey(), currentDate,
                expireDate);
        return ticket;
    }

    protected void sendTicketMessage(TicketGenerateRequestDto ticketGenerateRequestDto, String ticket) {
        try {
            smsService.sendOTP(ticketGenerateRequestDto.getSmsTemplateId(), ticketGenerateRequestDto.getMobileNumber(),
                    ticket, ticketGenerateRequestDto.getTicketTimeToLive() / 60);
        } catch (Throwable exception) {
            ticketCacheService.deleteTicket(ticketGenerateRequestDto.getCacheKey(), null);
            ticketCacheService.deleteLastSentTicketDate(ticketGenerateRequestDto.getLastSentCacheKey());
            throw exception;
        }
    }

    private String generateTicket(int ticketLength) {
        int bound = (int) Math.pow(10, ticketLength);
        return String.format("%0" + ticketLength + "d", SECURE_RANDOM.nextInt(bound));
    }

    private void handleFailureCount(TicketInfoCacheDto result, String ticketCacheKey, String mobileNumber)
            throws TicketValidationBlockException {
        if (result != null) {
            int failureCount = result.incrementAndGetFailureCount();
            if (failureCount >= getMaxFailureCount()) {
                deleteTicket(ticketCacheKey, null);
                blockedMobileNumbersCacheService.addMobileNumber(mobileNumber, getBlockDuration().toSeconds());
                throw new TicketValidationBlockException("This mobile number is blocked.");
            }
            ticketCacheService.updateTicketInfoDto(ticketCacheKey, result);
        }
    }
}
