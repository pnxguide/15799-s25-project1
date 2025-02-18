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
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.util.ImmutableBitSet;

public class Database {

    private Map<String, List<Object[]>> tables;
    private Map<String, List<ImmutableBitSet>> tableKeys;
    private Map<String, List<RelCollation>> tableSortedColumns;
    private boolean isRead;

    private static Database INSTANCE;

    public static Database getInstance() throws ClassNotFoundException, SQLException {
        if (INSTANCE == null) {
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

    private Database() {
        this.tables = new LinkedHashMap<>();
        this.tableKeys = new LinkedHashMap<>();
        this.tableSortedColumns = new LinkedHashMap<>();

        // Mark as unread
        this.isRead = false;
    }

    public List<Object[]> getTable(String tableName) {
        return this.tables.get(tableName);
    }

    public List<ImmutableBitSet> getTableKey(String tableName) {
        return this.tableKeys.get(tableName);
    }

    public List<RelCollation> getSortedColumns(String tableName) {
        return this.tableSortedColumns.get(tableName);
    }

    public void loadData(Schema schema, File duckDbFile) throws SQLException {
        if (isRead) {
            return;
        }

        Set<String> tableNames = schema.getTableNames();

        try {
            Class.forName("org.duckdb.DuckDBDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        }

        try {
            try (Connection connection = DriverManager.getConnection("jdbc:duckdb:" + duckDbFile.getPath())) {
                for (String tableName : tableNames) {
                    if (tables.containsKey(tableName)) {
                        continue;
                    }
                    
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

                    // List<RelFieldCollation> sorted = new ArrayList<>();                    
                    // // Find sorted columns (only numerics columns)
                    // for (int i = 0; i < fields.size(); i++) {
                    //     boolean isAscending = true;
                    //     if (fields.get(i).getType().getSqlTypeName().getName().equals("BIGINT") || fields.get(i).getType().getSqlTypeName().getName().equals("INTEGER")) {
                    //         long prev = ((Number)enumerableList.get(0)[i]).longValue();
                    //         for (int j = 1; j < enumerableList.size(); j++) {
                    //             long cur = ((Number)enumerableList.get(j)[i]).longValue();
                    //             if (prev > cur) {
                    //                 isAscending = false;
                    //                 break;
                    //             }
                    //         }
                    //     }

                    //     if (isAscending) {
                    //         sorted.add(new RelFieldCollation(i, RelFieldCollation.Direction.ASCENDING));
                    //     }
                    // }
                    // List<RelCollation> sortList = new ArrayList<>();
                    // RelCollation sortCollation = RelCollationImpl.of(sorted);
                    // sortList.add(sortCollation);
                    // this.tableSortedColumns.put(tableName, sortList);

                    // // Find keys (only numeric columns)
                    // List<Integer> uniqueColumns = new ArrayList<>();
                    // for (int i = 0; i < fields.size(); i++) {
                    //     Set<Object> uniqueObjects = new HashSet<>();
                    //     for (int r = 0; r < enumerableList.size(); r++) {
                    //         Object[] row = enumerableList.get(r);
                    //         uniqueObjects.add(row[i]);
                    //     }
                    //     if (uniqueObjects.size() == enumerableList.size()) {
                    //         uniqueColumns.add(1);
                    //     } else {
                    //         uniqueColumns.add(0);
                    //     }
                    //     System.gc();
                    // }

                    // List<ImmutableBitSet> uniqueList = new ArrayList<>();
                    // uniqueList.add(ImmutableBitSet.builder().addAll(uniqueColumns).build());
                    // this.tableKeys.put(tableName, uniqueList);

                    this.tableSortedColumns.put(tableName, null);
                    this.tableKeys.put(tableName, null);
                    
                    System.gc();
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

        this.isRead = true;

        System.gc();
    }
}
