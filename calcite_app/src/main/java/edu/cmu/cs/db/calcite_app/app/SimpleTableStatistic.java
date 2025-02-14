package edu.cmu.cs.db.calcite_app.app;

import org.apache.calcite.schema.Statistic;

public class SimpleTableStatistic implements Statistic {

    private final long rowCount;
    
    public SimpleTableStatistic(long rowCount) {
        this.rowCount = rowCount;
    }
    
    @Override
    public Double getRowCount() {
        return (double) rowCount;
    }
    
    // Other methods no-op
}