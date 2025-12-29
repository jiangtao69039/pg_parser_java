package com.github.ttttz.pgParser.parse;

import com.github.ttttz.pgParser.PgQueryError;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Maps to C struct:
 * typedef struct {
 *   char* parse_tree;      // JSON string
 *   char* stderr_buffer;
 *   PgQueryError* error;
 * } PgQueryParseResult;
 */
public class PgQueryParseResult extends Structure {

    public static class ByReference extends PgQueryParseResult implements Structure.ByReference {
    }

    public static class ByValue extends PgQueryParseResult implements Structure.ByValue {
    }

    public String parse_tree;      // JSON format parse tree
    public String stderr_buffer;
    public PgQueryError.ByReference error;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("parse_tree", "stderr_buffer", "error");
    }

    public boolean hasError() {
        return error != null && error.message != null;
    }

    public String getErrorMessage() {
        if (error == null) return null;
        return error.message;
    }
}
