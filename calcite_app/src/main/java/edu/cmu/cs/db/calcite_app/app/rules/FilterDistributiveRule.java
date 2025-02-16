package edu.cmu.cs.db.calcite_app.app.rules;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.immutables.value.Value;

@Value.Enclosing
public class FilterDistributiveRule extends RelRule<FilterDistributiveRule.Config> {

    protected FilterDistributiveRule(Config config) {
        super(config);
    }

    @Override
    public void onMatch(RelOptRuleCall call) {
        throw new UnsupportedOperationException("Not supported yet.");
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
