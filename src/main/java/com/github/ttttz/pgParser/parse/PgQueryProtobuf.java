package com.github.ttttz.pgParser.parse;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Maps to C struct:
 * typedef struct {
 *   size_t len;
 *   char* data;
 * } PgQueryProtobuf;
 */
public class PgQueryProtobuf extends Structure {

    public static class ByReference extends PgQueryProtobuf implements Structure.ByReference {
    }

    public static class ByValue extends PgQueryProtobuf implements Structure.ByValue {
    }

    public long len;      // size_t
    public Pointer data;  // char*

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("len", "data");
    }

    /**
     * Get the protobuf binary data as byte array
     */
    public byte[] getBytes() {
        if (data == null || len == 0) {
            return new byte[0];
        }
        return data.getByteArray(0, (int) len);
    }
}
