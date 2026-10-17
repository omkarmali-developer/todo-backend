-- Populate current_period_end for tenants created before the app set it on insert.
UPDATE tenants t
INNER JOIN plans p ON p.id = t.plan_id
SET t.current_period_end = CASE
  WHEN LOWER(p.billing_period) IN ('month', 'monthly')
    THEN TIMESTAMPADD(MONTH, 1, t.created_at)
  WHEN LOWER(p.billing_period) IN ('year', 'yearly', 'annual')
    THEN TIMESTAMPADD(YEAR, 1, t.created_at)
  ELSE t.current_period_end
END
WHERE t.current_period_end IS NULL
  AND LOWER(p.billing_period) NOT IN ('forever');
