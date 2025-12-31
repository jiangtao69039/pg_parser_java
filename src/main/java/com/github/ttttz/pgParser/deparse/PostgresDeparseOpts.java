package com.github.ttttz.pgParser.deparse;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Maps to C struct:
 * typedef struct PostgresDeparseOpts {
 *     PostgresDeparseComment **comments;
 *     size_t comment_count;
 *     bool pretty_print;
 *     int indent_size;
 *     int max_line_length;
 *     bool trailing_newline;
 *     bool commas_start_of_line;
 * } PostgresDeparseOpts;
 */
public class PostgresDeparseOpts extends Structure {

    public static class ByReference extends PostgresDeparseOpts implements Structure.ByReference {
    }

    public static class ByValue extends PostgresDeparseOpts implements Structure.ByValue {
    }

    public Pointer comments;        // PostgresDeparseComment**
    public long comment_count;      // size_t
    public boolean pretty_print;
    public int indent_size;
    public int max_line_length;
    public boolean trailing_newline;
    public boolean commas_start_of_line;

    public PostgresDeparseOpts() {
        // Default values
        this.comments = null;
        this.comment_count = 0;
        this.pretty_print = false;
        this.indent_size = 4;
        this.max_line_length = 80;
        this.trailing_newline = false;
        this.commas_start_of_line = false;
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "comments",
                "comment_count",
                "pretty_print",
                "indent_size",
                "max_line_length",
                "trailing_newline",
                "commas_start_of_line"
        );
    }

    /**
     * Create a builder for PostgresDeparseOpts
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for PostgresDeparseOpts
     */
    public static class Builder {
        private boolean prettyPrint = false;
        private int indentSize = 4;
        private int maxLineLength = 80;
        private boolean trailingNewline = false;
        private boolean commasStartOfLine = false;

        public Builder prettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        public Builder indentSize(int indentSize) {
            this.indentSize = indentSize;
            return this;
        }

        public Builder maxLineLength(int maxLineLength) {
            this.maxLineLength = maxLineLength;
            return this;
        }

        public Builder trailingNewline(boolean trailingNewline) {
            this.trailingNewline = trailingNewline;
            return this;
        }

        public Builder commasStartOfLine(boolean commasStartOfLine) {
            this.commasStartOfLine = commasStartOfLine;
            return this;
        }

        public PostgresDeparseOpts.ByValue build() {
            PostgresDeparseOpts.ByValue opts = new PostgresDeparseOpts.ByValue();
            opts.comments = null;
            opts.comment_count = 0;
            opts.pretty_print = this.prettyPrint;
            opts.indent_size = this.indentSize;
            opts.max_line_length = this.maxLineLength;
            opts.trailing_newline = this.trailingNewline;
            opts.commas_start_of_line = this.commasStartOfLine;
            return opts;
        }
    }
}
