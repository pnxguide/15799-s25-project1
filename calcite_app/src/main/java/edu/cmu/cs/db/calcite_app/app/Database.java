package edu.cmu.cs.db.calcite_app.app;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.schema.Schema;

public class Database {

    private Map<String, List<Object[]>> tables;

    private static Database INSTANCE;

    public static Database getInstance() throws ClassNotFoundException, SQLException {
        if (INSTANCE == null) {
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

    private Database() {
        this.tables = new LinkedHashMap<>();
    }

    public List<Object[]> getTable(String tableName) {
        return this.tables.get(tableName);
    }

    public void loadData(Schema schema, File duckDbFile) throws SQLException {
        Set<String> tableNames = schema.getTableNames();

        try {
            Class.forName("org.duckdb.DuckDBDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        }

        try {
            Connection connection = DriverManager.getConnection("jdbc:duckdb:" + duckDbFile.getPath());
            
            for (String tableName : tableNames) {
                List<Object[]> enumerableList = new ArrayList<>();
                List<RelDataTypeField> fields = schema.getTable(tableName).getRowType(new JavaTypeFactoryImpl()).getFieldList();

                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
                    while (resultSet.next()) {
                        Object[] row = new Object[fields.size()];
                        for (int i = 0; i < row.length; i++) {
                            String fieldName = fields.get(i).getName();
                            row[i] = resultSet.getObject(fieldName);
                        }
                        enumerableList.add(row);
                    }
                }

                this.tables.put(tableName, enumerableList);
                System.out.println("Table " + tableName + " has been scanned!");

                System.gc();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
