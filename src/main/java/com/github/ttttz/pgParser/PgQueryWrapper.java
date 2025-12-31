package com.github.ttttz.pgParser;

import com.github.ttttz.pgParser.deparse.PgQueryDeparseResult;
import com.github.ttttz.pgParser.deparse.PostgresDeparseOpts;
import com.github.ttttz.pgParser.parse.PgQueryParseResult;
import com.github.ttttz.pgParser.parse.PgQueryProtobuf;
import com.github.ttttz.pgParser.parse.PgQueryProtobufParseResult;
import com.github.ttttz.pgParser.split.PgQuerySplitResult;
import com.github.ttttz.pgParser.split.PgQuerySplitStmt;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level API for PostgreSQL SQL parsing
 */
public class PgQueryWrapper {

    private static final int POINTER_SIZE = Native.POINTER_SIZE;

    /**
     * Parse SQL and return JSON AST
     *
     * @param sql SQL statement
     * @return JSON string representing the parse tree
     * @throws PgQueryException if parsing fails
     */
    public static String parse(String sql) throws PgQueryException {
        PgQueryParseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_parse(sql);
        try {
            if (result.hasError()) {
                throw new PgQueryException(result.error.message, result.error.cursorpos);
            }
            return result.parse_tree;
        } finally {
            PgQueryLibInterface.INSTANCE.pg_query_free_parse_result(result);
        }
    }

    /**
     * Parse SQL and return strongly-typed ParseResult object
     *
     * @param sql SQL statement
     * @return ParseResult containing the AST as Java objects
     * @throws PgQueryException if parsing fails
     */
    public static pg_query.PgQuery.ParseResult parseTree(String sql) throws PgQueryException {
        PgQueryProtobufParseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_parse_protobuf(sql);
        try {
            if (result.hasError()) {
                throw new PgQueryException(result.error.message, result.error.cursorpos);
            }

            byte[] protobufBytes = result.getProtobufBytes();
            return pg_query.PgQuery.ParseResult.parseFrom(protobufBytes);

        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw new PgQueryException("Failed to parse protobuf: " + e.getMessage());
        } finally {
            PgQueryLibInterface.INSTANCE.pg_query_free_protobuf_parse_result(result);
        }
    }

    /**
     * Split SQL into individual statements
     *
     * @param sql SQL containing multiple statements
     * @return List of individual SQL statements
     * @throws PgQueryException if splitting fails
     */
    public static List<String> split(String sql) throws PgQueryException {
        PgQuerySplitResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_split_with_parser(sql);
        try {
            if (result.error != null && result.error.message != null) {
                throw new PgQueryException(result.error.message, result.error.cursorpos);
            }

            List<String> statements = new ArrayList<>();
            PointerByReference stmts = result.stmts;

            for (int i = 0; i < result.n_stmts; i++) {
                PgQuerySplitStmt.ByReference stmt = new PgQuerySplitStmt.ByReference(
                        stmts.getPointer().getPointer(i * POINTER_SIZE)
                );
                stmt.read();
                String statement = sql.substring(stmt.stmt_location, stmt.stmt_location + stmt.stmt_len).trim();
                statements.add(statement);
            }

            return statements;
        } finally {
            PgQueryLibInterface.INSTANCE.pg_query_free_split_result(result);
        }
    }

    /**
     * Deparse protobuf parse tree back to SQL string
     *
     * @param parseResult ParseResult from parseTree()
     * @return SQL string
     * @throws PgQueryException if deparsing fails
     */
    public static String deparse(pg_query.PgQuery.ParseResult parseResult) throws PgQueryException {
        byte[] protobufBytes = parseResult.toByteArray();
        PgQueryProtobuf.ByValue protobuf = createProtobuf(protobufBytes);

        PgQueryDeparseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_deparse_protobuf(protobuf);
        try {
            if (result.hasError()) {
                throw new PgQueryException(result.error.message, result.error.cursorpos);
            }
            return result.query;
        } finally {
            PgQueryLibInterface.INSTANCE.pg_query_free_deparse_result(result);
        }
    }

    /**
     * Deparse protobuf parse tree back to SQL string with options
     *
     * @param parseResult ParseResult from parseTree()
     * @param opts        Deparse options
     * @return SQL string
     * @throws PgQueryException if deparsing fails
     */
    public static String deparseWithOpts(pg_query.PgQuery.ParseResult parseResult, PostgresDeparseOpts.ByValue opts) throws PgQueryException {
        byte[] protobufBytes = parseResult.toByteArray();
        PgQueryProtobuf.ByValue protobuf = createProtobuf(protobufBytes);

        PgQueryDeparseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_deparse_protobuf_opts(protobuf, opts);
        try {
            if (result.hasError()) {
                throw new PgQueryException(result.error.message, result.error.cursorpos);
            }
            return result.query;
        } finally {
            PgQueryLibInterface.INSTANCE.pg_query_free_deparse_result(result);
        }
    }

    /**
     * Deparse protobuf bytes back to SQL string
     *
     * @param protobufBytes Protobuf bytes
     * @return SQL string
     * @throws PgQueryException if deparsing fails
     */
    public static String deparseFromBytes(byte[] protobufBytes) throws PgQueryException {
        PgQueryProtobuf.ByValue protobuf = createProtobuf(protobufBytes);

        PgQueryDeparseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_deparse_protobuf(protobuf);
        try {
            if (result.hasError()) {
                throw new PgQueryException(result.error.message, result.error.cursorpos);
            }
            return result.query;
        } finally {
            PgQueryLibInterface.INSTANCE.pg_query_free_deparse_result(result);
        }
    }

    /**
     * Helper method to create PgQueryProtobuf.ByValue from byte array
     */
    private static PgQueryProtobuf.ByValue createProtobuf(byte[] bytes) {
        PgQueryProtobuf.ByValue protobuf = new PgQueryProtobuf.ByValue();
        protobuf.len = bytes.length;
        protobuf.data = new Memory(bytes.length);
        protobuf.data.write(0, bytes, 0, bytes.length);
        return protobuf;
    }

    /**
     * Default PostgreSQL version for deparse operations
     */
    private static final int DEFAULT_PG_VERSION = 170007;

    /**
     * Deparse a single Node back to SQL string
     * <p>
     * This is useful for extracting and deparsing sub-statements, such as
     * the SELECT clause from a CREATE VIEW or CREATE MATERIALIZED VIEW statement.
     * </p>
     *
     * @param node The AST node to deparse (e.g., a SelectStmt node)
     * @return SQL string
     * @throws PgQueryException if deparsing fails
     */
    public static String deparseNode(pg_query.PgQuery.Node node) throws PgQueryException {
        return deparseNode(node, DEFAULT_PG_VERSION);
    }

    /**
     * Deparse a single Node back to SQL string with specified PostgreSQL version
     *
     * @param node    The AST node to deparse
     * @param version PostgreSQL version number (e.g., 170007 for PG 17)
     * @return SQL string
     * @throws PgQueryException if deparsing fails
     */
    public static String deparseNode(pg_query.PgQuery.Node node, int version) throws PgQueryException {
        pg_query.PgQuery.ParseResult parseResult = pg_query.PgQuery.ParseResult.newBuilder()
                .setVersion(version)
                .addStmts(pg_query.PgQuery.RawStmt.newBuilder()
                        .setStmt(node)
                        .build())
                .build();
        return deparse(parseResult);
    }

    /**
     * Deparse a single Node back to SQL string with options
     *
     * @param node The AST node to deparse
     * @param opts Deparse options (pretty print, indentation, etc.)
     * @return SQL string
     * @throws PgQueryException if deparsing fails
     */
    public static String deparseNodeWithOpts(pg_query.PgQuery.Node node, PostgresDeparseOpts.ByValue opts) throws PgQueryException {
        return deparseNodeWithOpts(node, DEFAULT_PG_VERSION, opts);
    }

    /**
     * Deparse a single Node back to SQL string with version and options
     *
     * @param node    The AST node to deparse
     * @param version PostgreSQL version number
     * @param opts    Deparse options
     * @return SQL string
     * @throws PgQueryException if deparsing fails
     */
    public static String deparseNodeWithOpts(pg_query.PgQuery.Node node, int version, PostgresDeparseOpts.ByValue opts) throws PgQueryException {
        pg_query.PgQuery.ParseResult parseResult = pg_query.PgQuery.ParseResult.newBuilder()
                .setVersion(version)
                .addStmts(pg_query.PgQuery.RawStmt.newBuilder()
                        .setStmt(node)
                        .build())
                .build();
        return deparseWithOpts(parseResult, opts);
    }
}
