package edu.cmu.cs.db.calcite_app.app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.PostgresqlSqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlString;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.Program;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RuleSet;
import org.apache.calcite.tools.RuleSets;

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

    public static void optimize(String query, File inputFile, File outputDirectory, File statisticsFile) throws Exception {
        String baseSql = query;

        Optimizer optimizer = Optimizer.getInstance();
        RelNode validatedSqlNode = optimizer.parseAndValidate(baseSql);
        System.out.println(validatedSqlNode);

        // VolcanoPlanner planner = new VolcanoPlanner(
        //     RelOptCostImpl.FACTORY, 
        //     Contexts.of(config)
        // );
        // planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        // // planner.addRelTraitDef(RelDistributionTraitDef.INSTANCE);
        // // planner.addRule(CoreRules.FILTER_INTO_JOIN);
        // // planner.addRule(EnumerableRules.ENUMERABLE_SORT_RULE);
        // // planner.addRule(EnumerableRules.ENUMERABLE_VALUES_RULE);
        // // planner.addRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
        // // planner.addRule(EnumerableRules.ENUMERABLE_FILTER_RULE);
        // // planner.addRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);

        
        
        
        // System.out.println(RelOptUtil.dumpPlan("unoptimized", relNode, SqlExplainFormat.TEXT, SqlExplainLevel.ALL_ATTRIBUTES));
        
        // // RelNode enumerableRelNode = planner.changeTraits(relNode, relNode.getCluster().traitSet().replace(EnumerableConvention.INSTANCE));
        // // planner.setRoot(enumerableRelNode);
        // // planner.setRoot(relNode);

        // RuleSet rules = RuleSets.ofList(
        //     CoreRules.FILTER_TO_CALC,
        //     CoreRules.PROJECT_TO_CALC,
        //     CoreRules.FILTER_CALC_MERGE,
        //     CoreRules.PROJECT_CALC_MERGE,
        //     EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE,
        //     EnumerableRules.ENUMERABLE_PROJECT_RULE,
        //     EnumerableRules.ENUMERABLE_FILTER_RULE,
        //     EnumerableRules.ENUMERABLE_CALC_RULE,
        //     EnumerableRules.ENUMERABLE_AGGREGATE_RULE
        // );

        // Program program = Programs.of(RuleSets.ofList(rules));
        // RelNode optimizedRelNode = program.run(
        //     planner,
        //     relNode,
        //     relNode.getTraitSet().plus(EnumerableConvention.INSTANCE),
        //     Collections.emptyList(),
        //     Collections.emptyList()
        // );
        
        // System.out.println(RelOptUtil.dumpPlan("optimized", optimizedRelNode, SqlExplainFormat.TEXT, SqlExplainLevel.ALL_ATTRIBUTES));

        // // // Run optimized query
        // // RelRunner runner = connection.unwrap(RelRunner.class);
        // // PreparedStatement stmt = runner.prepareStatement(optimizedRelNode);
        // // ResultSet resultSet = stmt.executeQuery();

        // // RelNode to SQL
        // RelToSqlConverter rel2sql = new RelToSqlConverter(PostgresqlSqlDialect.DEFAULT);
        // RelToSqlConverter.Result res = rel2sql.visitRoot(optimizedRelNode);
        // SqlNode optimizedSqlNode = res.asQueryOrValues();
        // SqlString optimizedSql = optimizedSqlNode.toSqlString(PostgresqlSqlDialect.DEFAULT);
        // System.out.println(optimizedSql);

        // // Output
        // String initialOutputFileName = outputDirectory.getAbsolutePath() + "/" + getFileNameWithoutExtension(inputFile);
        // System.out.println(initialOutputFileName);

        // // 1. query.sql
        // SerializeSql(baseSql, new File(initialOutputFileName + ".sql"));
        // // 2. query.txt
        // SerializePlan(relNode, new File(initialOutputFileName + ".txt"));
        // // // 3. query_optimized.txt
        // // SerializePlan(optimizedRelNode, new File(initialOutputFileName + "_optimized.txt"));
        // // // 4. query_result.csv
        // // SerializeResultSet(resultSet, new File(initialOutputFileName + "_result.csv"));
        // // 5. query_optimized.sql
        // SerializeSql(optimizedSql.toString(), new File(initialOutputFileName + "_optimized.sql"));
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Usage: java -jar App.jar <input_file> <output_dir> <statistics_file>");
            return;
        }

        File inputFile = new File(args[0]);
        File outputDirectory = new File(args[1]);
        File statisticsFile = new File(args[2]);

        optimize(Files.readString(inputFile.toPath(), Charset.defaultCharset()), inputFile, outputDirectory, statisticsFile);
    }
}
