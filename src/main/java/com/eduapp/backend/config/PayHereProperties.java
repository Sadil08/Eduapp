package com.eduapp.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PayHere settings. The defaults are placeholders so the app starts locally; {@link
 * com.eduapp.backend.service.PayHereSigner} refuses to sign while the secret is a placeholder, and
 * the prod profile fails at startup (see PaymentConfigCheck).
 */
@ConfigurationProperties(prefix = "payhere")
public class PayHereProperties {

    public static final String PLACEHOLDER = "CHANGE_ME";

    private String merchantId = PLACEHOLDER;
    private String merchantSecret = PLACEHOLDER;
    private boolean sandbox = true;
    private String currency = "USD";
    /** Public URL PayHere calls with the payment result. Must be reachable from the internet. */
    private String notifyUrl = "http://localhost:8080/api/payments/payhere/notify";
    /** Browser return pages, built from the frontend base URL. */
    private String returnUrl = "http://localhost:3000/payment/return";
    private String cancelUrl = "http://localhost:3000/payment/cancelled";

    public boolean isConfigured() {
        return !PLACEHOLDER.equals(merchantId) && !PLACEHOLDER.equals(merchantSecret)
                && merchantId != null && !merchantId.isBlank()
                && merchantSecret != null && !merchantSecret.isBlank();
    }

    public String checkoutUrl() {
        return sandbox ? "https://sandbox.payhere.lk/pay/checkout" : "https://www.payhere.lk/pay/checkout";
    }

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getMerchantSecret() { return merchantSecret; }
    public void setMerchantSecret(String merchantSecret) { this.merchantSecret = merchantSecret; }
    public boolean isSandbox() { return sandbox; }
    public void setSandbox(boolean sandbox) { this.sandbox = sandbox; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    public String getCancelUrl() { return cancelUrl; }
    public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
}
