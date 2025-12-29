package com.github.ttttz.pgParser.parse;

import com.github.ttttz.pgParser.PgQueryError;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Maps to C struct:
 * typedef struct {
 *   PgQueryProtobuf parse_tree;
 *   char* stderr_buffer;
 *   PgQueryError* error;
 * } PgQueryProtobufParseResult;
 */
public class PgQueryProtobufParseResult extends Structure {

    public static class ByReference extends PgQueryProtobufParseResult implements Structure.ByReference {
    }

    public static class ByValue extends PgQueryProtobufParseResult implements Structure.ByValue {
    }

    public PgQueryProtobuf parse_tree;  // Embedded struct (not pointer)
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

    /**
     * Get the protobuf binary data
     */
    public byte[] getProtobufBytes() {
        return parse_tree.getBytes();
    }
}
