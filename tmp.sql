pragma disable_optimizer;
SELECT "$cor2"."s_name", COUNT(*) AS "numwait"
FROM (SELECT *
FROM ("supplier" INNER JOIN (SELECT *
FROM "lineitem"
WHERE "l_receiptdate" > "l_commitdate") AS "t" ON "supplier"."s_suppkey" = "t"."l_suppkey" INNER JOIN (SELECT *
FROM "orders"
WHERE "o_orderstatus" = 'F') AS "t0" ON "t"."l_orderkey" = "t0"."o_orderkey" INNER JOIN (SELECT *
FROM "nation"
WHERE "n_name" = 'CHINA') AS "t1" ON "supplier"."s_nationkey" = "t1"."n_nationkey") AS "$cor0",
LATERAL (SELECT BOOL_AND(TRUE) AS "$f0"
FROM "lineitem"
WHERE "l_orderkey" = "$cor0"."l_orderkey" AND "l_suppkey" <> "$cor0"."l_suppkey") AS "t4"
WHERE "t4"."$f0" IS NOT NULL) AS "$cor2",
LATERAL (SELECT BOOL_AND(TRUE) AS "$f0"
FROM "lineitem"
WHERE "l_orderkey" = "$cor2"."l_orderkey" AND "l_suppkey" <> "$cor2"."l_suppkey" AND "l_receiptdate" > "l_commitdate") AS "t8"
WHERE "t8"."$f0" IS NULL
GROUP BY "$cor2"."s_name"
ORDER BY 2 DESC, "$cor2"."s_name"
FETCH NEXT 100 ROWS ONLY