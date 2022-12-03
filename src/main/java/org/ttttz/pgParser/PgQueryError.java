package org.ttttz.pgParser;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class PgQueryError extends Structure {

    public static class ByReference extends PgQueryError implements Structure.ByReference {
    }

    public static class ByValue extends PgQueryError implements Structure.ByValue {
    }

    public String message; // exception message
    public String funcname; // source function of exception (e.g. SearchSysCache)
    public String filename; // source of exception (e.g. parse.l)
    public int lineno; // source of exception (e.g. 104)
    public int cursorpos; // char in query at which exception occurred
    public String context; // additional context (optional, can be NULL)

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("message", "funcname", "filename", "lineno", "cursorpos", "context");
    }
}
