# Reflection

## Done
- capybara1
  - Require FILTER_REDUCE_EXPRESSIONS
  - Not benefit much from doing decorrelation first; do decorrelation after the first pass of VolcanoOptimizer
    - Multiple-Pass Optimization
- capybara3
  - Require AGGREGATE_EXPAND_DISTINCT_AGGREGATES and AGGREGATE_REDUCE_FUNCTIONS
  - Filter should be push down before join
- q3
  - Should SORTLIMIT (TOP) before PROJECT
- q4
  - Merge Aggregate and Project
- q5
  - 
- q9 
  - join transformation ruins the hash join transformation -- perhaps, due to cost model
    - Rewrite NestedLoopJoin to make its cost so expensive that it is not a good choice
    - Better when doing join enumeration
  - Projection causes longer time
- q19
  - Implement rule for enumerating more filtering conditions (which may let other rules be able to optimize)
    - (a AND b) OR (a AND c) -> a AND (b OR c) [Possibly translate Q19 to HashJoin]

## Tried
- Plug in unique columns and sorted columns -- seems like nothing happened

## 
- Calcite's cost model is somehow not quite good; many bushy trees are just bad
