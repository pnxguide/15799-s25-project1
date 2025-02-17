pragma disable_optimizer;
SELECT "t4"."l_orderkey", "t4"."revenue", "t4"."o_orderdate", "t4"."o_shippriority"
FROM (SELECT "t"."l_orderkey", "t1"."o_orderdate", "t1"."o_shippriority", COALESCE(SUM("t"."l_extendedprice" * (1 - "t"."l_discount")), 0) AS "revenue"
FROM (SELECT *
FROM "lineitem"
WHERE "l_shipdate" > DATE '1995-03-20') AS "t"
INNER JOIN ((SELECT *
FROM "customer"
WHERE "c_mktsegment" = 'FURNITURE') AS "t0" INNER JOIN (SELECT *
FROM "orders"
WHERE "o_orderdate" < DATE '1995-03-20') AS "t1" ON "t0"."c_custkey" = "t1"."o_custkey") ON "t"."l_orderkey" = "t1"."o_orderkey"
GROUP BY "t"."l_orderkey", "t1"."o_orderdate", "t1"."o_shippriority"
ORDER BY 4 DESC, "t1"."o_orderdate"
FETCH NEXT 10 ROWS ONLY) AS "t4"