-- N-R3: a verified gateway payment may grant a given bundle at most once. Backstop behind the
-- app-level idempotency check in PurchaseService.checkout (existsByPaymentId).
--
-- NULL payment_id rows (admin/legacy grants) are distinct under a Postgres unique index, so
-- they are unaffected — only real paid grants are constrained.
--
-- If pre-existing duplicate (payment_id, bundle_id) rows exist in prod, this index creation
-- will fail; de-duplicate first (keep the earliest id per pair) before applying.
CREATE UNIQUE INDEX IF NOT EXISTS uq_sba_payment_bundle
    ON student_bundle_accesses (payment_id, bundle_id);
