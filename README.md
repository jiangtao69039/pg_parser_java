# pg_parser_java

Java library for accessing the PostgreSQL parser outside of the server.

This library uses the base library [libpg_query](https://github.com/pganalyze/libpg_query#libpg_query)  

## Required tools before install
 * wget
 * maven
 * java 8+

**For local build (without Docker):**
 * make
 * gcc
 * protoc (for regenerating protobuf classes,sudo apt install protobuf-compiler)

**For Docker build (recommended):**
 * docker

## Installation

### Option 1: Build with Docker (Recommended)

Uses debian:10 container for better glibc compatibility across different Linux distributions.

```shell
docker build -t libpg-query-builder .
mvn clean package -Pdownload -PbuildLibDocker -PgenerateProtobuf  -DskipTests
```

### Option 2: Build locally

Uses your system's gcc and glibc. May have compatibility issues on other systems.

```shell
mvn clean package -Pdownload -PbuildLib -PgenerateProtobuf  -DskipTests
```

## Maven Profiles

| Profile | Description |
|---------|-------------|
| `download` | Download [libpg_query](https://github.com/pganalyze/libpg_query) source code to `target/downloads/` |
| `buildLib` | Build `libpg_query.so` locally using system gcc/glibc |
| `buildLibDocker` | Build `libpg_query.so` using Docker CentOS 7 (glibc 2.17, best compatibility) |
| `generateProtobuf` | Generate Java protobuf classes from `pg_query.proto` (requires `protoc`) |

### Examples

**Full build with Docker:**
```shell
mvn clean package -Pdownload -PbuildLibDocker -PgenerateProtobuf
```  
  

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
 * 2024-12-30: Add Docker build profile with CentOS 7 for glibc compatibility
 * 2024-12-30: Add protobuf support and Java bindings
 * 2022-12-02: add support of pg_query_split_with_parser
 * 2022-12-02: add support of pg_query_split_with_scanner
 * 2022-12-02: add support of pg_query_free_split_result