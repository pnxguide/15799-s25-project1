package edu.cmu.cs.db.calcite_app.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.tools.RelRunner;

public class Processor {

    private final CalciteConnection calciteConnection;

    private static Processor INSTANCE;

    public static Processor getInstance() throws ClassNotFoundException, SQLException {
        if (INSTANCE == null) {
            INSTANCE = new Processor();
        }
        return INSTANCE;
    }

    public void setSchema(CalciteSchema schema) throws SQLException, ClassNotFoundException {
        SchemaPlus rootSchema = this.calciteConnection.getRootSchema();
        for (String tableName : schema.getTableNames()) {
            Table table = schema.getTable(tableName, true).getTable();
            rootSchema.add(tableName, new CustomTable(table, new TableStatistic(100), tableName));
        }
    }

    private Processor() throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.calcite.jdbc.Driver");
        Properties info = new Properties();
        info.setProperty("lex", "JAVA");
        Connection connection
                = DriverManager.getConnection("jdbc:calcite:", info);
        this.calciteConnection
                = connection.unwrap(CalciteConnection.class);
    }

    public ResultSet execute(RelNode relNode) throws SQLException {
        RelRunner runner = this.calciteConnection.unwrap(RelRunner.class);
        PreparedStatement statement = runner.prepareStatement(relNode);
        ResultSet resultSet = statement.executeQuery();
        return resultSet;
    }
}
