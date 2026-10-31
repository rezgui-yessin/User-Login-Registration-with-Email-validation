package com.yessinCoding.auth;

import com.yessinCoding.email.EmailService;
import com.yessinCoding.email.EmailTemplateName;
import com.yessinCoding.entity.PasswordResetToken;
import com.yessinCoding.entity.RefreshToken;
import com.yessinCoding.entity.Token;
import com.yessinCoding.entity.User;
import com.yessinCoding.repository.PasswordResetTokenRepository;
import com.yessinCoding.repository.RefreshTokenRepository;
import com.yessinCoding.repository.RoleRepository;
import com.yessinCoding.repository.TokenRepository;
import com.yessinCoding.repository.UserRepository;
import com.yessinCoding.security.JwtService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthenticationManager authenticationManager;

    @Value("${spring.application.mailing.frontend.activationUrl}")
    private String activationUrl;

    @Value("${spring.application.mailing.frontend.resetPasswordUrl}")
    private String resetPasswordUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        userRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(existing -> {
            throw new IllegalStateException("User with this email already exists");
        });

        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initiated"));

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .failedLoginAttempts(0)
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);
        sendValidationEmail(user);
    }

    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.error("Invalid token: {}", token);
                    return new RuntimeException("Invalid token");
                });

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            log.error("Token expired: {}", token);
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been sent to the same email address");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> {
                    log.error("User not found for token: {}", token);
                    return new UsernameNotFoundException("User not found");
                });

        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
        log.info("Account activated successfully for user: {}", user.getEmail());
    }

    public AuthenticationResponse login(LoginRequest request) {
        var user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        if (!user.isEnabled()) {
            throw new LockedException("Please activate your account before logging in.");
        }

        if (user.isAccountLocked()) {
            if (user.getLockTime() != null && user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES).isAfter(LocalDateTime.now())) {
                throw new LockedException("Your account is temporarily locked due to too many failed login attempts.");
            }
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
        }

        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            var authenticatedUser = (User) auth.getPrincipal();
            authenticatedUser.setFailedLoginAttempts(0);
            authenticatedUser.setLockTime(null);
            authenticatedUser.setAccountLocked(false);
            userRepository.save(authenticatedUser);

            var claims = new HashMap<String, Object>();
            claims.put("fullName", authenticatedUser.fullName());
            var jwtToken = jwtService.generateToken(claims, authenticatedUser);
            var refreshToken = generateAndSaveRefreshToken(authenticatedUser);

            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getJwtExpirationInMs())
                    .build();
        } catch (BadCredentialsException e) {
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);

            if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
                userRepository.save(user);
                throw new LockedException("Too many failed login attempts. Your account has been temporarily locked for 15 minutes.");
            }

            userRepository.save(user);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public AuthenticationResponse refreshToken(String refreshTokenRequest) {
        var refreshToken = refreshTokenRepository.findByToken(refreshTokenRequest)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        var user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);

        var newAccessToken = jwtService.generateToken(new HashMap<>(java.util.Map.of("fullName", user.fullName())), user);
        String newRefreshToken = generateAndSaveRefreshToken(user);

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpirationInMs())
                .build();
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    public void forgotPassword(String email) throws MessagingException {
        var user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        passwordResetTokenRepository.deleteByUser(user);

        var token = UUID.randomUUID().toString();
        var resetToken = PasswordResetToken.builder()
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendEmail(
                user.getEmail(),
                user.fullName(),
                EmailTemplateName.RESET_PASSWORD,
                resetPasswordUrl,
                token,
                "Password reset"
        );
    }

    public void resetPassword(String token, String newPassword) {
        var resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Reset token expired");
        }

        var user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    private String generateAndSaveRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        var refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .user(user)
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);

        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        tokenRepository.save(token);
        return generatedToken;
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.fullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }
}
