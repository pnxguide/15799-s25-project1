pragma disable_optimizer;
.timer on
EXPLAIN ANALYZE 

SELECT "nation"."n_name" AS "nation", EXTRACT(YEAR FROM "orders"."o_orderdate") AS "o_year", COALESCE(SUM("lineitem"."l_extendedprice" * (1 - "lineitem"."l_discount") - "partsupp"."ps_supplycost" * "lineitem"."l_quantity"), 0) AS "sum_profit"
FROM 
    (SELECT * FROM "part" WHERE "p_name" LIKE '%forest%') AS "t"
        INNER JOIN "lineitem" ON "t"."p_partkey" = "lineitem"."l_partkey"
        INNER JOIN "supplier" ON "supplier"."s_suppkey" = "lineitem"."l_suppkey"
        INNER JOIN "partsupp" ON "lineitem"."l_suppkey" = "partsupp"."ps_suppkey" AND 
            "lineitem"."l_partkey" = "partsupp"."ps_partkey" 
        INNER JOIN "orders" ON "lineitem"."l_orderkey" = "orders"."o_orderkey" 
        INNER JOIN "nation" ON "supplier"."s_nationkey" = "nation"."n_nationkey" 
GROUP BY "nation"."n_name", EXTRACT(YEAR FROM "orders"."o_orderdate")
ORDER BY "nation"."n_name", 2 DESC