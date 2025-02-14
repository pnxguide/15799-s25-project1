package edu.cmu.cs.db.calcite_app.app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;

public class App {
    // Method getFileNameWithoutExtension
    // Code Reference: https://stackoverflow.com/questions/924394/how-to-get-the-filename-without-the-extension-in-java
    public static String getFileNameWithoutExtension(File file) {
        final Pattern ext = Pattern.compile("(?<=.)\\.[^.]+$");
        return ext.matcher(file.getName()).replaceAll("");
    }

    private static void SerializeSql(String sql, File outputPath) throws IOException {
        Files.writeString(outputPath.toPath(), sql);
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
                resultSetString.append(",");
            }
            resultSetString.append(metaData.getColumnName(i));
        }
        resultSetString.append("\n");
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) {
                    resultSetString.append(",");
                }
                String s = resultSet.getString(i);
                s = s.replace("\n", "\\n");
                s = s.replace("\r", "\\r");
                s = s.replace("\"", "\"\"");
                resultSetString.append("\"");
                resultSetString.append(s);
                resultSetString.append("\"");
            }
            resultSetString.append("\n");
        }
        Files.writeString(outputPath.toPath(), resultSetString.toString());
    }

    public static void process(String query, File inputFile, File outputDirectory, File statisticsFile) throws Exception {
        String baseSql = query;

        Optimizer optimizer = Optimizer.getInstance();
        RelNode validatedSqlNode = optimizer.parseAndValidate(baseSql);
        RelNode optimizedSqlNode = optimizer.optimize(validatedSqlNode);
        System.out.println(RelOptUtil.dumpPlan("", optimizedSqlNode, SqlExplainFormat.TEXT, SqlExplainLevel.ALL_ATTRIBUTES));

        // optimizer.execute(optimizedSqlNode);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Usage: java -jar App.jar <input_file> <output_dir> <statistics_file>");
            return;
        }

        File inputFile = new File(args[0]);
        File outputDirectory = new File(args[1]);
        File statisticsFile = new File(args[2]);

        process(Files.readString(inputFile.toPath(), Charset.defaultCharset()), inputFile, outputDirectory, statisticsFile);
    }
}
