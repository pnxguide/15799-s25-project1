package edu.cmu.cs.db.calcite_app.app;

import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import static org.apache.calcite.jdbc.CalciteSchema.createRootSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.PostgresqlSqlDialect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlString;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;

import edu.cmu.cs.db.calcite_app.app.rules.FilterDistributiveRule;

// import edu.cmu.cs.db.calcite_app.app.rules.FilterDistributiveRule;

public class Optimizer {

    private final CalciteSchema rootSchema;
    private final RelDataTypeFactory typeFactory;
    private RelOptCluster cluster;

    private static Optimizer INSTANCE;
    private static final RelOptTable.ViewExpander NOOP_EXPANDER = (type, query, schema, path) -> null;

    public static Optimizer getInstance() throws SQLException, ClassNotFoundException {
        if (INSTANCE == null) {
            INSTANCE = new Optimizer();
        }
        return INSTANCE;
    }

    public CalciteSchema getSchema() {
        return this.rootSchema;
    }

    private Optimizer() throws SQLException, ClassNotFoundException {
        this.rootSchema = createRootSchema(false);
        this.typeFactory = new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
    }

    public void initialize(File duckDbFile) throws SQLException, ClassNotFoundException {
        // Discovering schema
        DataSource jdbcDataSource = JdbcSchema.dataSource(
                "jdbc:duckdb:" + duckDbFile.getPath(), "org.duckdb.DuckDBDriver", null, null);
        Schema schema = JdbcSchema.create(this.rootSchema.plus(), "stat", jdbcDataSource, null, null);

        // 
        Database db = Database.getInstance();
        db.loadData(schema, duckDbFile);

        // 
        for (String tableName : schema.getTableNames()) {
            Table table = schema.getTable(tableName);
            this.rootSchema.add(tableName, new CustomTable(table, new TableStatistic(db.getTable(tableName).size()), tableName));
        }

        // Initialize planner and cluster
        RelOptPlanner planner = new VolcanoPlanner();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        this.cluster = RelOptCluster.create(planner, new RexBuilder(this.typeFactory));
    }

    public EnumerableRel optimize(RelNode relNode) {
        RelOptPlanner planner = this.cluster.getPlanner();
        planner.addRule(FilterDistributiveRule.Config.DEFAULT.toRule());
        planner.addRule(CoreRules.FILTER_INTO_JOIN);
        planner.addRule(CoreRules.FILTER_REDUCE_EXPRESSIONS);
        planner.addRule(CoreRules.AGGREGATE_EXPAND_DISTINCT_AGGREGATES);
        planner.addRule(CoreRules.AGGREGATE_REDUCE_FUNCTIONS);
        planner.addRule(EnumerableRules.ENUMERABLE_JOIN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_SORT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_LIMIT_SORT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_FILTER_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_VALUES_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_AGGREGATE_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_CORRELATE_RULE);

        RelNode newRoot = planner.changeTraits(relNode, relNode.getTraitSet().replace(EnumerableConvention.INSTANCE));
        planner.setRoot(newRoot);

        EnumerableRel optimizedNode = (EnumerableRel) planner.findBestExp();

        return optimizedNode;
    }

    public RelNode parseAndValidate(String baseSql) throws SqlParseException {
        // Initialize config
        Properties configProperties = new Properties();
        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
        configProperties.put(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.toString());
        configProperties.put(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.toString());

        CalciteConnectionConfigImpl config = new CalciteConnectionConfigImpl(configProperties);

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
                        .withExpand(true)
        );

        RelNode relNode = sql2rel.convertQuery(validatedSqlNode, false, true).rel;

        return relNode;
    }

    public SqlString relNodeToSqlString(RelNode relNode) {
        RelToSqlConverter rel2sql = new RelToSqlConverter(PostgresqlSqlDialect.DEFAULT);
        RelToSqlConverter.Result res = rel2sql.visitRoot(relNode);
        SqlNode optimizedSqlNode = res.asQueryOrValues();
        SqlString optimizedSql = optimizedSqlNode.toSqlString(PostgresqlSqlDialect.DEFAULT);
        return optimizedSql;
    }
}
