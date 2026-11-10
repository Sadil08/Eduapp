package com.eduapp.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(PayHereProperties.class)
public class PaymentConfig {

    private static final Logger logger = LoggerFactory.getLogger(PaymentConfig.class);

    private final PayHereProperties props;
    private final Environment env;

    public PaymentConfig(PayHereProperties props, Environment env) {
        this.props = props;
        this.env = env;
    }

    @PostConstruct
    void check() {
        if (props.isConfigured()) {
            if (!props.isSandbox() && !"USD".equals(props.getCurrency())) {
                logger.warn("PayHere live mode with currency {}", props.getCurrency());
            }
            return;
        }
        if (env.acceptsProfiles(Profiles.of("prod"))) {
            throw new IllegalStateException(
                    "PAYHERE_MERCHANT_ID / PAYHERE_MERCHANT_SECRET must be set in the prod profile");
        }
        logger.warn("PayHere is not configured: checkout will fail until PAYHERE_MERCHANT_ID and "
                + "PAYHERE_MERCHANT_SECRET are set.");
    }
}
