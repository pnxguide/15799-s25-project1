pragma disable_optimizer;
.timer on
EXPLAIN ANALYZE
SELECT SUM("l_extendedprice" * "l_discount") AS "revenue"
FROM "lineitem"
WHERE "l_shipdate" >= DATE '1995-01-01' AND "l_shipdate" < (DATE '1995-01-01' + INTERVAL '1' YEAR) AND "l_discount" >= 0.05 - 0.01 AND "l_discount" <= 0.05 + 0.01 AND "l_quantity" < 24.00