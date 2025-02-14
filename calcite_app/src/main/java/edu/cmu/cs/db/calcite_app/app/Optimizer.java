package edu.cmu.cs.db.calcite_app.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import static org.apache.calcite.adapter.enumerable.EnumerableRules.ENUMERABLE_AGGREGATE_RULE;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.Program;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RelRunner;
import org.apache.calcite.tools.RuleSet;
import org.apache.calcite.tools.RuleSets;

public class Optimizer {

    private final CalciteSchema rootSchema;
    private final DataSource jdbcDataSource;
    private final RelOptPlanner planner;
    private final CalciteConnectionConfig config;

    private static Optimizer INSTANCE;

    public static Optimizer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Optimizer();
        }
        return INSTANCE;
    }

    private Optimizer() {
        this.rootSchema = CalciteSchema.createRootSchema(false, false);

        this.jdbcDataSource = JdbcSchema.dataSource(
                "jdbc:duckdb:/home/pnx/15799-s25-project1/stat.db", "org.duckdb.DuckDBDriver", null, null);

        Schema schema = JdbcSchema.create(this.rootSchema.plus(), "stat", this.jdbcDataSource, null, null);
        this.rootSchema.add("stat", schema);

        Properties configProperties = new Properties();
        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
        configProperties.put(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.toString());
        configProperties.put(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.toString());

        this.config = new CalciteConnectionConfigImpl(configProperties);

        this.planner = new VolcanoPlanner(
                RelOptCostImpl.FACTORY,
                Contexts.of(this.config)
        );
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
    }

    public void execute(RelNode relNode) throws SQLException {
        Connection connection = this.jdbcDataSource.createConnectionBuilder().build();
        RelRunner runner = connection.unwrap(RelRunner.class);

        PreparedStatement statement = runner.prepareStatement(relNode);
        ResultSet resultSet = statement.executeQuery();

        System.out.println(resultSet);
    }

    public RelNode optimize(RelNode relNode) {
        RuleSet rules;
        rules = RuleSets.ofList(CoreRules.FILTER_TO_CALC,
                CoreRules.PROJECT_TO_CALC,
                CoreRules.FILTER_CALC_MERGE,
                CoreRules.PROJECT_CALC_MERGE,
                EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE,
                EnumerableRules.ENUMERABLE_PROJECT_RULE,
                EnumerableRules.ENUMERABLE_FILTER_RULE,
                EnumerableRules.ENUMERABLE_CALC_RULE, ENUMERABLE_AGGREGATE_RULE);

        Program program = Programs.of(RuleSets.ofList(rules));
        RelNode optimizedRelNode;
        optimizedRelNode = program.run(
                this.planner,
                relNode,
                relNode.getTraitSet().plus(EnumerableConvention.INSTANCE),
                Collections.emptyList(),
                Collections.emptyList()
        );

        return optimizedRelNode;
    }

    public RelNode parseAndValidate(String baseSql) throws SqlParseException {
        JavaTypeFactoryImpl typeFactory = new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT);

        CatalogReader catalogReader = new CalciteCatalogReader(
                this.rootSchema,
                Collections.singletonList("stat"),
                typeFactory,
                config
        );

        SqlValidator validator = SqlValidatorUtil.newValidator(
                SqlStdOperatorTable.instance(),
                catalogReader,
                typeFactory,
                SqlValidator.Config.DEFAULT
        );

        SqlParser parser = SqlParser.create(
                baseSql,
                SqlParser.config()
                        .withCaseSensitive(config.caseSensitive())
                        .withUnquotedCasing(config.unquotedCasing())
                        .withQuotedCasing(config.quotedCasing())
                        .withConformance(config.conformance())
        );

        SqlNode sqlNode = parser.parseStmt();
        SqlNode validatedSqlNode = validator.validate(sqlNode);

        SqlToRelConverter sql2rel = new SqlToRelConverter(
                null,
                validator,
                catalogReader,
                RelOptCluster.create(this.planner, new RexBuilder(typeFactory)),
                StandardConvertletTable.INSTANCE,
                SqlToRelConverter.config()
                        .withTrimUnusedFields(true)
                        .withExpand(false)
        );

        RelNode relNode = sql2rel.convertQuery(validatedSqlNode, false, true).rel;

        return relNode;
    }
}
