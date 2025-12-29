package com.github.ttttz.pgParser;

import com.github.ttttz.pgParser.parse.PgQueryParseResult;
import com.github.ttttz.pgParser.split.PgQuerySplitResult;
import com.github.ttttz.pgParser.split.PgQuerySplitStmt;
import com.sun.jna.ptr.PointerByReference;
import org.junit.jupiter.api.Test;
import pg_query.PgQuery.ParseResult;
import pg_query.PgQuery.RawStmt;
import pg_query.PgQuery.Node;
import pg_query.PgQuery.SelectStmt;
import pg_query.PgQuery.RangeVar;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PgLibTest {

    @Test
    public void test_split() {
        String input = "select * from t;select * from t;";
        PgQuerySplitResult.ByValue byValue = PgQueryLibInterface
                .INSTANCE
                .pg_query_split_with_parser(input);
        PointerByReference stmts = byValue.stmts;
        int pointIndex = 0;
        int pointSize = 8; //bytes
        for (int i = 0; i < byValue.n_stmts; i++) {
            pointIndex = i * pointSize;
            PgQuerySplitStmt.ByReference pgQuerySplitStmt = new PgQuerySplitStmt.ByReference(stmts.getPointer().getPointer(pointIndex));
            pgQuerySplitStmt.read();
            String split = input.substring(pgQuerySplitStmt.stmt_location, pgQuerySplitStmt.stmt_location + pgQuerySplitStmt.stmt_len);
            assertEquals("select * from t", split);
        }
        PgQueryLibInterface
                .INSTANCE.pg_query_free_split_result(byValue);
    }

    @Test
    public void test_parse_simple() {
        String sql = "SELECT 1";
        PgQueryParseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_parse(sql);

        assertFalse(result.hasError(), "Should not have error");
        assertNotNull(result.parse_tree, "parse_tree should not be null");
        assertTrue(result.parse_tree.contains("SelectStmt"), "Should contain SelectStmt");
        System.out.println("Parse result: " + result.parse_tree);

        PgQueryLibInterface.INSTANCE.pg_query_free_parse_result(result);
    }

    @Test
    public void test_parse_select_from() {
        String sql = "SELECT * FROM users WHERE id = 1";
        PgQueryParseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_parse(sql);

        assertFalse(result.hasError());
        assertTrue(result.parse_tree.contains("\"relname\":\"users\""));
        System.out.println("Parse result: " + result.parse_tree);

        PgQueryLibInterface.INSTANCE.pg_query_free_parse_result(result);
    }

    @Test
    public void test_parse_insert() {
        String sql = "INSERT INTO orders (product, qty) VALUES ('apple', 10)";
        PgQueryParseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_parse(sql);

        assertFalse(result.hasError());
        assertTrue(result.parse_tree.contains("InsertStmt"));
        assertTrue(result.parse_tree.contains("\"relname\":\"orders\""));
        System.out.println("Parse result: " + result.parse_tree);

        PgQueryLibInterface.INSTANCE.pg_query_free_parse_result(result);
    }

    @Test
    public void test_parse_error() {
        String sql = "SELECT * FROM";  // incomplete SQL
        PgQueryParseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_parse(sql);

        assertTrue(result.hasError(), "Should have parse error");
        assertNotNull(result.getErrorMessage());
        System.out.println("Error message: " + result.getErrorMessage());

        PgQueryLibInterface.INSTANCE.pg_query_free_parse_result(result);
    }

    // ============== High-level API tests (JSON) ==============

    @Test
    public void test_PgQuery_parse() throws PgQueryException {
        String json = com.github.ttttz.pgParser.PgQuery.parse("SELECT * FROM users");

        assertNotNull(json);
        assertTrue(json.contains("SelectStmt"));
        assertTrue(json.contains("\"relname\":\"users\""));
        System.out.println("PgQuery.parse result: " + json);
    }

    @Test
    public void test_PgQuery_parse_error() {
        PgQueryException exception = assertThrows(PgQueryException.class, () -> {
            com.github.ttttz.pgParser.PgQuery.parse("SELECT * FROM");
        });
        System.out.println("Exception: " + exception.getMessage());
    }

    @Test
    public void test_PgQuery_split() throws PgQueryException {
        List<String> statements = com.github.ttttz.pgParser.PgQuery.split("SELECT 1; SELECT 2; SELECT 3;");

        assertEquals(3, statements.size());
        assertEquals("SELECT 1", statements.get(0));
        assertEquals("SELECT 2", statements.get(1));
        assertEquals("SELECT 3", statements.get(2));
    }

    // ============== Protobuf API tests ==============

    @Test
    public void test_parseTree_simple() throws PgQueryException {
        ParseResult result = com.github.ttttz.pgParser.PgQuery.parseTree("SELECT 1");

        assertNotNull(result);
        assertEquals(1, result.getStmtsCount());
        System.out.println("ParseResult version: " + result.getVersion());
        System.out.println("Statements count: " + result.getStmtsCount());
    }

    @Test
    public void test_parseTree_select_from() throws PgQueryException {
        ParseResult result = com.github.ttttz.pgParser.PgQuery.parseTree("SELECT * FROM users WHERE id = 1");

        assertEquals(1, result.getStmtsCount());

        RawStmt rawStmt = result.getStmts(0);
        Node stmtNode = rawStmt.getStmt();

        // Check it's a SelectStmt
        assertTrue(stmtNode.hasSelectStmt(), "Should be a SelectStmt");

        SelectStmt selectStmt = stmtNode.getSelectStmt();

        // Check FROM clause
        assertEquals(1, selectStmt.getFromClauseCount());
        Node fromNode = selectStmt.getFromClause(0);
        assertTrue(fromNode.hasRangeVar(), "FROM clause should be RangeVar");

        RangeVar rangeVar = fromNode.getRangeVar();
        assertEquals("users", rangeVar.getRelname());

        System.out.println("Table name: " + rangeVar.getRelname());
    }

    @Test
    public void test_parseTree_insert() throws PgQueryException {
        ParseResult result = com.github.ttttz.pgParser.PgQuery.parseTree(
                "INSERT INTO orders (product, qty) VALUES ('apple', 10)"
        );

        assertEquals(1, result.getStmtsCount());

        RawStmt rawStmt = result.getStmts(0);
        Node stmtNode = rawStmt.getStmt();

        assertTrue(stmtNode.hasInsertStmt(), "Should be an InsertStmt");

        pg_query.PgQuery.InsertStmt insertStmt = stmtNode.getInsertStmt();
        RangeVar relation = insertStmt.getRelation();

        assertEquals("orders", relation.getRelname());
        System.out.println("Insert into table: " + relation.getRelname());

        // Check columns
        assertEquals(2, insertStmt.getColsCount());
        System.out.println("Columns count: " + insertStmt.getColsCount());
    }

    @Test
    public void test_parseTree_multiple_tables() throws PgQueryException {
        ParseResult result = com.github.ttttz.pgParser.PgQuery.parseTree(
                "SELECT * FROM users u JOIN orders o ON u.id = o.user_id"
        );

        RawStmt rawStmt = result.getStmts(0);
        SelectStmt selectStmt = rawStmt.getStmt().getSelectStmt();

        // FROM clause should contain a JoinExpr
        assertEquals(1, selectStmt.getFromClauseCount());
        Node fromNode = selectStmt.getFromClause(0);

        assertTrue(fromNode.hasJoinExpr(), "Should have JoinExpr");
        System.out.println("Has JOIN expression: true");
    }

    @Test
    public void test_parseTree_error() {
        PgQueryException exception = assertThrows(PgQueryException.class, () -> {
            com.github.ttttz.pgParser.PgQuery.parseTree("SELECT * FROM");
        });
        System.out.println("Protobuf parse exception: " + exception.getMessage());
    }
}
