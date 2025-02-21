package edu.cmu.cs.db.calcite_app.app;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataTypeField;

public class CustomEnumerable extends AbstractEnumerable<Object[]> {
    private final String tableName;
    private List<RelDataTypeField> fields;
    
    public CustomEnumerable(String tableName, List<RelDataTypeField> fields) {
        this.tableName = tableName;
        this.fields = fields;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Enumerator<Object[]> enumerator() {
        try {
            Database db = Database.getInstance();
            return Linq4j.enumerator(db.getTable(this.tableName));
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e);
        }
        
        return Linq4j.enumerator(Collections.EMPTY_LIST);
    }
}
