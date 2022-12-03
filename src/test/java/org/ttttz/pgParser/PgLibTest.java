package org.ttttz.pgParser;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.PointerByReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PgLibTest {

    @Test
    public void test_split(){
        String input = "select * from t;select * from t;";
        PgQuerySplitResult.ByValue byValue = PgQueryLibInterface
                .INSTANCE
                .pg_query_split_with_parser(input);
        PointerByReference stmts = byValue.stmts;
        int pointIndex = 0;
        int pointSize = 8; //bytes
        for(int i=0;i<byValue.n_stmts;i++){
            pointIndex = i*pointSize;
            PgQuerySplitStmt.ByReference pgQuerySplitStmt = new PgQuerySplitStmt.ByReference(stmts.getPointer().getPointer(pointIndex));
            pgQuerySplitStmt.read();
            String split = input.substring(pgQuerySplitStmt.stmt_location, pgQuerySplitStmt.stmt_location+ pgQuerySplitStmt.stmt_len);
            assertEquals("select * from t",split);
        }
        PgQueryLibInterface
                .INSTANCE.pg_query_free_split_result(byValue);
    }
}
