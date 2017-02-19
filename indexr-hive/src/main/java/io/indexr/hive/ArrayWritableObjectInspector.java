package io.indexr.hive;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.SettableStructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.io.ArrayWritable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ArrayWritableObjectInspector extends SettableStructObjectInspector {
    private final TypeInfo typeInfo;
    private final List<TypeInfo> fieldInfos;
    private final List<String> fieldNames;
    private final List<StructField> fields;
    private final HashMap<String, StructFieldImpl> fieldsByName;

    public ArrayWritableObjectInspector(final StructTypeInfo rowTypeInfo) {

        typeInfo = rowTypeInfo;
        fieldNames = rowTypeInfo.getAllStructFieldNames();
        fieldInfos = rowTypeInfo.getAllStructFieldTypeInfos();
        fields = new ArrayList<StructField>(fieldNames.size());
        fieldsByName = new HashMap<String, StructFieldImpl>();

        for (int i = 0; i < fieldNames.size(); ++i) {
            final String name = fieldNames.get(i);
            final TypeInfo fieldInfo = fieldInfos.get(i);

            final StructFieldImpl field = new StructFieldImpl(name, getObjectInspector(fieldInfo), i);
            fields.add(field);
            fieldsByName.put(name, field);
        }
    }

    private ObjectInspector getObjectInspector(final TypeInfo typeInfo) {
        if (typeInfo.equals(TypeInfoFactory.doubleTypeInfo)) {
            return PrimitiveObjectInspectorFactory.writableDoubleObjectInspector;
        } else if (typeInfo.equals(TypeInfoFactory.booleanTypeInfo)) {
            return PrimitiveObjectInspectorFactory.writableBooleanObjectInspector;
        } else if (typeInfo.equals(TypeInfoFactory.floatTypeInfo)) {
            return PrimitiveObjectInspectorFactory.writableFloatObjectInspector;
        } else if (typeInfo.equals(TypeInfoFactory.intTypeInfo)) {
            return PrimitiveObjectInspectorFactory.writableIntObjectInspector;
        } else if (typeInfo.equals(TypeInfoFactory.longTypeInfo)) {
            return PrimitiveObjectInspectorFactory.writableLongObjectInspector;
        } else if (typeInfo.equals(TypeInfoFactory.stringTypeInfo)) {
            return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
        } else if (typeInfo.equals(TypeInfoFactory.timestampTypeInfo)) {
            return PrimitiveObjectInspectorFactory.writableTimestampObjectInspector;
        } else if (typeInfo.equals(TypeInfoFactory.dateTypeInfo)) {
            return PrimitiveObjectInspectorFactory.writableDateObjectInspector;
        } else {
            throw new UnsupportedOperationException("Unknown field type: " + typeInfo);
        }
    }

    @Override
    public Category getCategory() {
        return Category.STRUCT;
    }

    @Override
    public String getTypeName() {
        return typeInfo.getTypeName();
    }

    @Override
    public List<? extends StructField> getAllStructFieldRefs() {
        return fields;
    }

    @Override
    public Object getStructFieldData(final Object data, final StructField fieldRef) {
        if (data == null) {
            return null;
        }

        if (data instanceof ArrayWritable) {
            final ArrayWritable arr = (ArrayWritable) data;
            return arr.get()[((StructFieldImpl) fieldRef).getIndex()];
        }

        //since setStructFieldData and create return a list, getStructFieldData should be able to
        //handle list data. This is required when table serde is ParquetHiveSerDe and partition serde
        //is something else.
        if (data instanceof List) {
            return ((List) data).get(((StructFieldImpl) fieldRef).getIndex());
        }

        throw new UnsupportedOperationException("Cannot inspect " + data.getClass().getCanonicalName());
    }

    @Override
    public StructField getStructFieldRef(final String name) {
        return fieldsByName.get(name);
    }

    @Override
    public List<Object> getStructFieldsDataAsList(final Object data) {
        if (data == null) {
            return null;
        }

        if (data instanceof ArrayWritable) {
            final ArrayWritable arr = (ArrayWritable) data;
            final Object[] arrWritable = arr.get();
            return new ArrayList<Object>(Arrays.asList(arrWritable));
        }

        throw new UnsupportedOperationException("Cannot inspect " + data.getClass().getCanonicalName());
    }

    @Override
    public Object create() {
        final ArrayList<Object> list = new ArrayList<Object>(fields.size());
        for (int i = 0; i < fields.size(); ++i) {
            list.add(null);
        }
        return list;
    }

    @Override
    public Object setStructFieldData(Object struct, StructField field, Object fieldValue) {
        final ArrayList<Object> list = (ArrayList<Object>) struct;
        list.set(((StructFieldImpl) field).getIndex(), fieldValue);
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArrayWritableObjectInspector other = (ArrayWritableObjectInspector) obj;
        if (this.typeInfo != other.typeInfo && (this.typeInfo == null || !this.typeInfo.equals(other.typeInfo))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.typeInfo != null ? this.typeInfo.hashCode() : 0);
        return hash;
    }

    class StructFieldImpl implements StructField {

        private final String name;
        private final ObjectInspector inspector;
        private final int index;

        public StructFieldImpl(final String name, final ObjectInspector inspector, final int index) {
            this.name = name;
            this.inspector = inspector;
            this.index = index;
        }

        @Override
        public String getFieldComment() {
            return "";
        }

        @Override
        public String getFieldName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public ObjectInspector getFieldObjectInspector() {
            return inspector;
        }

        @Override
        public int getFieldID() {
            return index;
        }
    }
}