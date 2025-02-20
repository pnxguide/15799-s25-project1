# Reflection

## Done
- **HepPlanner for Projection Pushdown**
  - My assumption is that the cost model does not consider row size and projection pushdown typically causes more operators (with may not be a good preference for the cost model)
  - Since it is (personally) so difficult to change the cost model in Calcite, I decided to do projection pushdown in RBO
  - Since it is RBO, I iteratively apply the rules to ensure that the projection gets pushed down as much as possible
    - 100 iterations are my current choice (it may be too much)
- **New Rule: FilterDistributiveRule**
  - q19 has a weird OR filtering condition in which it leads to NestedLoopJoin (which is expensive)
    - However, it is pretty easy to see that all the conditions in OR share the join condition
    - Applying the distributive property of boolean algebra, we can extract the join condition
      - (a AND b) OR (a AND c) -> a AND (b OR c) [Possibly translate Q19 to HashJoin using 'a' as a condition]
- **If it is unnecessary to use NestedLoopJoin, avoid it**
  - This might not be a good for every case. However, it is better if for some cases that HashJoin can be used with a lot of improvement.
    - Rewrite NestedLoopJoin to make its cost so expensive that it is not a good choice
    - This also requires me to rewrite EnumerableJoinRule to match with the new NestedLoopJoin op
- **Rule Selection Rationals**
  - capybara1
    - Require FILTER_REDUCE_EXPRESSIONS
    - Not benefit much from doing decorrelation first; do decorrelation after the first pass of VolcanoOptimizer
      - Multiple-Pass Optimization
  - capybara3
    - Require AGGREGATE_EXPAND_DISTINCT_AGGREGATES and AGGREGATE_REDUCE_FUNCTIONS
    - Filter should be push down before join
    - Projection extraction from aggregation does not work; perhaps because of the cost model that does not consider row size
      - Need to enforce with heuristic
  - q3
    - Should SORTLIMIT (TOP) before PROJECT
  - q4
    - Merge Aggregate and Project
  - q21
    - Need to put filter nearby correlate then decorrelate to transform them into joins
  - Other rationals are briefly indicated in the code

## Tried
- q10 - tends to be better with join enumeration rules (?)
  - But for some reasons, join enumeration tends to be very bad -- doubt that it might be because of the cost model
- Plug in unique columns and sorted columns as statistic - seems like nothing happened
- Try join enumeration rules (e.g., Commutative, Associative) - it makes thing worse
  - I do not quite understand why this happen. My gut feeling says that it is because of the cost model. (This is one of the reasons why I decided to customize the cost of the NestedLoopJoin)

## Feedback
- Since I have taken 15-721 (Spring 2023) before, I understand the point of getting struggle a lot on "learning tools"
  - Calcite is quite well-documented in terms of API specifications. However, in terms of getting started with it (e.g., tutorial), it takes me a while (around 2 weeks) to land on a first workable code.
- When I firstly got the first workable code, now, the things start to be very fun.
  - Experimenting different rules and finding what are wrong with Calcite and fixing them are something enjoyable
- I think it helps me understand the things a lot better aside from the lecture.
  - Do not get me wrong. The lectures are good but the project makes me really understand how rule-based and cost-based are really different.
  - Also, it makes me understand how important the cost model is :(
  