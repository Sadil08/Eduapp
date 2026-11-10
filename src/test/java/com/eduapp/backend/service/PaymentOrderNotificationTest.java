package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.eduapp.backend.config.PayHereProperties;
import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** SEC-02: only a correctly signed, amount-matching PayHere notification may grant access. */
class PaymentOrderNotificationTest {

    private static final String MERCHANT = "1211149";
    private static final String SECRET = "s3cret";

    private PaymentOrderRepository orders;
    private StudentBundleAccessRepository access;
    private ExtraAttemptPurchaseRepository extra;
    private PaperRepository papers;
    private PaperBundleRepository bundles;
    private UserRepository users;
    private PaymentOrderService service;
    private User user;
    private PaymentOrder order;

    @BeforeEach
    void setUp() {
        PayHereProperties props = new PayHereProperties();
        props.setMerchantId(MERCHANT);
        props.setMerchantSecret(SECRET);
        orders = mock(PaymentOrderRepository.class);
        access = mock(StudentBundleAccessRepository.class);
        extra = mock(ExtraAttemptPurchaseRepository.class);
        papers = mock(PaperRepository.class);
        bundles = mock(PaperBundleRepository.class);
        users = mock(UserRepository.class);
        service = new PaymentOrderService(orders, props, new PayHereSigner(props), mock(CartService.class), access,
                bundles, papers, mock(CustomBundleRepository.class), mock(CustomBundleService.class), extra, users);

        user = new User();
        user.setId(7L);
        when(users.findById(7L)).thenReturn(Optional.of(user));

        order = new PaymentOrder();
        order.setOrderId("EDU1");
        order.setUser(user);
        order.setKind(PaymentOrder.Kind.EXTRA_ATTEMPTS);
        order.setAmount(new BigDecimal("10.00"));
        order.setCurrency("USD");
        order.setPaperId(3L);
        order.setOriginBundleId(4L);
        order.setAttemptsCount(2);
        when(orders.findByOrderIdForUpdate("EDU1")).thenReturn(Optional.of(order));
        when(papers.findById(3L)).thenReturn(Optional.of(new Paper()));
        when(bundles.findById(4L)).thenReturn(Optional.of(new PaperBundle()));
    }

    private Map<String, String> notification(String amount, String currency, String status) {
        Map<String, String> p = new HashMap<>();
        p.put("merchant_id", MERCHANT);
        p.put("order_id", "EDU1");
        p.put("payment_id", "320032");
        p.put("payhere_amount", amount);
        p.put("payhere_currency", currency);
        p.put("status_code", status);
        p.put("md5sig", PayHereSigner.md5Upper(MERCHANT + "EDU1" + amount + currency + status
                + PayHereSigner.md5Upper(SECRET)));
        return p;
    }

    @Test
    void validSuccess_marksPaidAndGrantsOnce() {
        service.handleNotification(notification("10.00", "USD", "2"));
        assertThat(order.getStatus()).isEqualTo(PaymentOrder.Status.PAID);
        assertThat(order.getGatewayPaymentId()).isEqualTo("320032");
        verify(extra, times(1)).save(any(ExtraAttemptPurchase.class));

        // PayHere retries notifications: a duplicate must not grant again.
        service.handleNotification(notification("10.00", "USD", "2"));
        verify(extra, times(1)).save(any(ExtraAttemptPurchase.class));
    }

    @Test
    void badSignature_isRejectedAndGrantsNothing() {
        Map<String, String> p = notification("10.00", "USD", "2");
        p.put("md5sig", "0".repeat(32));
        assertThatThrownBy(() -> service.handleNotification(p)).isInstanceOf(SecurityException.class);
        assertThat(order.getStatus()).isEqualTo(PaymentOrder.Status.PENDING);
        verify(extra, never()).save(any());
    }

    @Test
    void signedButWrongAmount_failsOrderWithoutGranting() {
        service.handleNotification(notification("1.00", "USD", "2"));
        assertThat(order.getStatus()).isEqualTo(PaymentOrder.Status.FAILED);
        verify(extra, never()).save(any());
    }

    @Test
    void signedButWrongCurrency_failsOrderWithoutGranting() {
        service.handleNotification(notification("10.00", "LKR", "2"));
        assertThat(order.getStatus()).isEqualTo(PaymentOrder.Status.FAILED);
        verify(extra, never()).save(any());
    }

    @Test
    void cancelledAndPending_doNotGrant() {
        service.handleNotification(notification("10.00", "USD", "-1"));
        assertThat(order.getStatus()).isEqualTo(PaymentOrder.Status.CANCELLED);
        service.handleNotification(notification("10.00", "USD", "0"));
        verify(extra, never()).save(any());
    }

    @Test
    void cartOrder_grantsSnapshottedBundlesAtPaidPrice() {
        PaperBundle b = new PaperBundle();
        b.setId(11L);
        b.setPrice(new BigDecimal("10.00"));
        order.setKind(PaymentOrder.Kind.CART);
        order.setBundleIds("11");
        when(bundles.findById(11L)).thenReturn(Optional.of(b));
        when(access.existsByStudentIdAndBundleId(7L, 11L)).thenReturn(false);
        Cart cart = new Cart(user);
        cart.getBundles().add(b);
        CartService cartService = mock(CartService.class);
        when(cartService.getOrCreateCart(7L)).thenReturn(cart);
        PayHereProperties props = new PayHereProperties();
        props.setMerchantId(MERCHANT);
        props.setMerchantSecret(SECRET);
        service = new PaymentOrderService(orders, props, new PayHereSigner(props), cartService, access, bundles,
                papers, mock(CustomBundleRepository.class), mock(CustomBundleService.class), extra, users);

        service.handleNotification(notification("10.00", "USD", "2"));

        verify(access).save(any(StudentBundleAccess.class));
        assertThat(cart.getBundles()).isEmpty();
    }

    @Test
    void unknownOrder_isIgnored() {
        when(orders.findByOrderIdForUpdate("EDU1")).thenReturn(Optional.empty());
        service.handleNotification(notification("10.00", "USD", "2"));
        verify(extra, never()).save(any());
    }
}
