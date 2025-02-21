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
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.rules.FilterFlattenCorrelatedConditionRule;
import org.apache.calcite.rel.rules.PruneEmptyRules;
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
import org.apache.calcite.sql2rel.RelDecorrelator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;

import edu.cmu.cs.db.calcite_app.app.rules.CustomEnumerableJoinRule;
import edu.cmu.cs.db.calcite_app.app.rules.FilterDistributiveRule;

public class Optimizer {

    private final CalciteSchema rootSchema;
    private final RelDataTypeFactory typeFactory;
    private RelOptCluster cboCluster, rboCluster;
    private boolean isInitialized;

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
        this.isInitialized = false;
        this.rootSchema = createRootSchema(false);
        this.typeFactory = new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
    }

    public void initialize(File duckDbFile) throws SQLException, ClassNotFoundException {
        if (this.isInitialized) {
            return;
        }

        // Discovering schema
        DataSource jdbcDataSource = JdbcSchema.dataSource(
                "jdbc:duckdb:" + duckDbFile.getPath(), "org.duckdb.DuckDBDriver", null, null);
        Schema schema = JdbcSchema.create(this.rootSchema.plus(), "stat", jdbcDataSource, null, null);

        // Load data
        Database db = Database.getInstance();
        db.loadData(schema, duckDbFile);

        // Add tables and their statistics
        for (String tableName : schema.getTableNames()) {
            Table table = schema.getTable(tableName);
            this.rootSchema.add(tableName, new CustomTable(table, new TableStatistic(db.getTable(tableName).size()), tableName));
        }

        // Rule that must do
        HepProgram hepProgram = HepProgram.builder()
                // projection pushdown
                .addRuleInstance(FilterDistributiveRule.Config.DEFAULT.toRule())
                .addRuleInstance(CoreRules.FILTER_INTO_JOIN)
                .addRuleInstance(CoreRules.PROJECT_JOIN_TRANSPOSE)
                .addRuleInstance(CoreRules.PROJECT_FILTER_TRANSPOSE)
                .addRuleInstance(CoreRules.PROJECT_CORRELATE_TRANSPOSE)
                .build();
        RelOptPlanner hepPlanner = new HepPlanner(hepProgram);
        this.rboCluster = RelOptCluster.create(hepPlanner, new RexBuilder(this.typeFactory));

        // Initialize planner and cluster
        RelOptPlanner planner = new VolcanoPlanner();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);

        // Project
        // q9
        planner.addRule(CoreRules.PROJECT_MERGE);
        
        // Filter
        // capybara3
        planner.addRule(CoreRules.FILTER_AGGREGATE_TRANSPOSE);
        planner.addRule(CoreRules.FILTER_PROJECT_TRANSPOSE);
        // q9
        planner.addRule(CoreRules.FILTER_INTO_JOIN);
        // q19
        planner.addRule(FilterDistributiveRule.Config.DEFAULT.toRule());
        // q21
        planner.addRule(CoreRules.FILTER_CORRELATE);
        planner.addRule(FilterFlattenCorrelatedConditionRule.Config.DEFAULT.toRule());

        // Aggregate
        // q4
        planner.addRule(CoreRules.AGGREGATE_PROJECT_MERGE);
        // capybara3
        planner.addRule(CoreRules.AGGREGATE_EXPAND_DISTINCT_AGGREGATES);
        planner.addRule(CoreRules.AGGREGATE_REDUCE_FUNCTIONS);
        // q10 ?
        planner.addRule(CoreRules.AGGREGATE_ANY_PULL_UP_CONSTANTS);
        // q14 ?
        // planner.addRule(CoreRules.AGGREGATE_CASE_TO_FILTER);

        // Sort
        planner.addRule(CoreRules.SORT_PROJECT_TRANSPOSE);
        // planner.addRule(CoreRules.SORT_REMOVE_REDUNDANT);

        // Try reducing constant
        planner.addRule(CoreRules.PROJECT_REDUCE_EXPRESSIONS);
        // capybara1
        planner.addRule(CoreRules.FILTER_REDUCE_EXPRESSIONS);
        planner.addRule(CoreRules.JOIN_REDUCE_EXPRESSIONS);

        // Prune
        planner.addRule(PruneEmptyRules.AGGREGATE_INSTANCE);
        planner.addRule(PruneEmptyRules.EMPTY_TABLE_INSTANCE);
        planner.addRule(PruneEmptyRules.SORT_INSTANCE);
        planner.addRule(PruneEmptyRules.SORT_FETCH_ZERO_INSTANCE);
        planner.addRule(PruneEmptyRules.PROJECT_INSTANCE);
        planner.addRule(PruneEmptyRules.FILTER_INSTANCE);

        // // Join
        // planner.addRule(CoreRules.JOIN_COMMUTE);
        // planner.addRule(CoreRules.JOIN_ASSOCIATE);
        planner.addRule(CoreRules.JOIN_EXTRACT_FILTER);

        // Enumerable
        planner.addRule(CustomEnumerableJoinRule.DEFAULT_CONFIG.toRule());
        // planner.addRule(EnumerableRules.ENUMERABLE_JOIN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_SORT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_LIMIT_SORT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_FILTER_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_VALUES_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_AGGREGATE_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_CORRELATE_RULE);

        this.cboCluster = RelOptCluster.create(planner, new RexBuilder(this.typeFactory));

        this.isInitialized = true;
    }

    public EnumerableRel optimize(RelNode relNode) {
        // Go through RBO first
        // This is to mitigate the assumption that row size is not considered in the cost model
        RelOptPlanner planner = this.rboCluster.getPlanner();
        RelNode rboOptimizedNode = relNode;
        // Try to push down projection until it cannot be pushed
        // Let's get exhausted after 100 times
        for (int i = 0; i < 100; i++) {
            planner.setRoot(rboOptimizedNode);
            rboOptimizedNode = planner.findBestExp();
        }
        // Then, CBO
        RelNode cboOptimizedNode = optimizeHandler(rboOptimizedNode, false);

        // RelNode cboOptimizedNode = rboOptimizedNode;

        // // If still seeing NestedLoopJoin, try join enumeration
        // // Probably due to the cost model, join enumeration sometimes makes things worse
        // if (RelOptUtil.dumpPlan("", optimizedNode, SqlExplainFormat.TEXT, SqlExplainLevel.ALL_ATTRIBUTES).contains("NestedLoopJoin")) {
        //     System.out.println("Try join enumeration");
        //     // q9
        //     optimizedNode = optimizeHandler(relNode, true);
        // }

        return (EnumerableRel) cboOptimizedNode;
    }

    public EnumerableRel optimizeHandler(RelNode relNode, boolean enumeratingJoins) {
        RelOptPlanner planner = this.cboCluster.getPlanner();
        RelNode currentNode = relNode;

        // if (enumeratingJoins) {
        //     planner.addRule(CoreRules.JOIN_COMMUTE);
        //     planner.addRule(CoreRules.JOIN_ASSOCIATE);
        // }

        try {
            // Work really well for capybara1 and q21
            // Phase A1(a) - Volcano optimization
            currentNode = planner.changeTraits(currentNode,
                    currentNode.getTraitSet().replace(EnumerableConvention.INSTANCE));
            planner.setRoot(currentNode);
            RelNode withoutUnnesting = planner.findBestExp();
            // Phase A1(b) - Unnesting
            final RelBuilder relBuilder = RelBuilder.create(Frameworks.newConfigBuilder().build());
            currentNode = RelDecorrelator.decorrelateQuery(withoutUnnesting, relBuilder);
        } catch (Exception e) {
            // Fallback if cannot unnest after the first Volcano optimization (due to error)
            // q4 got ArrayIndexOutOfBound, q20 got NullPointerException
            
            // Phase A2 - Decorrelate first
            final RelBuilder relBuilder = RelBuilder.create(Frameworks.newConfigBuilder().build());
            currentNode = RelDecorrelator.decorrelateQuery(relNode, relBuilder);
        }

        // Phase B - Volcano optimization
        currentNode = planner.changeTraits(currentNode,
                currentNode.getTraitSet().replace(EnumerableConvention.INSTANCE));
        planner.setRoot(currentNode);
        RelNode optimizedNode = planner.findBestExp();

        // if (enumeratingJoins) {
        //     planner.removeRule(CoreRules.JOIN_COMMUTE);
        //     planner.removeRule(CoreRules.JOIN_ASSOCIATE);
        // }

        return (EnumerableRel) optimizedNode;
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
                config);

        SqlValidator validator = SqlValidatorUtil.newValidator(
                SqlStdOperatorTable.instance(),
                catalogReader,
                this.typeFactory,
                SqlValidator.Config.DEFAULT
                        .withDefaultNullCollation(config.defaultNullCollation())
                        .withConformance(config.conformance())
                        .withIdentifierExpansion(true));

        SqlParser parser = SqlParser.create(
                baseSql,
                SqlParser.config()
                        .withCaseSensitive(config.caseSensitive())
                        .withUnquotedCasing(config.unquotedCasing())
                        .withQuotedCasing(config.quotedCasing())
                        .withConformance(config.conformance()));

        SqlNode sqlNode = parser.parseStmt();
        SqlNode validatedSqlNode = validator.validate(sqlNode);

        SqlToRelConverter sql2rel = new SqlToRelConverter(
                NOOP_EXPANDER,
                validator,
                catalogReader,
                this.cboCluster,
                StandardConvertletTable.INSTANCE,
                SqlToRelConverter.config()
                        .withExpand(true));

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

    public String finalRewrite(String sql) {
        // // Avoid unnecessary casting
        // String rewritten = sql.replace(".00", "");
        // return rewritten;
        return sql;
    }
}
