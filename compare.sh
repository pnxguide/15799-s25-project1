# !/usr/bin/env bash

for i in {1..4}; do
    echo 'pragma disable_optimizer;' > tmp.sql;
    echo '.mode csv' >> tmp.sql;
    cat output/capybara${i}_optimized.sql >> tmp.sql;
    ./duckdb ./stat.db < tmp.sql > output/capybara${i}_sql_optimized_result;

    echo 'pragma enable_optimizer;' > tmp.sql;
    echo '.mode csv' >> tmp.sql;
    cat output/capybara${i}.sql >> tmp.sql;
    ./duckdb ./stat.db < tmp.sql > output/capybara${i}_sql_unoptimized_result;
done

for i in {1..22}; do
    echo 'pragma disable_optimizer;' > tmp.sql;
    echo '.mode csv' >> tmp.sql;
    cat output/q${i}_optimized.sql >> tmp.sql;
    ./duckdb ./stat.db < tmp.sql > output/q${i}_sql_optimized_result;

    echo 'pragma enable_optimizer;' > tmp.sql;
    echo '.mode csv' >> tmp.sql;
    cat output/q${i}.sql >> tmp.sql;
    ./duckdb ./stat.db < tmp.sql > output/q${i}_sql_unoptimized_result;
done

# echo 'pragma disable_optimizer;' > tmp.sql;
# echo '.mode csv' >> tmp.sql;
# cat output/q21_optimized.sql >> tmp.sql;
# ./duckdb ./stat.db < tmp.sql > output/q21_sql_optimized_result;

# echo 'pragma enable_optimizer;' > tmp.sql;
# echo '.mode csv' >> tmp.sql;
# cat output/q21.sql >> tmp.sql;
# ./duckdb ./stat.db < tmp.sql > output/q21_sql_unoptimized_result;