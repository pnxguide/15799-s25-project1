package edu.cmu.cs.db.calcite_app.app;

import java.util.Map;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

public class SimpleSchema extends AbstractSchema {

    private final String schemaName;
    private final Map<String, Table> tableMap;

    private SimpleSchema(String schemaName, Map<String, Table> tableMap) {
        this.schemaName = schemaName;
        this.tableMap = tableMap;
    }

    @Override
    public Map<String, Table> getTableMap() {
        return tableMap;
    }
}