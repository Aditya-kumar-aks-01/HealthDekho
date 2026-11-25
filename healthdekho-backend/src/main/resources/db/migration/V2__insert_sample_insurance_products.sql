
INSERT INTO insurance_product (
    product_code,
    policy_name,
    policy_type,
    term_months,
    premium_frequency,
    premium_amount,
    sum_insured,
    min_entry_age,
    max_entry_age,
    active
)
VALUES
(
    'HD-LIFE-001',
    'HealthDekho Life Secure',
    'LIFE',
    240,
    'Monthly',
    1500.00,
    5000000.00,
    18,
    60,
    TRUE
),
(
    'HD-HEALTH-001',
    'HealthDekho Health Plus',
    'HEALTH',
    12,
    'Yearly',
    12000.00,
    1000000.00,
    18,
    65,
    TRUE
),
(
    'HD-TERM-001',
    'HealthDekho Term Protect',
    'TERM',
    360,
    'Monthly',
    850.00,
    10000000.00,
    18,
    55,
    TRUE
),
(
    'HD-RETIRE-001',
    'HealthDekho Retirement Plan',
    'RETIREMENT',
    240,
    'Yearly',
    25000.00,
    3000000.00,
    25,
    60,
    TRUE
);
