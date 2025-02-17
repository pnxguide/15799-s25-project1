# Reflection

## Done
- Implement rule for enumerating more filtering conditions (which may let other rules be able to optimize)
  - (a AND b) OR (a AND c) -> a AND (b OR c) [Possibly translate Q19 to HashJoin]
- Multiple-Pass Optimization
  - capybara1 is not benefit much from doing decorrelation first
    - Do decorrelation after the first pass of VolcanoOptimizer
- q2 - join transformation (commutative) and statistic are important (otherwise, expensive cross join)
- 