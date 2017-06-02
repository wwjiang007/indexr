package io.indexr.segment;

import java.io.IOException;

import io.indexr.segment.index.OuterIndex_Invalid;
import io.indexr.segment.pack.DataPackNode;

public interface Column {

    String name();

    /**
     * The storage type.
     *
     * @see {@link ColumnType}
     */
    default byte dataType() {
        return sqlType().dataType;
    }

    SQLType sqlType();

    boolean isIndexed();

    int packCount();

    long rowCount() throws IOException;

    /**
     * Get the infomation of the pack.
     */
    DataPackNode dpn(int packId) throws IOException;

    /**
     * Fetch a pack by its <i>packId</i>.
     *
     * You should not hold the pack for a long time, i.e. more than several munites.
     * Because it could be freed by underlying system after some cache time. After that any operation
     * with it becomes dangerous, could even crash the process.
     *
     * This operation is mean to be heavy costly, don't call it unless you really need to fetch raw data.
     * {@link #dpn(int)} should provide enough infomation.
     */
    <T extends DPValues> T pack(int packId) throws IOException;

    /**
     * Get the rs index of the column.
     */
    <T extends RSIndex> T rsIndex() throws IOException;

    /**
     * Get the extend index of the pack.
     */
    <T extends PackExtIndex> T extIndex(int packId) throws IOException;

    /**
     * Get the outer index of the column.
     */
    default <T extends OuterIndex> T outerIndex() throws IOException {
        return (T) new OuterIndex_Invalid();
    }
}
