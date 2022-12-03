package org.ttttz.pgParser;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

public class PgQuerySplitResult extends Structure {

    public static class ByReference extends PgQuerySplitResult implements Structure.ByReference {
    }

    public static class ByValue extends PgQuerySplitResult implements Structure.ByValue {
    }

    public PgQuerySplitResult() {

    }

    protected PgQuerySplitResult(Pointer p) {
        super(p);
        //read();
    }

    public PointerByReference stmts;
    //public PgQuerySplitStmt.ByReference stmts;
    public int n_stmts;
    public String stderr_buffer;
    public PgQueryError.ByReference error;

    /*PgQuerySplitStmt **stmts;
    int n_stmts;
    char* stderr_buffer;
    PgQueryError* error;*/

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("stmts", "n_stmts", "stderr_buffer", "error");
    }
}
