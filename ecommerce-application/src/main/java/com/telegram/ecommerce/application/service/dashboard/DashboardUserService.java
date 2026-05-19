package com.telegram.ecommerce.application.service.dashboard;

import com.telegram.ecommerce.application.api.dto.dashboard.*;
import com.telegram.ecommerce.application.api.dto.dashboard.enumeration.Role;
import com.telegram.ecommerce.application.api.exception.*;
import com.telegram.ecommerce.application.config.properties.dto.SignupProperties;
import com.telegram.ecommerce.application.config.security.UserDetailsDto;
import com.telegram.ecommerce.application.invoker.shahkar.ShahkarService;
import com.telegram.ecommerce.application.service.jwt.JwtService;
import com.telegram.ecommerce.application.service.ticket.SignupTicketService;
import com.telegram.ecommerce.application.service.ticket.dto.TicketGenerateRequestDto;
import com.telegram.ecommerce.persistence.cache.SignupCacheService;
import com.telegram.ecommerce.persistence.cache.dto.SignupData;
import com.telegram.ecommerce.persistence.entity.AppUser;
import com.telegram.ecommerce.persistence.entity.enumeration.UserRole;
import com.telegram.ecommerce.persistence.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@Service
@RequiredArgsConstructor
public class DashboardUserService {

    private static final String TICKET_LAST_SENT_DATE_CACHE_KEY_PREFIX = "lastSentTicketDateForSignup_";
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SignupTicketService signupTicketService;
    private final SignupCacheService signupCacheService;
    private final SignupProperties signupProperties;
    private final ShahkarService shahkarService;

    public SendSignupTicketResponseDto sendSignupTicket(SendSignupTicketRequestDto requestDto)
            throws TicketValidationBlockException {
        signupTicketService.sendTicket(
                convertToTicketGenerateRequestDto(requestDto.getNationalCode(), requestDto.getMobileNumber()));
        return new SendSignupTicketResponseDto(signupProperties.getTicket().getTimeToLive().toSeconds());
    }

    public SendResetCredentialTicketResponseDto sendResetCredentialTicket(
            SendResetCredentialTicketRequestDto requestDto) throws TicketValidationBlockException,
            UserNotFoundException {
        AppUser appUser = appUserRepository.findByUsername(requestDto.getNationalCode())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        signupTicketService.sendTicket(convertToTicketGenerateRequestDto(requestDto.getNationalCode(),
                appUser.getMobile()));
        return new SendResetCredentialTicketResponseDto(signupProperties.getTicket().getTimeToLive().toSeconds(),
                appUser.getMobile());
    }

    public SignupTicketValidationResponseDto validateCredentialTicket(SignupTicketValidationRequestDto requestDto)
            throws TicketValidationBlockException, InvalidTicketException {
        String nationalCode = requestDto.getNationalCode();
        String mobileNumber = requestDto.getMobileNumber();
        signupTicketService.validateTicket(getCacheKey(nationalCode, mobileNumber), requestDto.getTicket(),
                mobileNumber);
        Optional<AppUser> user = appUserRepository.findByUsername(requestDto.getNationalCode());
        UUID uuid = UUID.randomUUID();
        String signupToken = uuid.toString();
        signupCacheService.addSignupData(signupToken, new SignupData(nationalCode, mobileNumber),
                signupProperties.getTokenTtl().toSeconds());
        return new SignupTicketValidationResponseDto(signupToken, user.isEmpty());
    }

    public void signup(SignupRequestDto requestDto) throws UserHasAlreadyExistException, InvalidSignupTokenException,
            MismatchNationalCodeWithMobileNumberException, InvalidNationalCodeException {
        SignupData signupData = signupCacheService.getSignupData(requestDto.getSignupToken());
        if (signupData == null) {
            throw new InvalidSignupTokenException("Invalid signup token");
        }
        Optional<AppUser> user = appUserRepository.findByUsername(signupData.getNationalCode());
        if (user.isPresent()) {
            throw new UserHasAlreadyExistException("User already exists");
        }
        shahkarService.match(signupData.getNationalCode(), signupData.getMobile());
        AppUser appUserEntity = new AppUser();
        appUserEntity.setName(requestDto.getName());
        appUserEntity.setUsername(signupData.getNationalCode());
        appUserEntity.setMobile(signupData.getMobile());
        appUserEntity.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        appUserEntity.setRole(UserRole.ROLE_APP_USER);
        appUserEntity.setIsEnabled(true);
        appUserRepository.save(appUserEntity);
        signupCacheService.deleteSignupData(requestDto.getSignupToken());
    }

    public void resetCredential(ResetCredentialRequestDto requestDto) throws InvalidSignupTokenException,
            UserNotFoundException {
        SignupData signupData = signupCacheService.getSignupData(requestDto.getSignupToken());
        if (signupData == null) {
            throw new InvalidSignupTokenException("Invalid signup token");
        }
        AppUser appUser = appUserRepository.findByUsername(signupData.getNationalCode())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        appUser.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        appUserRepository.save(appUser);
        signupCacheService.deleteSignupData(requestDto.getSignupToken());
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));
        UserDetailsDto userDetails = (UserDetailsDto) authenticate.getPrincipal();
        return new LoginResponseDto(jwtService.generateToken(userDetails),
                Role.valueOf(userDetails.getAuthorities().getFirst().getAuthority()));
    }

    private String getCacheKey(String nationalCode, String mobileNumber) {
        return String.join("+", nationalCode, mobileNumber);
    }

    private TicketGenerateRequestDto convertToTicketGenerateRequestDto(String nationalCode, String mobileNumber) {
        TicketGenerateRequestDto ticketGenerateRequestDto = new TicketGenerateRequestDto();
        ticketGenerateRequestDto.setMobileNumber(mobileNumber);
        ticketGenerateRequestDto.setCacheKey(getCacheKey(nationalCode, mobileNumber));
        ticketGenerateRequestDto.setLastSentCacheKey(getLastSentCacheKey(mobileNumber));
        ticketGenerateRequestDto.setSmsTemplateId(signupProperties.getTicket().getTemplateId());
        ticketGenerateRequestDto.setTicketTimeToLive((int) signupProperties.getTicket().getTimeToLive().toSeconds());
        return ticketGenerateRequestDto;
    }

    private String getLastSentCacheKey(String mobileNumber) {
        return TICKET_LAST_SENT_DATE_CACHE_KEY_PREFIX + mobileNumber;
    }
}
