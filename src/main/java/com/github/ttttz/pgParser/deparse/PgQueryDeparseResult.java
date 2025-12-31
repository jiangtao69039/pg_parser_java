package com.github.ttttz.pgParser.deparse;

import com.github.ttttz.pgParser.PgQueryError;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Maps to C struct:
 * typedef struct {
 *   char* query;
 *   PgQueryError* error;
 * } PgQueryDeparseResult;
 */
public class PgQueryDeparseResult extends Structure {

    public static class ByReference extends PgQueryDeparseResult implements Structure.ByReference {
    }

    public static class ByValue extends PgQueryDeparseResult implements Structure.ByValue {
    }

    public String query;
    public PgQueryError.ByReference error;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("query", "error");
    }

    public boolean hasError() {
        return error != null && error.message != null;
    }

    public String getErrorMessage() {
        return error != null ? error.message : null;
    }
}
