pragma disable_optimizer;
.timer on
EXPLAIN ANALYZE
SELECT SUM("t0"."$f0") AS "revenue"
FROM (SELECT "l_partkey", "l_extendedprice" * (1 - "l_discount") AS "$f0", "l_quantity" >= 9.00 AS "EXPR$1", "l_quantity" <= CAST(9 + 10 AS DECIMAL(15, 2)) AS "EXPR$2", "l_quantity" >= 18.00 AS "EXPR$3", "l_quantity" <= CAST(18 + 10 AS DECIMAL(15, 2)) AS "EXPR$4", "l_quantity" <= CAST(22 + 10 AS DECIMAL(15, 2)) AS "EXPR$5", "l_quantity" >= 22.00 AS "EXPR$6"
FROM "lineitem"
WHERE "l_shipinstruct" = 'DELIVER IN PERSON' AND ("l_shipmode" = 'AIR' OR "l_shipmode" = 'AIR REG')) AS "t0"
INNER JOIN (SELECT "p_partkey", "p_brand" = 'Brand#52' AS "EXPR$0", CAST("p_container" AS VARCHAR(7)) IN ('SM CASE', 'SM PACK') OR CAST("p_container" AS VARCHAR(6)) IN ('SM BOX', 'SM PKG') AS "EXPR$1", "p_size" <= 5 AS "EXPR$2", "p_size" <= 10 AS "EXPR$3", "p_brand" = 'Brand#42' AS "EXPR$4", CAST("p_container" AS VARCHAR(7)) IN ('MED BAG', 'MED BOX', 'MED PKG') OR "p_container" = 'MED PACK' AS "EXPR$5", "p_brand" = 'Brand#23' AS "EXPR$6", "p_size" <= 15 AS "EXPR$7", CAST("p_container" AS VARCHAR(7)) IN ('LG CASE', 'LG PACK') OR CAST("p_container" AS VARCHAR(6)) IN ('LG BOX', 'LG PKG') AS "EXPR$8"
FROM "part"
WHERE "p_size" >= 1) AS "t2" ON "t0"."l_partkey" = "t2"."p_partkey" AND ("t2"."EXPR$0" AND "t0"."EXPR$1" AND "t0"."EXPR$2" AND "t2"."EXPR$1" AND "t2"."EXPR$2" OR "t0"."EXPR$3" AND "t0"."EXPR$4" AND "t2"."EXPR$3" AND "t2"."EXPR$4" AND "t2"."EXPR$5" OR "t2"."EXPR$6" AND "t2"."EXPR$7" AND "t0"."EXPR$5" AND "t0"."EXPR$6" AND "t2"."EXPR$8")