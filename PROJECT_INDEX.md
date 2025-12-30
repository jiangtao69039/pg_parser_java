# Project Index: pg_parser_java

Generated: 2025-12-30

## Project Overview

Java library for accessing the PostgreSQL parser outside of the server using JNA to call libpg_query native library.

| Item | Value |
|------|-------|
| GroupId | `com.github.ttttz` |
| ArtifactId | `pg-parser-java` |
| Version | `1.0` |
| Java | 8+ |
| libpg_query | 17-1229 |

## Project Structure

```
pg_parser_java/
├── src/
│   ├── main/java/
│   │   ├── com/github/ttttz/pgParser/
│   │   │   ├── PgQueryWrapper.java      # High-level API (recommended)
│   │   │   ├── PgQueryLibInterface.java # JNA native interface
│   │   │   ├── PgQueryError.java        # Error structure
│   │   │   ├── PgQueryException.java    # Exception class
│   │   │   ├── parse/
│   │   │   │   ├── PgQueryParseResult.java
│   │   │   │   ├── PgQueryProtobuf.java
│   │   │   │   └── PgQueryProtobufParseResult.java
│   │   │   └── split/
│   │   │       ├── PgQuerySplitResult.java
│   │   │       └── PgQuerySplitStmt.java
│   │   └── pg_query/
│   │       └── PgQuery.java             # Protobuf generated (7000+ lines)
│   ├── main/resources/
│   │   └── libpg_query.so               # Native library (~7MB)
│   └── test/java/
│       └── com/github/ttttz/pgParser/
│           └── PgLibTest.java
├── docs/
│   ├── 简介.md
│   └── 当前支持方法.md
├── pom.xml
├── Dockerfile
└── README.md
```

## API Reference

### High-level API: PgQueryWrapper (Recommended)

| Method | Return | Description |
|--------|--------|-------------|
| `parse(sql)` | `String` | Parse SQL → JSON AST |
| `parseTree(sql)` | `ParseResult` | Parse SQL → Protobuf object |
| `split(sql)` | `List<String>` | Split multi-statement SQL |

```java
// Usage
String json = PgQueryWrapper.parse("SELECT * FROM users");
ParseResult tree = PgQueryWrapper.parseTree("SELECT * FROM users");
List<String> stmts = PgQueryWrapper.split("SELECT 1; SELECT 2;");
```

### Low-level API: PgQueryLibInterface

| Method | Description | Must Free |
|--------|-------------|-----------|
| `pg_query_parse(input)` | Parse → JSON | `pg_query_free_parse_result` |
| `pg_query_parse_opts(input, opts)` | Parse with options | `pg_query_free_parse_result` |
| `pg_query_parse_protobuf(input)` | Parse → Protobuf | `pg_query_free_protobuf_parse_result` |
| `pg_query_parse_protobuf_opts(input, opts)` | Parse with options | `pg_query_free_protobuf_parse_result` |
| `pg_query_split_with_scanner(input)` | Split (lexer) | `pg_query_free_split_result` |
| `pg_query_split_with_parser(input)` | Split (parser) | `pg_query_free_split_result` |

## Build Commands

```bash
# Full build with Docker (recommended)
docker build -t libpg-query-builder .
mvn clean package -Pdownload -PbuildLibDocker -DskipTests

# Local build (may have glibc issues)
mvn clean package -Pdownload -PbuildLib -DskipTests

# Regenerate protobuf classes
mvn generate-sources -Pdownload -PgenerateProtobuf

# Run tests
mvn test

# Deploy to private repo
mvn deploy -DskipTests
```

## Maven Profiles

| Profile | Phase | Description |
|---------|-------|-------------|
| `download` | initialize | Download libpg_query source from GitHub |
| `buildLib` | generate-sources | Build .so locally with system gcc |
| `buildLibDocker` | generate-sources | Build .so in Docker (Debian 10, glibc 2.28) |
| `generateProtobuf` | generate-sources | Generate Java from pg_query.proto |

## Dependencies

| Dependency | Version | Scope |
|------------|---------|-------|
| `net.java.dev.jna:jna` | 5.18.1 | compile |
| `com.google.protobuf:protobuf-java` | 3.21.12 | compile |
| `org.junit.jupiter:junit-jupiter-engine` | 5.9.0 | test |

## Class Loading Behavior

```
Dependency only (no method call)
└── PgQueryWrapper class NOT loaded
    └── PgQueryLibInterface NOT loaded
        └── Native.load() NOT called
            └── libpg_query.so NOT loaded
            └── ✓ Safe, no JVM risk

Method call: PgQueryWrapper.parse()
└── PgQueryWrapper class loaded
    └── POINTER_SIZE = Native.POINTER_SIZE (safe, JNA constant)
    └── PgQueryLibInterface.INSTANCE accessed
        └── Native.load(libpg_query.so) called
            └── Native library loaded
            └── ⚠ Potential JVM crash risk
```

## Use Cases

- SQL 语句分句 (Split multi-statement SQL)
- SQL 解析为 AST (Parse SQL to AST)
- 提取表名/列名/变量 (Extract table/column names)
- SQL 语法校验 (SQL syntax validation)
- SQL 格式化 (SQL formatting)
- SQL 改写 (SQL rewriting)
