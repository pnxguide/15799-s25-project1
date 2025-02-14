package edu.cmu.cs.db.calcite_app.app;

import org.apache.calcite.schema.Statistic;

public class TableStatistic implements Statistic {

    private final long rowCount;
    
    public TableStatistic(long rowCount) {
        this.rowCount = rowCount;
    }
    
    @Override
    public Double getRowCount() {
        return (double) rowCount;
    }
    
    // Other methods no-op
}