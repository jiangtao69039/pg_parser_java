# pg_parser_java

Java library for accessing the PostgreSQL parser outside of the server.

This library uses the base library [libpg_query](https://github.com/pganalyze/libpg_query#libpg_query)  

## Required tools before install
 * wget
 * make
 * gcc
 * maven
 * java 8+
 
**This was built under the following conditions.**
1. open jdk 8u302
2. GNU Make 4.3
3. gcc 12.2.0
4. maven 3.8.6
5. OS: arch linux x86_64

**In addition**: It's works fine on openjdk-8-slim-buster docker image. But you must build the libpg_query.so in the image to avoid GBLIC version differences.

  
This was tested almost not at all. Good luck!

## Installation
```shell
mvn clean package -Pdownload -PbuildLib
```

-Pdownload: download [libpg_query](https://github.com/pganalyze/libpg_query#libpg_query) source code into target/downloads/   
-PbuildLib: generate libpg_query.so into src/main/resource/libpg_query.so  
  

## Test
```shell
mvn test 
```

## Usage: Split multiple sql statements
```java
import com.github.ttttz.pgParser.split.PgQuerySplitResult;
import com.github.ttttz.pgParser.split.PgQuerySplitStmt;
import com.sun.jna.ptr.PointerByReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

```

## Authors
 - [jiangtao69039](https://github.com/jiangtao69039)

## Change log
 * add support of pg_query_split_with_parser
 * add support of pg_query_split_with_scanner
 * add support of pg_query_free_split_result