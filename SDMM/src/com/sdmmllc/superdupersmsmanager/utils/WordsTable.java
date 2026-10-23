package com.sdmmllc.superdupersmsmanager.utils;
public final class WordsTable {

    /**
     * Not instantiable.
     * @hide
     */
    public WordsTable() {}

    /**
     * Primary key.
     * <P>Type: INTEGER (long)</P>
     */
    public static final String ID = "_id";

    /**
     * Source row ID.
     * <P>Type: INTEGER (long)</P>
     */
    public static final String SOURCE_ROW_ID = "source_id";

    /**
     * Table ID (either 1 or 2).
     * <P>Type: INTEGER</P>
     */
    public static final String TABLE_ID = "table_to_use";

    /**
     * The words to index.
     * <P>Type: TEXT</P>
     */
    public static final String INDEXED_TEXT = "index_text";
}
