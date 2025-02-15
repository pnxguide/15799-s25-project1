package edu.cmu.cs.db.calcite_app.app;

import java.util.List;

import org.apache.calcite.DataContext;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractTable;

public class CustomTable extends AbstractTable implements ScannableTable {

    private final Table table;
    private final TableStatistic statistic;

    private final List<RelDataTypeField> fields;
    private final String tableName;

    public CustomTable(
        Table table,
        TableStatistic statistic,
        String tableName
    ) {
        this.table = table;
        this.statistic = statistic;
        this.tableName = tableName;
        this.fields = this.table.getRowType(new JavaTypeFactoryImpl()).getFieldList();
    }
    
    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        return this.table.getRowType(typeFactory);
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        return new CustomEnumerable(tableName, this.fields);
    }

    @Override
    public Statistic getStatistic() {
        return statistic;
    }
}