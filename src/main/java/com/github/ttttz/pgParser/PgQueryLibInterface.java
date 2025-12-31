package com.github.ttttz.pgParser;

import com.github.ttttz.pgParser.parse.PgQueryParseResult;
import com.github.ttttz.pgParser.parse.PgQueryProtobufParseResult;
import com.github.ttttz.pgParser.split.PgQuerySplitResult;
import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public interface PgQueryLibInterface extends Library {

    String libPath = extractNativeLibrary();

    PgQueryLibInterface INSTANCE = Native.load(libPath, PgQueryLibInterface.class);

    /**
     * Extract native library from JAR to temp file.
     */
    static String extractNativeLibrary() {
        String libName = "libpg_query.so";
        try (InputStream is = PgQueryLibInterface.class.getClassLoader().getResourceAsStream(libName)) {
            Objects.requireNonNull(is, "Native library not found: " + libName);

            File tempFile = File.createTempFile("libpg_query", ".so");
            tempFile.deleteOnExit();

            try (OutputStream os = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract native library: " + libName, e);
        }
    }

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
