package com.github.ttttz.pgParser;

import com.github.ttttz.pgParser.parse.PgQueryParseResult;
import com.github.ttttz.pgParser.parse.PgQueryProtobufParseResult;
import com.github.ttttz.pgParser.split.PgQuerySplitResult;
import com.github.ttttz.pgParser.split.PgQuerySplitStmt;
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
}
