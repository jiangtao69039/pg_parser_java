# pg_parser_java API Reference

## Overview

pg_parser_java 提供两层 API：
- **High-level API** (`PgQueryWrapper`) - 推荐使用，自动管理内存
- **Low-level API** (`PgQueryLibInterface`) - 直接调用 native 方法，需手动释放内存

---

## High-level API

### PgQueryWrapper

位置: `com.github.ttttz.pgParser.PgQueryWrapper`

封装了对 libpg_query 的调用，自动处理内存释放和错误转换。

#### parse(String sql)

解析 SQL 语句，返回 JSON 格式的语法树。

```java
public static String parse(String sql) throws PgQueryException
```

**参数:**
- `sql` - SQL 语句字符串

**返回:**
- JSON 格式的语法树字符串

**异常:**
- `PgQueryException` - SQL 语法错误时抛出

**示例:**
```java
String json = PgQueryWrapper.parse("SELECT * FROM users WHERE id = 1");
// {"version":170007,"stmts":[{"stmt":{"SelectStmt":{...}}}]}
```

---

#### parseTree(String sql)

解析 SQL 语句，返回强类型的 Protobuf 对象。

```java
public static pg_query.PgQuery.ParseResult parseTree(String sql) throws PgQueryException
```

**参数:**
- `sql` - SQL 语句字符串

**返回:**
- `ParseResult` Protobuf 对象，可直接访问语法树节点

**异常:**
- `PgQueryException` - SQL 语法错误或 Protobuf 解析失败时抛出

**示例:**
```java
ParseResult result = PgQueryWrapper.parseTree("SELECT * FROM users");

// 获取语句数量
int count = result.getStmtsCount();

// 获取第一个语句
RawStmt rawStmt = result.getStmts(0);
Node stmtNode = rawStmt.getStmt();

// 判断语句类型
if (stmtNode.hasSelectStmt()) {
    SelectStmt select = stmtNode.getSelectStmt();
    // 获取表名
    RangeVar table = select.getFromClause(0).getRangeVar();
    String tableName = table.getRelname();  // "users"
}
```

---

#### split(String sql)

将多条 SQL 语句分割为列表。

```java
public static List<String> split(String sql) throws PgQueryException
```

**参数:**
- `sql` - 包含多条 SQL 语句的字符串（以分号分隔）

**返回:**
- SQL 语句列表，每个元素为单条语句（已 trim）

**异常:**
- `PgQueryException` - 分割失败时抛出

**示例:**
```java
List<String> stmts = PgQueryWrapper.split("SELECT 1; SELECT 2; SELECT 3;");
// ["SELECT 1", "SELECT 2", "SELECT 3"]
```

---

### PgQueryException

位置: `com.github.ttttz.pgParser.PgQueryException`

SQL 解析失败时抛出的异常。

#### 构造方法

```java
public PgQueryException(String message)
public PgQueryException(String message, int cursorPosition)
```

#### 方法

| 方法 | 返回类型 | 描述 |
|------|----------|------|
| `getMessage()` | `String` | 错误信息（包含位置） |
| `getCursorPosition()` | `int` | 错误发生位置（-1 表示未知） |

**示例:**
```java
try {
    PgQueryWrapper.parse("SELECT * FROM");
} catch (PgQueryException e) {
    System.out.println(e.getMessage());        // "syntax error at end of input at position 14"
    System.out.println(e.getCursorPosition()); // 14
}
```

---

## Low-level API

### PgQueryLibInterface

位置: `com.github.ttttz.pgParser.PgQueryLibInterface`

JNA 接口，直接映射 libpg_query C 函数。

> **注意:** 使用底层 API 必须手动调用对应的 `pg_query_free_*` 方法释放内存。

#### 获取实例

```java
PgQueryLibInterface lib = PgQueryLibInterface.INSTANCE;
```

---

#### Parse 方法

##### pg_query_parse

```java
PgQueryParseResult.ByValue pg_query_parse(String input)
```

解析 SQL，返回 JSON 格式结果。

**必须调用:** `pg_query_free_parse_result(result)` 释放内存

---

##### pg_query_parse_opts

```java
PgQueryParseResult.ByValue pg_query_parse_opts(String input, int parser_options)
```

带选项解析 SQL。

**必须调用:** `pg_query_free_parse_result(result)` 释放内存

---

##### pg_query_parse_protobuf

```java
PgQueryProtobufParseResult.ByValue pg_query_parse_protobuf(String input)
```

解析 SQL，返回 Protobuf 格式结果。

**必须调用:** `pg_query_free_protobuf_parse_result(result)` 释放内存

---

##### pg_query_parse_protobuf_opts

```java
PgQueryProtobufParseResult.ByValue pg_query_parse_protobuf_opts(String input, int parser_options)
```

带选项解析 SQL，返回 Protobuf 格式。

**必须调用:** `pg_query_free_protobuf_parse_result(result)` 释放内存

---

#### Split 方法

##### pg_query_split_with_scanner

```java
PgQuerySplitResult.ByValue pg_query_split_with_scanner(String input)
```

使用词法分析器分割 SQL。速度快但精度较低。

**必须调用:** `pg_query_free_split_result(result)` 释放内存

---

##### pg_query_split_with_parser

```java
PgQuerySplitResult.ByValue pg_query_split_with_parser(String input)
```

使用语法分析器分割 SQL。精度高但速度较慢。

**必须调用:** `pg_query_free_split_result(result)` 释放内存

---

#### Free 方法

| 方法 | 用途 |
|------|------|
| `pg_query_free_parse_result(result)` | 释放 `pg_query_parse*` 结果 |
| `pg_query_free_protobuf_parse_result(result)` | 释放 `pg_query_parse_protobuf*` 结果 |
| `pg_query_free_split_result(result)` | 释放 `pg_query_split_*` 结果 |

---

## Data Structures

### PgQueryParseResult

JSON 解析结果结构。

| 字段 | 类型 | 描述 |
|------|------|------|
| `parse_tree` | `String` | JSON 格式语法树 |
| `stderr_buffer` | `String` | 标准错误输出 |
| `error` | `PgQueryError.ByReference` | 错误信息（可为 null） |

| 方法 | 返回类型 | 描述 |
|------|----------|------|
| `hasError()` | `boolean` | 是否有错误 |
| `getErrorMessage()` | `String` | 获取错误信息 |

---

### PgQueryProtobufParseResult

Protobuf 解析结果结构。

| 字段 | 类型 | 描述 |
|------|------|------|
| `parse_tree` | `PgQueryProtobuf` | Protobuf 数据结构 |
| `stderr_buffer` | `String` | 标准错误输出 |
| `error` | `PgQueryError.ByReference` | 错误信息（可为 null） |

| 方法 | 返回类型 | 描述 |
|------|----------|------|
| `hasError()` | `boolean` | 是否有错误 |
| `getErrorMessage()` | `String` | 获取错误信息 |
| `getProtobufBytes()` | `byte[]` | 获取 Protobuf 二进制数据 |

---

### PgQueryProtobuf

Protobuf 二进制数据容器。

| 字段 | 类型 | 描述 |
|------|------|------|
| `len` | `long` | 数据长度 |
| `data` | `Pointer` | 数据指针 |

| 方法 | 返回类型 | 描述 |
|------|----------|------|
| `getBytes()` | `byte[]` | 获取字节数组 |

---

### PgQuerySplitResult

SQL 分割结果结构。

| 字段 | 类型 | 描述 |
|------|------|------|
| `stmts` | `PointerByReference` | 语句数组指针 |
| `n_stmts` | `int` | 语句数量 |
| `stderr_buffer` | `String` | 标准错误输出 |
| `error` | `PgQueryError.ByReference` | 错误信息 |

---

### PgQuerySplitStmt

单条分割语句信息。

| 字段 | 类型 | 描述 |
|------|------|------|
| `stmt_location` | `int` | 语句在原字符串中的起始位置 |
| `stmt_len` | `int` | 语句长度 |

**使用方式:**
```java
String statement = originalSql.substring(stmt.stmt_location, stmt.stmt_location + stmt.stmt_len);
```

---

### PgQueryError

错误信息结构。

| 字段 | 类型 | 描述 |
|------|------|------|
| `message` | `String` | 错误信息 |
| `funcname` | `String` | 发生错误的 C 函数名 |
| `filename` | `String` | 发生错误的源文件 |
| `lineno` | `int` | 源文件行号 |
| `cursorpos` | `int` | SQL 中错误位置 |
| `context` | `String` | 额外上下文（可为 null） |

---

## Complete Example

### Low-level API 使用示例

```java
import com.github.ttttz.pgParser.*;
import com.github.ttttz.pgParser.parse.*;
import com.github.ttttz.pgParser.split.*;
import com.sun.jna.ptr.PointerByReference;

public class LowLevelExample {

    private static final int POINTER_SIZE = 8; // 64-bit

    public void parseJson() {
        String sql = "SELECT * FROM users";
        PgQueryParseResult.ByValue result = PgQueryLibInterface.INSTANCE.pg_query_parse(sql);

        try {
            if (!result.hasError()) {
                System.out.println("Parse tree: " + result.parse_tree);
            } else {
                System.out.println("Error: " + result.getErrorMessage());
            }
        } finally {
            // 必须释放！
            PgQueryLibInterface.INSTANCE.pg_query_free_parse_result(result);
        }
    }

    public void parseProtobuf() throws Exception {
        String sql = "SELECT * FROM users";
        PgQueryProtobufParseResult.ByValue result =
            PgQueryLibInterface.INSTANCE.pg_query_parse_protobuf(sql);

        try {
            if (!result.hasError()) {
                byte[] data = result.getProtobufBytes();
                pg_query.PgQuery.ParseResult parseResult =
                    pg_query.PgQuery.ParseResult.parseFrom(data);
                System.out.println("Version: " + parseResult.getVersion());
            }
        } finally {
            // 必须释放！
            PgQueryLibInterface.INSTANCE.pg_query_free_protobuf_parse_result(result);
        }
    }

    public void splitSql() {
        String sql = "SELECT 1; SELECT 2; SELECT 3;";
        PgQuerySplitResult.ByValue result =
            PgQueryLibInterface.INSTANCE.pg_query_split_with_parser(sql);

        try {
            if (result.error == null || result.error.message == null) {
                PointerByReference stmts = result.stmts;
                for (int i = 0; i < result.n_stmts; i++) {
                    PgQuerySplitStmt.ByReference stmt = new PgQuerySplitStmt.ByReference(
                        stmts.getPointer().getPointer(i * POINTER_SIZE)
                    );
                    stmt.read();
                    String statement = sql.substring(
                        stmt.stmt_location,
                        stmt.stmt_location + stmt.stmt_len
                    );
                    System.out.println("Statement " + i + ": " + statement);
                }
            }
        } finally {
            // 必须释放！
            PgQueryLibInterface.INSTANCE.pg_query_free_split_result(result);
        }
    }
}
```
