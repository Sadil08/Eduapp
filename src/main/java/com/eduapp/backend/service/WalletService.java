package com.eduapp.backend.service;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final AppSettingRepository appSettingRepository;

    public WalletService(UserRepository userRepository, WalletTransactionRepository walletTransactionRepository,
            AppSettingRepository appSettingRepository) {
        this.userRepository = userRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.appSettingRepository = appSettingRepository;
    }

    public BigDecimal getBalance(Long userId) {
        return userRepository.findById(userId)
                .map(User::getWalletBalance)
                .orElse(BigDecimal.ZERO);
    }

    public List<WalletTransaction> getTransactions(Long userId) {
        return walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void topUp(Long userId, BigDecimal amount) {
        logger.info("Topping up wallet for user {}: {}", userId, amount);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);

        WalletTransaction transaction = new WalletTransaction(user, amount, WalletTransactionType.TOP_UP,
                "Wallet top up");
        walletTransactionRepository.save(transaction);

        // Ensure user has a referral code if they don't have one
        if (user.getReferralCode() == null) {
            user.setReferralCode(generateUniqueReferralCode());
            userRepository.save(user);
        }
    }

    @Transactional
    public void debit(User user, BigDecimal amount, String description) {
        logger.info("Debiting wallet for user {}: {}", user.getId(), amount);
        if (user.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        userRepository.save(user);

        WalletTransaction transaction = new WalletTransaction(user, amount.negate(), WalletTransactionType.DEBIT,
                description);
        walletTransactionRepository.save(transaction);

        // Process referral bonus if applicable
        processReferralBonus(user, amount);
    }

    private void processReferralBonus(User user, BigDecimal purchaseAmount) {
        User referrer = user.getReferredBy();
        if (referrer == null)
            return;

        BigDecimal percentage = getReferralPercentage();
        BigDecimal bonus = purchaseAmount.multiply(percentage).divide(new BigDecimal("100"));

        if (bonus.compareTo(BigDecimal.ZERO) > 0) {
            logger.info("Crediting referral bonus to user {}: {} (purchased by {})", referrer.getId(), bonus,
                    user.getId());
            referrer.setWalletBalance(referrer.getWalletBalance().add(bonus));
            userRepository.save(referrer);

            WalletTransaction transaction = new WalletTransaction(referrer, bonus,
                    WalletTransactionType.REFERRAL_CREDIT,
                    "Referral bonus from " + user.getUsername());
            walletTransactionRepository.save(transaction);
        }
    }

    public BigDecimal getReferralPercentage() {
        return appSettingRepository.findById("referral_percentage")
                .map(setting -> new BigDecimal(setting.getValue()))
                .orElse(new BigDecimal("0.5"));
    }

    @Transactional
    public void setReferralPercentage(BigDecimal percentage) {
        AppSetting setting = appSettingRepository.findById("referral_percentage")
                .orElse(new AppSetting("referral_percentage", "0.5"));
        setting.setValue(percentage.toString());
        appSettingRepository.save(setting);
    }

    private String generateUniqueReferralCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.findByReferralCode(code).isPresent());
        return code;
    }
}
