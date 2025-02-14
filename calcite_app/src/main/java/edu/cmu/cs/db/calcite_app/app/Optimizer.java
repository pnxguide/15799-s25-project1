package edu.cmu.cs.db.calcite_app.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.adapter.jdbc.JdbcTable;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.RelRunner;

public class Optimizer {

    private final CalciteSchema rootSchema;
    private final DataSource jdbcDataSource;
    private final RelOptCluster cluster;
    private final CalciteConnectionConfig config;
    private final RelDataTypeFactory typeFactory;

    private static Optimizer INSTANCE;
    private static final RelOptTable.ViewExpander NOOP_EXPANDER = (type, query, schema, path) -> null;

    public static Optimizer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Optimizer();
        }
        return INSTANCE;
    }

    private Optimizer() {
        this.rootSchema = CalciteSchema.createRootSchema(false);
        this.typeFactory = new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
    
        // Discovering the schema
        this.jdbcDataSource = JdbcSchema.dataSource(
                "jdbc:duckdb:/home/pnx/15799-s25-project1/stat.db", "org.duckdb.DuckDBDriver", null, null);
        Schema schema = JdbcSchema.create(this.rootSchema.plus(), "stat", this.jdbcDataSource, null, null);
        for (String tableName : schema.getTableNames()) {
            JdbcTable table = (JdbcTable)schema.getTable(tableName);
            List<String> fieldNames = new ArrayList<>();
            List<SqlTypeName> fieldTypes = new ArrayList<>();
            for (RelDataTypeField column : table.getRowType(typeFactory).getFieldList()) {
                fieldNames.add(column.getName());
                fieldTypes.add(column.getType().getSqlTypeName());
            }
            this.rootSchema.add(tableName, new CustomTable(tableName, fieldNames, fieldTypes, new TableStatistic(100)));
        }

        Properties configProperties = new Properties();
        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
        configProperties.put(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.toString());
        configProperties.put(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.toString());

        this.config = new CalciteConnectionConfigImpl(configProperties);

        RelOptPlanner planner = new VolcanoPlanner();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        this.cluster = RelOptCluster.create(planner, new RexBuilder(this.typeFactory));
    }

    public void execute(RelNode relNode) throws SQLException, ClassNotFoundException {
        Connection connection = this.jdbcDataSource.getConnection();
        RelRunner runner = connection.unwrap(RelRunner.class);

        PreparedStatement statement = runner.prepareStatement(relNode);
        ResultSet resultSet = statement.executeQuery();

        System.out.println(resultSet);
    }

    public EnumerableRel optimize(RelNode relNode) {
        RelOptPlanner planner = this.cluster.getPlanner();
        planner.addRule(EnumerableRules.ENUMERABLE_JOIN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_SORT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_LIMIT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_AGGREGATE_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_FILTER_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_LIMIT_RULE);

        RelNode newRoot = planner.changeTraits(relNode, relNode.getTraitSet().replace(EnumerableConvention.INSTANCE));
        planner.setRoot(newRoot);

        EnumerableRel optimizedNode = (EnumerableRel) planner.findBestExp();

        return optimizedNode;
    }

    public RelNode parseAndValidate(String baseSql) throws SqlParseException {
        CatalogReader catalogReader = new CalciteCatalogReader(
                this.rootSchema,
                Collections.singletonList("stat"),
                this.typeFactory,
                config
        );

        SqlValidator validator = SqlValidatorUtil.newValidator(
                SqlStdOperatorTable.instance(),
                catalogReader,
                this.typeFactory,
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
                NOOP_EXPANDER,
                validator,
                catalogReader,
                this.cluster,
                StandardConvertletTable.INSTANCE,
                SqlToRelConverter.config()
                        .withTrimUnusedFields(true)
                        .withExpand(false)
        );

        RelNode relNode = sql2rel.convertQuery(validatedSqlNode, false, true).rel;

        return relNode;
    }
}
