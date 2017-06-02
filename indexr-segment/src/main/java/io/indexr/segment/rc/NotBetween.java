package io.indexr.segment.rc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.spark.unsafe.types.UTF8String;

import java.io.IOException;

import io.indexr.segment.Column;
import io.indexr.segment.InfoSegment;
import io.indexr.segment.OuterIndex;
import io.indexr.segment.RSValue;
import io.indexr.segment.Segment;
import io.indexr.util.BitMap;

public class NotBetween extends Between {
    @JsonCreator
    public NotBetween(@JsonProperty("attr") Attr attr,
                      @JsonProperty("numValue1") long numValue1,
                      @JsonProperty("numValue2") long numValue2,
                      @JsonProperty("strValue1") String strValue1,
                      @JsonProperty("strValue2") String strValue2) {
        super(attr, numValue1, numValue2, strValue1, strValue2);
    }

    public NotBetween(Attr attr,
                      long numValue1,
                      long numValue2,
                      UTF8String strValue1,
                      UTF8String strValue2) {
        super(attr, numValue1, numValue2, strValue1, strValue2);
    }

    @Override
    public String getType() {
        return "not_between";
    }

    @Override
    public RCOperator applyNot() {
        return new Between(attr, numValue1, numValue2, strValue1, strValue2);
    }

    @Override
    public BitMap exactCheckOnPack(Segment segment) throws IOException {
        assert attr.checkCurrent(segment.schema().columns);

        Column column = segment.column(attr.columnId());
        try (OuterIndex outerIndex = column.outerIndex()) {
            return outerIndex.between(column, numValue1, numValue2, strValue1, strValue2, true);
        }
    }

    @Override
    public byte roughCheckOnPack(Segment segment, int packId) throws IOException {
        return RSValue.not(super.roughCheckOnPack(segment, packId));
    }

    @Override
    public byte roughCheckOnColumn(InfoSegment segment) throws IOException {
        return RSValue.not(super.roughCheckOnColumn(segment));
    }

    @Override
    public BitMap exactCheckOnRow(Segment segment, int packId) throws IOException {
        return BitMap.not(super.exactCheckOnRow(segment, packId));
    }
}
