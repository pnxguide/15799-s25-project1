package edu.cmu.cs.db.calcite_app.app;

import java.util.List;

import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.util.ImmutableBitSet;

public class TableStatistic implements Statistic {

    private final long rowCount;
    private final List<ImmutableBitSet> keys;
    private final List<RelCollation> collations;
    
    public TableStatistic(long rowCount, List<ImmutableBitSet> keys, List<RelCollation> collations) {
        this.rowCount = rowCount;
        this.keys = keys;
        this.collations = collations;
    }
    
    @Override
    public Double getRowCount() {
        return (double) rowCount;
    }

    @Override
    public List<ImmutableBitSet> getKeys() {
        return keys;
    }

    @Override
    public List<RelCollation> getCollations() {
        return collations;
    }
}