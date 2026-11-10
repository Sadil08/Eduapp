package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import com.eduapp.backend.config.PayHereProperties;
import org.junit.jupiter.api.Test;

class PayHereSignerTest {

    private PayHereSigner signer(String id, String secret) {
        PayHereProperties p = new PayHereProperties();
        p.setMerchantId(id);
        p.setMerchantSecret(secret);
        return new PayHereSigner(p);
    }

    @Test
    void checkoutHash_matchesIndependentlyComputedValue() {
        // md5 values computed outside the code (python hashlib) from PayHere's documented formula.
        assertThat(signer("1211149", "s3cret").checkoutHash("EDU1", new BigDecimal("10"), "USD"))
                .isEqualTo("A00EECB6D8178481E3E51632987BFB56");
    }

    @Test
    void amountIsFormattedWithTwoDecimalsAndNoSeparators() {
        assertThat(PayHereSigner.formatAmount(new BigDecimal("1000"))).isEqualTo("1000.00");
        assertThat(PayHereSigner.formatAmount(new BigDecimal("1234.5"))).isEqualTo("1234.50");
    }

    @Test
    void notification_validSignatureAccepted_tamperedRejected() {
        PayHereSigner s = signer("1211149", "s3cret");
        String sig = "48DE7FA8447327FE651E37D93CA78087";
        assertThat(s.verifyNotification("1211149", "EDU1", "10.00", "USD", "2", sig)).isTrue();
        assertThat(s.verifyNotification("1211149", "EDU1", "10.00", "USD", "2", sig.toLowerCase())).isTrue();
        assertThat(s.verifyNotification("1211149", "EDU1", "1.00", "USD", "2", sig)).isFalse();
        assertThat(s.verifyNotification("1211149", "EDU1", "10.00", "USD", "-2", sig)).isFalse();
        assertThat(s.verifyNotification("1211149", "EDU1", "10.00", "USD", "2", null)).isFalse();
    }

    @Test
    void placeholderConfig_neverVerifiesAndNeverSigns() {
        PayHereSigner s = signer("CHANGE_ME", "CHANGE_ME");
        // An attacker who guesses the placeholder secret must not be able to forge a payment.
        assertThat(s.verifyNotification("CHANGE_ME", "EDU1", "10.00", "USD", "2",
                PayHereSigner.md5Upper("CHANGE_ME" + "EDU1" + "10.00" + "USD" + "2"
                        + PayHereSigner.md5Upper("CHANGE_ME")))).isFalse();
        assertThatThrownBy(() -> s.checkoutHash("EDU1", BigDecimal.TEN, "USD"))
                .isInstanceOf(IllegalStateException.class);
    }
}
