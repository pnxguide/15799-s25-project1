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
import org.apache.calcite.sql.util.SqlString;

public class App {

    public static String getFileExtension(File f) {
        String filename = f.getName();
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

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

    public static void process(String query, File inputFile, File outputDirectory, File statisticsFile, File duckDbFile) throws Exception {
        String initialOutputFileName = outputDirectory.getAbsolutePath() + "/" + getFileNameWithoutExtension(inputFile);

        String baseSql = query;
        SerializeSql(baseSql, new File(initialOutputFileName + ".sql"));

        Optimizer optimizer = Optimizer.getInstance();
        optimizer.initialize(duckDbFile);

        RelNode validatedSqlNode = optimizer.parseAndValidate(baseSql);
        SerializePlan(validatedSqlNode, new File(initialOutputFileName + ".txt"));

        RelNode optimizedSqlNode = optimizer.optimize(validatedSqlNode);
        SerializePlan(optimizedSqlNode, new File(initialOutputFileName + "_optimized.txt"));

        SqlString optimizedSql = optimizer.relNodeToSqlString(optimizedSqlNode);
        SerializeSql(optimizedSql.toString(), new File(initialOutputFileName + "_optimized.sql"));
        System.out.println(RelOptUtil.dumpPlan("", optimizedSqlNode, SqlExplainFormat.TEXT, SqlExplainLevel.ALL_ATTRIBUTES));

        Processor processor = Processor.getInstance();

        processor.setSchema(optimizer.getSchema());

        try {
            ResultSet resultSet = processor.execute(optimizedSqlNode);
            SerializeResultSet(resultSet, new File(initialOutputFileName + "_result.csv"));
        } catch (SQLException e) {
            System.out.println(e);
        }

        System.gc();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("Usage: java -jar App.jar <input_file> <output_dir> <statistics_file>");
            return;
        }

        File queryDirectory = new File(args[0]);
        File outputDirectory = new File(args[1]);
        File statisticsFile = new File(args[2]);
        File duckDbFile = new File(args[3]);

        // Debug
        if (queryDirectory.isFile()) {
            File inputFile = queryDirectory;
            process(Files.readString(inputFile.toPath(), Charset.defaultCharset()), inputFile, outputDirectory, statisticsFile, duckDbFile);
        } else {
            for (File inputFile : queryDirectory.listFiles()) {
                if (getFileExtension(inputFile).equals("sql")) {
                    System.out.println("Optimizing " + inputFile.getName() + "...");
                    process(Files.readString(inputFile.toPath(), Charset.defaultCharset()), inputFile, outputDirectory, statisticsFile, duckDbFile);
                }
            }
        }
    }
}
