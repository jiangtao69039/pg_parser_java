package com.github.ttttz.pgParser.split;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class PgQuerySplitStmt extends Structure {

    public static class ByReference extends PgQuerySplitStmt implements Structure.ByReference {

        public ByReference(Pointer value) {
            super(value);
        }
    }

    public static class ByValue extends PgQuerySplitStmt implements Structure.ByValue {
    }


    public PgQuerySplitStmt() {
    }

    public PgQuerySplitStmt(Pointer p) {
        super(p);
        //read();
    }

    public int stmt_location;
    public int stmt_len;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("stmt_location", "stmt_len");
    }

}
