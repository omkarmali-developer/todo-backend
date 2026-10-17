-- Temporary: all plans are free with no billing period end.
UPDATE plans
SET price_cents = 0,
    billing_period = 'forever',
    cta_label = 'Start free';

UPDATE tenants
SET current_period_end = NULL,
    subscription_status = 'active'
WHERE subscription_status = 'expired'
   OR current_period_end IS NOT NULL;
