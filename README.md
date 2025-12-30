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
 * protoc ( 3.21.12 for regenerating protobuf classes,sudo apt install protobuf-compiler, and protoc --version is same with protobuf-java in pom.xml)

**For Docker build (recommended):**
 * docker

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

## Authors
 - [jiangtao69039](https://github.com/jiangtao69039)

## Change log
 * 2024-12-30: Add Docker build profile with debian10 for glibc compatibility
 * 2024-12-30: Add protobuf support and Java bindings
 * 2022-12-02: add support of pg_query_split_with_parser
 * 2022-12-02: add support of pg_query_split_with_scanner
 * 2022-12-02: add support of pg_query_free_split_result