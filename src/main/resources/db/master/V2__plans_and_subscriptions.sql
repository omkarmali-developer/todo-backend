CREATE TABLE plans (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  tagline VARCHAR(128) NOT NULL,
  price_cents INT NOT NULL DEFAULT 0,
  billing_period VARCHAR(32) NOT NULL DEFAULT 'forever',
  cta_label VARCHAR(64) NOT NULL,
  highlighted TINYINT(1) NOT NULL DEFAULT 0,
  max_todos INT NULL,
  max_users INT NOT NULL DEFAULT 1,
  features_json JSON NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO plans (
  code, name, tagline, price_cents, billing_period, cta_label, highlighted,
  max_todos, max_users, features_json, active
) VALUES
(
  'starter',
  'Starter',
  'For personal focus',
  0,
  'forever',
  'Start free',
  0,
  50,
  1,
  JSON_ARRAY(
    '1 workspace (your username)',
    'Up to 50 active todos',
    'Basic priorities',
    'Email support in 48h'
  ),
  1
),
(
  'pro',
  'Pro',
  'For builders who ship',
  1200,
  'month',
  'Go Pro',
  1,
  NULL,
  1,
  JSON_ARRAY(
    'Unlimited todos',
    'All priority levels',
    'Complete / incomplete filters',
    'Export-ready history',
    'Priority support'
  ),
  1
),
(
  'team',
  'Team',
  'For small squads',
  2900,
  'month',
  'Start Team',
  0,
  NULL,
  10,
  JSON_ARRAY(
    'Everything in Pro',
    'Up to 10 users in one tenant',
    'Shared task board',
    'Admin role controls',
    'Onboarding help'
  ),
  1
);

ALTER TABLE tenants
  ADD COLUMN plan_id BIGINT NULL,
  ADD COLUMN subscription_status VARCHAR(32) NOT NULL DEFAULT 'active',
  ADD COLUMN current_period_end TIMESTAMP NULL;

UPDATE tenants t
INNER JOIN plans p ON p.code = 'starter'
SET t.plan_id = p.id
WHERE t.plan_id IS NULL;

ALTER TABLE tenants
  MODIFY COLUMN plan_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_tenants_plan FOREIGN KEY (plan_id) REFERENCES plans (id);
