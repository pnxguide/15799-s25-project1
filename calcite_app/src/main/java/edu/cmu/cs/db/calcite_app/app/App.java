package edu.cmu.cs.db.calcite_app.app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.dialect.PostgresqlSqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlOperatorTables;
import org.apache.calcite.sql.util.SqlString;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;

public class App {

    public static String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
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

        // Schema
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(false, false);

        DataSource dataSource = JdbcSchema.dataSource(
                "jdbc:duckdb:/home/ubuntu/15799-s25-project1/stat.db", "org.duckdb.DuckDBDriver", null, null);

        Schema schema = JdbcSchema.create(rootSchema.plus(), "stat", dataSource, null, null);
        rootSchema.add("stat", schema);

        schema.getTableNames().forEach(System.out::println);

        // Parse
        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

        Properties configProperties = new Properties();

        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
        configProperties.put(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.toString());
        configProperties.put(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.toString());

        CalciteConnectionConfig config = new CalciteConnectionConfigImpl(configProperties);

        CatalogReader catalogReader = new CalciteCatalogReader(
            rootSchema,
            Collections.singletonList("stat"),
            typeFactory,
            config
        );

        SqlOperatorTable operatorTable = SqlOperatorTables.chain(
            SqlStdOperatorTable.instance()
        );

        SqlValidator validator = SqlValidatorUtil.newValidator(
            operatorTable,
            catalogReader,
            typeFactory,
            SqlValidator.Config.DEFAULT
        );

        System.out.println(baseSQL);

        SqlParser parser = SqlParser.create(
            baseSQL,
            SqlParser.config()
                .withCaseSensitive(config.caseSensitive())
                .withUnquotedCasing(config.unquotedCasing())
                .withQuotedCasing(config.quotedCasing())
                .withConformance(config.conformance())
        );
        SqlNode parseTree = parser.parseStmt();

        System.out.println(parseTree);

        SqlNode validatedParseTree = validator.validate(parseTree);

        // Output
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
