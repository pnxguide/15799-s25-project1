package edu.cmu.cs.db.calcite_app.app.op;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableNestedLoopJoin;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelNodes;
import org.apache.calcite.rel.core.CorrelationId;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMdUtil;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;

public class CustomNestedLoopJoin extends EnumerableNestedLoopJoin {
    protected CustomNestedLoopJoin(RelOptCluster cluster, RelTraitSet traits, RelNode left, RelNode right,
            RexNode condition, Set<CorrelationId> variablesSet, JoinRelType joinType) {
        super(cluster, traits, left, right, condition, variablesSet, joinType);
    }

    public static CustomNestedLoopJoin create(
            RelNode left,
            RelNode right,
            RexNode condition,
            Set<CorrelationId> variablesSet,
            JoinRelType joinType) {
        final RelOptCluster cluster = left.getCluster();
        final RelMetadataQuery mq = cluster.getMetadataQuery();
        final RelTraitSet traitSet = cluster.traitSetOf(EnumerableConvention.INSTANCE)
                .replaceIfs(RelCollationTraitDef.INSTANCE,
                        () -> RelMdCollation.enumerableNestedLoopJoin(mq, left, right, joinType));
        return new CustomNestedLoopJoin(cluster, traitSet, left, right, condition,
                variablesSet, joinType);
    }

    @Override
    public @Nullable RelOptCost computeSelfCost(RelOptPlanner planner,
            RelMetadataQuery mq) {
        double rowCount = mq.getRowCount(this);

        // Joins can be flipped, and for many algorithms, both versions are viable
        // and have the same cost. To make the results stable between versions of
        // the planner, make one of the versions slightly more expensive.
        switch (joinType) {
            case SEMI:
            case ANTI:
                // SEMI and ANTI join cannot be flipped
                break;
            case RIGHT:
                rowCount = RelMdUtil.addEpsilon(rowCount);
                break;
            default:
                if (RelNodes.COMPARATOR.compare(left, right) > 0) {
                    rowCount = RelMdUtil.addEpsilon(rowCount);
                }
        }

        final double rightRowCount = mq.getRowCount(right);
        final double leftRowCount = mq.getRowCount(left);
        if (Double.isInfinite(leftRowCount)) {
            rowCount = leftRowCount;
        }
        if (Double.isInfinite(rightRowCount)) {
            rowCount = rightRowCount;
        }

        RelOptCost cost = planner.getCostFactory().makeCost(rowCount, leftRowCount * rightRowCount,
                leftRowCount * rightRowCount);

        // Give it a lot "more" penalty -- so that we can avoid doing nested loop join
        cost = cost.multiplyBy(1e100);
        // cost = cost.multiplyBy(10);

        return cost;
    }
}
