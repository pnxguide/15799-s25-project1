package edu.cmu.cs.db.calcite_app.app;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.charset.Charset;

import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;

import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.util.SqlString;
import org.apache.calcite.sql.dialect.PostgresqlSqlDialect;

public class App {
    public static String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }

        return extension;
    }

    private static void SerializePlan(RelNode relNode, File outputPath) throws IOException {
        Files.writeString(outputPath.toPath(), RelOptUtil.dumpPlan("", relNode, SqlExplainFormat.TEXT, SqlExplainLevel.ALL_ATTRIBUTES));
    }

    private static void SerializeResultSet(ResultSet resultSet, File outputPath) throws SQLException, IOException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        StringBuilder resultSetString = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            if (i > 1) {
                resultSetString.append(", ");
            }
            resultSetString.append(metaData.getColumnName(i));
        }
        resultSetString.append("\n");
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) {
                    resultSetString.append(", ");
                }
                resultSetString.append(resultSet.getString(i));
            }
            resultSetString.append("\n");
        }
        Files.writeString(outputPath.toPath(), resultSetString.toString());
    }

    public static void optimize(String query, File outputDirectory, File statisticsFile) throws Exception {
        String baseSQL = query;

        SqlParser parser = SqlParser.create(baseSQL);
        SqlNode parseTree = parser.parseQuery();

        // TODO:

        SqlString optimizedSQL = parseTree.toSqlString(PostgresqlSqlDialect.DEFAULT);
        System.out.println(optimizedSQL);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Usage: java -jar App.jar <input_file> <output_dir> <statistics_file>");
            return;
        }

        File inputFile = new File(args[0]);
        File outputDirectory = new File(args[1]);
        File statisticsFile = new File(args[2]);

        optimize(Files.readString(inputFile.toPath(), Charset.defaultCharset()), outputDirectory, statisticsFile);
    }
}
