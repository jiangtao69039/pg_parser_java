package org.ttttz.pgParser;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;

import java.util.Objects;

public interface PgQueryLibInterface extends Library {

    String libPath = Objects.requireNonNull(PgQueryLibInterface.class.getClassLoader().getResource("libpg_query.so")).getFile();

    PgQueryLibInterface INSTANCE = Native.load(libPath, PgQueryLibInterface.class);

    PgQuerySplitResult.ByValue pg_query_split_with_scanner(String input);

    PgQuerySplitResult.ByValue pg_query_split_with_parser(String input);

    void pg_query_free_split_result(PgQuerySplitResult.ByValue result);
}

