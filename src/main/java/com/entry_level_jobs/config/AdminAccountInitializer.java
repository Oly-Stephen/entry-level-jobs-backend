package com.entry_level_jobs.config;

import com.entry_level_jobs.model.UserAccount;
import com.entry_level_jobs.model.UserRole;
import com.entry_level_jobs.repository.UserAccountRepository;
import com.entry_level_jobs.service.EmailValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAccountInitializer {
    private final SecurityProperties securityProperties;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailValidationService emailValidationService;

    @PostConstruct
    public void ensureAdminAccount() {
        SecurityProperties.AdminProperties admin = securityProperties.getAdmin();
        if (admin == null || !StringUtils.hasText(admin.getEmail()) || !StringUtils.hasText(admin.getPassword())) {
            log.warn("Admin credentials not configured; skipping admin bootstrap");
            return;
        }

        emailValidationService.validate(admin.getEmail());
        String normalizedEmail = admin.getEmail().trim().toLowerCase();
        UserAccount account = userAccountRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> UserAccount.builder()
                        .email(normalizedEmail)
                        .displayName(StringUtils.hasText(admin.getDisplayName()) ? admin.getDisplayName()
                                : "Administrator")
                        .passwordHash(passwordEncoder.encode(admin.getPassword()))
                        .role(UserRole.ADMIN)
                        .emailVerified(true)
                        .active(true)
                        .build());

        if (account.getId() == null) {
            userAccountRepository.save(account);
            log.info("Bootstrap admin account created for {}", normalizedEmail);
            return;
        }

        boolean needsUpdate = false;
        if (account.getRole() != UserRole.ADMIN) {
            account.setRole(UserRole.ADMIN);
            needsUpdate = true;
        }
        if (!passwordEncoder.matches(admin.getPassword(), account.getPasswordHash())) {
            account.setPasswordHash(passwordEncoder.encode(admin.getPassword()));
            needsUpdate = true;
        }
        if (!account.isActive()) {
            account.setActive(true);
            needsUpdate = true;
        }
        if (needsUpdate) {
            userAccountRepository.save(account);
            log.info("Bootstrap admin account updated for {}", normalizedEmail);
        }
    }
}
