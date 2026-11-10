package com.eduapp.backend.service;

import com.eduapp.backend.config.PayHereProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PayHere signatures (https://support.payhere.lk/api-&-mobile-sdk/checkout-api):
 * <pre>
 * hash   = UPPER(md5(merchant_id + order_id + amount + currency + UPPER(md5(merchant_secret))))
 * md5sig = UPPER(md5(merchant_id + order_id + payhere_amount + payhere_currency + status_code
 *                    + UPPER(md5(merchant_secret))))
 * </pre>
 * MD5 is PayHere's choice, not ours; it is used here only to match their protocol.
 */
@Component
public class PayHereSigner {

    private final PayHereProperties props;

    public PayHereSigner(PayHereProperties props) {
        this.props = props;
    }

    /** Amount exactly as PayHere expects it: two decimals, dot, no thousands separator. */
    public static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public String checkoutHash(String orderId, BigDecimal amount, String currency) {
        requireConfigured();
        return md5Upper(props.getMerchantId() + orderId + formatAmount(amount) + currency
                + md5Upper(props.getMerchantSecret()));
    }

    /**
     * Verifies the md5sig on a notification. {@code payhereAmount} must be the raw string PayHere
     * sent (it is part of the signed text), not a re-formatted number.
     */
    public boolean verifyNotification(String merchantId, String orderId, String payhereAmount,
            String payhereCurrency, String statusCode, String md5sig) {
        if (!props.isConfigured() || md5sig == null || merchantId == null || orderId == null
                || payhereAmount == null || payhereCurrency == null || statusCode == null) {
            return false;
        }
        String expected = md5Upper(merchantId + orderId + payhereAmount + payhereCurrency + statusCode
                + md5Upper(props.getMerchantSecret()));
        // Constant-time comparison so the signature cannot be probed byte by byte.
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                md5sig.trim().toUpperCase().getBytes(StandardCharsets.UTF_8));
    }

    private void requireConfigured() {
        if (!props.isConfigured()) {
            throw new IllegalStateException("PayHere is not configured (set PAYHERE_MERCHANT_ID and PAYHERE_MERCHANT_SECRET)");
        }
    }

    static String md5Upper(String s) {
        try {
            byte[] d = MessageDigest.getInstance("MD5").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : d) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
