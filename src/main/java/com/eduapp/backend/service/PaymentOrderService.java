package com.eduapp.backend.service;

import com.eduapp.backend.config.PayHereProperties;
import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates payment orders (price computed here, never taken from the client) and fulfils them when
 * PayHere's signed notification reports success.
 */
@Service
public class PaymentOrderService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentOrderService.class);

    public static final BigDecimal EXTRA_ATTEMPT_PRICE = new BigDecimal("5.00");
    public static final int MAX_EXTRA_ATTEMPTS_PER_ORDER = 10;

    private final PaymentOrderRepository orders;
    private final PayHereProperties props;
    private final PayHereSigner signer;
    private final CartService cartService;
    private final StudentBundleAccessRepository access;
    private final PaperBundleRepository bundles;
    private final PaperRepository papers;
    private final CustomBundleRepository customBundles;
    private final CustomBundleService customBundleService;
    private final ExtraAttemptPurchaseRepository extraAttempts;
    private final UserRepository users;

    public PaymentOrderService(PaymentOrderRepository orders, PayHereProperties props, PayHereSigner signer,
            CartService cartService, StudentBundleAccessRepository access, PaperBundleRepository bundles,
            PaperRepository papers, CustomBundleRepository customBundles,
            CustomBundleService customBundleService, ExtraAttemptPurchaseRepository extraAttempts,
            UserRepository users) {
        this.orders = orders;
        this.props = props;
        this.signer = signer;
        this.cartService = cartService;
        this.access = access;
        this.bundles = bundles;
        this.papers = papers;
        this.customBundles = customBundles;
        this.customBundleService = customBundleService;
        this.extraAttempts = extraAttempts;
        this.users = users;
    }

    // ---- order creation -------------------------------------------------------------------

    @Transactional
    public PaymentOrder createCartOrder(User user) {
        Cart cart = cartService.getOrCreateCart(user.getId());
        List<PaperBundle> toBuy = cart.getBundles().stream()
                .filter(b -> !access.existsByStudentIdAndBundleId(user.getId(), b.getId()))
                .collect(Collectors.toList());
        BigDecimal total = toBuy.stream().map(PaymentOrderService::priceOf).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (toBuy.isEmpty() || total.signum() <= 0) {
            throw new IllegalArgumentException("Nothing to pay for in the cart");
        }
        PaymentOrder o = newOrder(user, PaymentOrder.Kind.CART, total);
        o.setBundleIds(toBuy.stream().map(b -> String.valueOf(b.getId())).collect(Collectors.joining(",")));
        return orders.save(o);
    }

    @Transactional
    public PaymentOrder createCustomBundleOrder(User user, Long customBundleId) {
        CustomBundle cb = customBundles.findById(customBundleId)
                .orElseThrow(() -> new IllegalArgumentException("Custom bundle not found"));
        if (!cb.getCreator().getId().equals(user.getId())) {
            throw new SecurityException("Not your bundle");
        }
        if (cb.getStatus() != CustomBundleStatus.CREATED) {
            throw new IllegalStateException("Bundle is already purchased");
        }
        if (cb.getPapers().isEmpty()) {
            throw new IllegalStateException("Cannot purchase an empty bundle");
        }
        BigDecimal total = customBundleService.getPricePerPaper().multiply(new BigDecimal(cb.getPapers().size()));
        if (total.signum() <= 0) {
            throw new IllegalStateException("Bundle has no price configured");
        }
        PaymentOrder o = newOrder(user, PaymentOrder.Kind.CUSTOM_BUNDLE, total);
        o.setCustomBundleId(cb.getId());
        return orders.save(o);
    }

    @Transactional
    public PaymentOrder createExtraAttemptsOrder(User user, Long paperId, Long bundleId, int count) {
        if (count < 1 || count > MAX_EXTRA_ATTEMPTS_PER_ORDER) {
            throw new IllegalArgumentException("attemptsCount must be between 1 and " + MAX_EXTRA_ATTEMPTS_PER_ORDER);
        }
        if (bundleId == null) {
            throw new IllegalArgumentException("bundleId is required");
        }
        Paper paper = papers.findById(paperId).orElseThrow(() -> new IllegalArgumentException("Paper not found"));
        PaperBundle bundle = bundles.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found"));
        if (!access.existsByStudentIdAndBundleId(user.getId(), bundleId)) {
            throw new SecurityException("Bundle not purchased");
        }
        if (paper.getBundles().stream().noneMatch(b -> b.getId().equals(bundle.getId()))) {
            throw new IllegalArgumentException("Paper is not in that bundle");
        }
        PaymentOrder o = newOrder(user, PaymentOrder.Kind.EXTRA_ATTEMPTS,
                EXTRA_ATTEMPT_PRICE.multiply(new BigDecimal(count)));
        o.setPaperId(paperId);
        o.setOriginBundleId(bundleId);
        o.setAttemptsCount(count);
        return orders.save(o);
    }

    private PaymentOrder newOrder(User user, PaymentOrder.Kind kind, BigDecimal amount) {
        PaymentOrder o = new PaymentOrder();
        o.setOrderId("EDU-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase());
        o.setUser(user);
        o.setKind(kind);
        o.setAmount(amount.setScale(2, java.math.RoundingMode.HALF_UP));
        o.setCurrency(props.getCurrency());
        return o;
    }

    private static BigDecimal priceOf(PaperBundle b) {
        return b.getPrice() == null ? BigDecimal.ZERO : b.getPrice();
    }

    // ---- checkout form ----------------------------------------------------------------------

    /** Fields the browser must POST to {@link PayHereProperties#checkoutUrl()}. */
    public Map<String, String> checkoutFields(PaymentOrder o) {
        User u = o.getUser();
        Map<String, String> f = new LinkedHashMap<>();
        f.put("merchant_id", props.getMerchantId());
        f.put("return_url", props.getReturnUrl() + "?order=" + o.getOrderId());
        f.put("cancel_url", props.getCancelUrl() + "?order=" + o.getOrderId());
        f.put("notify_url", props.getNotifyUrl());
        f.put("order_id", o.getOrderId());
        f.put("items", describe(o));
        f.put("currency", o.getCurrency());
        f.put("amount", PayHereSigner.formatAmount(o.getAmount()));
        f.put("first_name", u.getUsername() != null ? u.getUsername() : "Student");
        f.put("last_name", "-");
        f.put("email", u.getEmail());
        f.put("phone", "0000000000");
        f.put("address", "N/A");
        f.put("city", "N/A");
        f.put("country", u.getCountry() != null && !u.getCountry().isBlank() ? u.getCountry() : "N/A");
        f.put("hash", signer.checkoutHash(o.getOrderId(), o.getAmount(), o.getCurrency()));
        return f;
    }

    private String describe(PaymentOrder o) {
        switch (o.getKind()) {
            case CART: return "Paper bundles";
            case CUSTOM_BUNDLE: return "Custom bundle";
            default: return o.getAttemptsCount() + " extra attempt(s)";
        }
    }

    @Transactional(readOnly = true)
    public Optional<PaymentOrder> findForUser(String orderId, Long userId) {
        return orders.findByOrderIdAndUserId(orderId, userId);
    }

    // ---- notification -----------------------------------------------------------------------

    /**
     * Handles PayHere's server-to-server notification. Throws SecurityException on a bad signature
     * (caller answers 400); everything else is answered 200 so PayHere does not retry forever.
     */
    @Transactional
    public void handleNotification(Map<String, String> p) {
        String orderId = p.get("order_id");
        String statusCode = p.get("status_code");
        if (!signer.verifyNotification(p.get("merchant_id"), orderId, p.get("payhere_amount"),
                p.get("payhere_currency"), statusCode, p.get("md5sig"))
                || !props.getMerchantId().equals(p.get("merchant_id"))) {
            logger.warn("Rejected PayHere notification with bad signature for order {}", orderId);
            throw new SecurityException("Invalid signature");
        }

        PaymentOrder o = orders.findByOrderIdForUpdate(orderId).orElse(null);
        if (o == null) {
            logger.warn("PayHere notification for unknown order {}", orderId);
            return;
        }

        switch (statusCode) {
            case "2":
                onSuccess(o, p);
                break;
            case "-1":
                if (o.getStatus() == PaymentOrder.Status.PENDING) o.setStatus(PaymentOrder.Status.CANCELLED);
                break;
            case "-2":
                if (o.getStatus() == PaymentOrder.Status.PENDING) o.setStatus(PaymentOrder.Status.FAILED);
                break;
            case "-3":
                if (o.getStatus() == PaymentOrder.Status.PAID) {
                    o.setStatus(PaymentOrder.Status.CHARGEDBACK);
                    logger.error("CHARGEBACK on order {} (user {}): access is NOT revoked automatically; review manually",
                            o.getOrderId(), o.getUser().getId());
                }
                break;
            default:
                break; // 0 = pending: nothing to do yet
        }
        orders.save(o);
    }

    private void onSuccess(PaymentOrder o, Map<String, String> p) {
        if (o.getStatus() == PaymentOrder.Status.PAID || o.getStatus() == PaymentOrder.Status.CHARGEDBACK) {
            return; // duplicate notification: already fulfilled
        }
        boolean amountOk;
        try {
            amountOk = new BigDecimal(p.get("payhere_amount")).compareTo(o.getAmount()) == 0;
        } catch (NumberFormatException e) {
            amountOk = false;
        }
        if (!amountOk || !o.getCurrency().equals(p.get("payhere_currency"))) {
            o.setStatus(PaymentOrder.Status.FAILED);
            logger.error("Amount/currency mismatch on order {}: expected {} {}, got {} {}", o.getOrderId(),
                    o.getAmount(), o.getCurrency(), p.get("payhere_amount"), p.get("payhere_currency"));
            return;
        }
        o.setStatus(PaymentOrder.Status.PAID);
        o.setPaidAt(LocalDateTime.now());
        o.setGatewayPaymentId(p.get("payment_id"));
        fulfil(o);
        o.setFulfilledAt(LocalDateTime.now());
        logger.info("Order {} paid and fulfilled ({} {})", o.getOrderId(), o.getAmount(), o.getCurrency());
    }

    private void fulfil(PaymentOrder o) {
        User user = users.findById(o.getUser().getId()).orElseThrow();
        switch (o.getKind()) {
            case CART: {
                Cart cart = cartService.getOrCreateCart(user.getId());
                for (String idStr : o.getBundleIds().split(",")) {
                    Long id = Long.valueOf(idStr.trim());
                    PaperBundle b = bundles.findById(id).orElse(null);
                    if (b != null && !access.existsByStudentIdAndBundleId(user.getId(), id)) {
                        StudentBundleAccess a = new StudentBundleAccess(user, b, o.getOrderId());
                        a.setPricePaid(priceOf(b));
                        access.save(a);
                    }
                    cart.getBundles().removeIf(cb -> cb.getId().equals(id));
                }
                cartService.save(cart);
                break;
            }
            case CUSTOM_BUNDLE: {
                CustomBundle cb = customBundles.findById(o.getCustomBundleId()).orElseThrow();
                cb.setTotalPrice(o.getAmount());
                cb.setStatus(CustomBundleStatus.PURCHASED);
                cb.setPurchasedAt(LocalDateTime.now());
                customBundles.save(cb);
                break;
            }
            case EXTRA_ATTEMPTS: {
                Paper paper = papers.findById(o.getPaperId()).orElseThrow();
                PaperBundle bundle = bundles.findById(o.getOriginBundleId()).orElseThrow();
                extraAttempts.save(new ExtraAttemptPurchase(user, paper, bundle, o.getAttemptsCount(),
                        o.getAmount(), o.getOrderId()));
                break;
            }
        }
    }
}
