# pg_parser_java

Java library for accessing the PostgreSQL parser outside of the server.

This library uses the base library [libpg_query](https://github.com/pganalyze/libpg_query#libpg_query)  

## Required tools before install
 * wget (or manual download of [libpg_query](https://github.com/jiangtao69039/libpg_query/archive/refs/tags/17-1229.zip) source code and unzip into target/downloads/ to avoid wget)
 * maven
 * java 8+

**For Docker build (recommended):**
 * docker

**For local build (without Docker):**
* make
* gcc
* protoc ( 3.21.12 for regenerating protobuf classes,sudo apt install protobuf-compiler, and protoc --version is same with protobuf-java in pom.xml)


## Installation

### Option 1: Build with Docker (Recommended)

Uses debian:10 container for better glibc compatibility across different Linux distributions.

```shell
docker build -t libpg-query-builder .
mvn clean package -Pdownload -PbuildLibDocker -DskipTests
```

### Option 2: Build locally

Uses your system's gcc and glibc. May have compatibility issues on other systems.

```shell
mvn clean package -Pdownload -PbuildLib -DskipTests
```

## Maven Profiles

| Profile | Description                                                                                         |
|---------|-----------------------------------------------------------------------------------------------------|
| `download` | Download [libpg_query](https://github.com/pganalyze/libpg_query) source code to `target/downloads/` |
| `buildLib` | Build `libpg_query.so` locally using system gcc/glibc                                               |
| `buildLibDocker` | Build `libpg_query.so` using Docker debian10     |
| `generateProtobuf` | Generate Java protobuf classes from `pg_query.proto` (requires `protoc`)                            |

### Examples

**Full build with Docker( for developer):**
```shell
mvn clean package -Pdownload -PbuildLibDocker -DskipTests
```  
  

## Test
```shell
mvn test 
```

## Usage: Split multiple sql statements
 See examples in docs/*.md
```java
@Test
public void test_parse_protobuf() throws PgQueryException {
    ParseResult result = PgQueryWrapper.pgQueryParseProtobuf("SELECT * FROM users WHERE id = 1");

    // 获取语句数量
    System.out.println("Statements count: " + result.getStmtsCount());

    // 获取第一个语句
    RawStmt rawStmt = result.getStmts(0);
    Node stmtNode = rawStmt.getStmt();

    // 检查是否为 SELECT 语句
    if (stmtNode.hasSelectStmt()) {
        SelectStmt selectStmt = stmtNode.getSelectStmt();

        // 获取 FROM 子句中的表名
        Node fromNode = selectStmt.getFromClause(0);
        if (fromNode.hasRangeVar()) {
            RangeVar rangeVar = fromNode.getRangeVar();
            System.out.println("Table name: " + rangeVar.getRelname());
            // 输出: Table name: users
        }
    }
}
```
## Authors
 - [jiangtao69039](https://github.com/jiangtao69039)

## Change log
 * 2024-12-30: Add Docker build profile with debian10 for glibc compatibility
 * 2024-12-30: Add protobuf support and Java bindings
 * 2022-12-02: add support of pg_query_split_with_parser
 * 2022-12-02: add support of pg_query_split_with_scanner
 * 2022-12-02: add support of pg_query_free_split_result