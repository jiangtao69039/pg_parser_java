package com.github.ttttz.pgParser;

import com.github.ttttz.pgParser.parse.PgQueryParseResult;
import com.github.ttttz.pgParser.parse.PgQueryProtobufParseResult;
import com.github.ttttz.pgParser.split.PgQuerySplitResult;
import com.sun.jna.Library;
import com.sun.jna.Native;

import java.util.Objects;

public interface PgQueryLibInterface extends Library {

    String libPath = Objects.requireNonNull(PgQueryLibInterface.class.getClassLoader().getResource("libpg_query.so")).getFile();

    PgQueryLibInterface INSTANCE = Native.load(libPath, PgQueryLibInterface.class);

    // ============== Split functions ==============
    PgQuerySplitResult.ByValue pg_query_split_with_scanner(String input);

    PgQuerySplitResult.ByValue pg_query_split_with_parser(String input);

    void pg_query_free_split_result(PgQuerySplitResult.ByValue result);

    // ============== Parse functions (JSON output) ==============
    /**
     * Parse SQL and return JSON format AST
     * @param input SQL string
     * @return PgQueryParseResult with parse_tree as JSON string
     */
    PgQueryParseResult.ByValue pg_query_parse(String input);

    /**
     * Parse SQL with options
     * @param input SQL string
     * @param parser_options parser options (see pg_query.h)
     * @return PgQueryParseResult
     */
    PgQueryParseResult.ByValue pg_query_parse_opts(String input, int parser_options);

    void pg_query_free_parse_result(PgQueryParseResult.ByValue result);

    // ============== Parse functions (Protobuf output) ==============
    /**
     * Parse SQL and return Protobuf format AST
     * @param input SQL string
     * @return PgQueryProtobufParseResult with parse_tree as protobuf binary
     */
    PgQueryProtobufParseResult.ByValue pg_query_parse_protobuf(String input);

    /**
     * Parse SQL with options and return Protobuf format AST
     * @param input SQL string
     * @param parser_options parser options
     * @return PgQueryProtobufParseResult
     */
    PgQueryProtobufParseResult.ByValue pg_query_parse_protobuf_opts(String input, int parser_options);

    void pg_query_free_protobuf_parse_result(PgQueryProtobufParseResult.ByValue result);
}
