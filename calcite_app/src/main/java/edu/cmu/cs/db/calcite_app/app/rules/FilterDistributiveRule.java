package edu.cmu.cs.db.calcite_app.app.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.tools.RelBuilder;
import org.immutables.value.Value;

// (A AND B AND C) OR (A AND D AND E) => A AND ((B AND C) OR (D AND E))

@Value.Enclosing
public class FilterDistributiveRule extends RelRule<FilterDistributiveRule.Config> implements TransformationRule {

    protected FilterDistributiveRule(Config config) {
        super(config);
    }

    @Override
    public boolean matches(final RelOptRuleCall call) {
        Filter filter = call.rel(0);
        RexCall parentExpr = (RexCall) filter.getCondition();

        // Check whether the parent OP must be OR
        if (parentExpr.operandCount() > 1 && parentExpr.op.kind.equals(SqlKind.OR)) {
            // Check whether all the children's OP must be AND
            for (RexNode n : parentExpr.operands) {
                RexCall expr = (RexCall) n;
                if (!expr.op.kind.equals(SqlKind.AND)) {
                    return false;
                } 
            }
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMatch(RelOptRuleCall call) {
        Filter filter = call.rel(0);
        RexCall parentExpr = (RexCall) filter.getCondition();

        Set<RexNode> intersection = new HashSet<>();
        Set<RexNode>[] leftExpr = new Set[parentExpr.operandCount()];
        if (parentExpr.operandCount() > 1 && parentExpr.op.kind.equals(SqlKind.OR)) {
            // Get the first expression
            RexCall firstExpr = (RexCall) parentExpr.operands.get(0);
            leftExpr[0] = new HashSet<>(firstExpr.operands);

            // Initialize intersection using the first expression
            intersection = new HashSet<>(firstExpr.operands);

            for (int i = 1; i < parentExpr.operandCount(); i++) {
                RexCall expr = (RexCall) parentExpr.operands.get(i);
                Set<RexNode> extractedExpr = new HashSet<>(expr.operands);
                leftExpr[i] = new HashSet<>(extractedExpr);

                // Intersect more onto the intersection
                intersection.retainAll(extractedExpr);
            }
        }

        System.out.println("Intersection " + ": " + intersection);

        for (int i = 0; i < parentExpr.operandCount(); i++) {
            leftExpr[i].removeAll(intersection);
            System.out.println("Expr " + (i+1) + ": " + leftExpr[i]);
        }

        final RelBuilder leftBuilder = call.builder();
        List<RexNode> orExpr = new ArrayList<>();
        for (int i = 0; i < parentExpr.operandCount(); i++) {
            orExpr.add(leftBuilder.and(leftExpr[i]));
        }

        // intersection AND (left1 OR left2 OR left3 OR ...)
        final RelBuilder filterBuilder = call.builder();
        filterBuilder.push(filter.getInput());
        filterBuilder.filter(
            filterBuilder.and(filterBuilder.and(intersection), filterBuilder.or(orExpr))
        );
        call.transformTo(filterBuilder.build());
    }

    @Value.Immutable(singleton = false)
    public interface Config extends RelRule.Config {
        Config DEFAULT = ImmutableFilterDistributiveRule.Config.builder()
                .operandSupplier(b0 -> b0.operand(LogicalFilter.class).anyInputs())
                .build();

        @Override
        default FilterDistributiveRule toRule() {
            return new FilterDistributiveRule(this);
        }
    }
}
