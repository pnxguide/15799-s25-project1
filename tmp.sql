pragma disable_optimizer;
SELECT SUBSTRING("t"."c_phone", 1, 2) AS "cntrycode", COUNT(*) AS "numcust", COALESCE(SUM("t"."c_acctbal"), 0) AS "totacctbal"
FROM (SELECT *
FROM "customer"
WHERE CAST(SUBSTRING("c_phone", 1, 2) AS VARCHAR(2)) IN ('11', '17', '20', '25', '30', '31', '33')) AS "t"
INNER JOIN (SELECT CAST(CAST(CASE WHEN COUNT(*) = 0 THEN NULL ELSE COALESCE(SUM("c_acctbal"), 0) END AS DECIMAL(15, 2)) / COUNT(*) AS DECIMAL(15, 2)) AS "EXPR$0"
FROM "customer"
WHERE "c_acctbal" > 0.00 AND CAST(SUBSTRING("c_phone", 1, 2) AS VARCHAR(2)) IN ('11', '17', '20', '25', '30', '31', '33')) AS "t2" ON "t"."c_acctbal" > "t2"."EXPR$0"
LEFT JOIN (SELECT "o_custkey", BOOL_AND(TRUE) AS "$f1"
FROM "orders"
GROUP BY "o_custkey") AS "t4" ON "t"."c_custkey" = "t4"."o_custkey"
WHERE "t4"."$f1" IS NULL
GROUP BY SUBSTRING("t"."c_phone", 1, 2)
ORDER BY 1