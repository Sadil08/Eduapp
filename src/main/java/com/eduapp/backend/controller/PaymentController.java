package com.eduapp.backend.controller;

import com.eduapp.backend.config.PayHereProperties;
import com.eduapp.backend.model.PaymentOrder;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.AuthenticatedUser;
import com.eduapp.backend.service.PaymentOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Payments via PayHere. Flow: the client asks for an order, receives the signed form fields, POSTs
 * them to PayHere, and PayHere calls {@code /payhere/notify}. Only that signed call grants access.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentOrderService paymentOrderService;
    private final PayHereProperties props;
    private final UserRepository userRepository;

    public PaymentController(PaymentOrderService paymentOrderService, PayHereProperties props,
            UserRepository userRepository) {
        this.paymentOrderService = paymentOrderService;
        this.props = props;
        this.userRepository = userRepository;
    }

    public static class CreateOrderRequest {
        @NotNull
        private PaymentOrder.Kind kind;
        private Long customBundleId;
        private Long paperId;
        private Long bundleId;
        @Min(1)
        @Max(PaymentOrderService.MAX_EXTRA_ATTEMPTS_PER_ORDER)
        private Integer attemptsCount;

        public PaymentOrder.Kind getKind() { return kind; }
        public void setKind(PaymentOrder.Kind kind) { this.kind = kind; }
        public Long getCustomBundleId() { return customBundleId; }
        public void setCustomBundleId(Long customBundleId) { this.customBundleId = customBundleId; }
        public Long getPaperId() { return paperId; }
        public void setPaperId(Long paperId) { this.paperId = paperId; }
        public Long getBundleId() { return bundleId; }
        public void setBundleId(Long bundleId) { this.bundleId = bundleId; }
        public Integer getAttemptsCount() { return attemptsCount; }
        public void setAttemptsCount(Integer attemptsCount) { this.attemptsCount = attemptsCount; }
    }

    public record CheckoutResponse(String orderId, String checkoutUrl, BigDecimal amount, String currency,
            Map<String, String> fields) {
    }

    public record OrderStatusResponse(String orderId, String status, String kind, BigDecimal amount,
            String currency) {
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!props.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Payments are not available right now"));
        }
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        try {
            PaymentOrder order;
            switch (req.getKind()) {
                case CART:
                    order = paymentOrderService.createCartOrder(user);
                    break;
                case CUSTOM_BUNDLE:
                    if (req.getCustomBundleId() == null) {
                        return ResponseEntity.badRequest().body(Map.of("error", "customBundleId is required"));
                    }
                    order = paymentOrderService.createCustomBundleOrder(user, req.getCustomBundleId());
                    break;
                default:
                    if (req.getPaperId() == null || req.getAttemptsCount() == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "paperId, bundleId and attemptsCount are required"));
                    }
                    order = paymentOrderService.createExtraAttemptsOrder(user, req.getPaperId(), req.getBundleId(),
                            req.getAttemptsCount());
            }
            return ResponseEntity.ok(new CheckoutResponse(order.getOrderId(), props.checkoutUrl(), order.getAmount(),
                    order.getCurrency(), paymentOrderService.checkoutFields(order)));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not allowed"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderStatusResponse> getOrder(@PathVariable String orderId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return paymentOrderService.findForUser(orderId, principal.getUserId())
                .map(o -> ResponseEntity.ok(new OrderStatusResponse(o.getOrderId(), o.getStatus().name(),
                        o.getKind().name(), o.getAmount(), o.getCurrency())))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Called by PayHere's servers (form-encoded). Public; authenticated by the md5sig signature. */
    @PostMapping(value = "/payhere/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> notifyPayment(@RequestParam Map<String, String> params) {
        try {
            paymentOrderService.handleNotification(params);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // 500 makes PayHere retry the notification, which is what we want for transient errors.
            logger.error("PayHere notification processing failed for order {}", params.get("order_id"), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
