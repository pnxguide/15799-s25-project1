package edu.cmu.cs.db.calcite_app.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    
    @Override
    public Enumerator<Object[]> enumerator() {
        try {
            Class.forName("org.duckdb.DuckDBDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
            return Linq4j.enumerator(Collections.EMPTY_LIST); 
        }

        List<Object[]> enumerableList = new ArrayList<>();

        try {
            Connection connection = DriverManager.getConnection("jdbc:duckdb:/home/pnx/15799-s25-project1/stat.db");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + this.tableName);
            while (resultSet.next()) {
                Object[] row = new Object[this.fields.size()];
                for (int i = 0; i < row.length; i++) {
                    String fieldName = this.fields.get(i).getName();
                    row[i] = resultSet.getObject(fieldName);
                }
                enumerableList.add(row);
            }
        } catch (SQLException e) {
            System.out.println(e);
            return Linq4j.enumerator(Collections.EMPTY_LIST); 
        }

        return Linq4j.enumerator(enumerableList);
    }
}
