package com.android.jtwallet.core.coretypes;

import java.util.ArrayList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.jtwallet.core.fields.Field;
import com.android.jtwallet.core.fields.STArrayField;
import com.android.jtwallet.core.fields.Type;
import com.android.jtwallet.core.serialized.BinaryParser;
import com.android.jtwallet.core.serialized.BytesSink;
import com.android.jtwallet.core.serialized.SerializedType;
import com.android.jtwallet.core.serialized.TypeTranslator;

public class STArray extends ArrayList<STObject> implements SerializedType {
    public JSONArray toJSONArray() {
        JSONArray array = new JSONArray();

        for (STObject so : this) {
            array.add(so.toJSON());
        }

        return array;
    }

    @Override
    public Object toJSON() {
        return toJSONArray();
    }

    @Override
    public byte[] toBytes() {
        return translate.toBytes(this);
    }

    @Override
    public String toHex() {
        return translate.toHex(this);
    }

    @Override
    public void toBytesSink(BytesSink to) {
        for (STObject stObject : this) {
            stObject.toBytesSink(to);
        }
    }

    @Override
    public Type type() {
        return Type.STArray;
    }

    public static class Translator extends TypeTranslator<STArray> {

        @Override
        public STArray fromParser(BinaryParser parser, Integer hint) {
            STArray stArray = new STArray();
            while (!parser.end()) {
                Field field = parser.readField();
                if (field == Field.ArrayEndMarker) {
                    break;
                }
                STObject outer = new STObject();
                // assert field.getType() == Type.STObject;
                outer.put(field, STObject.translate.fromParser(parser));
                stArray.add(STObject.formatted(outer));
            }
            return stArray;
        }

        @Override
        public com.alibaba.fastjson.JSONArray toJSONArray(STArray obj) {
            return obj.toJSONArray();
        }

        @Override
        public STArray fromJSONArray(JSONArray jsonArray) {
            STArray arr = new STArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                Object o = jsonArray.get(i);
                arr.add(STObject.fromJSONObject((JSONObject) o));
            }

            return arr;
        }
    }
    static public Translator translate = new Translator();

    public STArray(){}

    public static STArrayField starrayField(final Field f) {
        return new STArrayField(){ @Override public Field getField() {return f;}};
    }

    static public STArrayField AffectedNodes = starrayField(Field.AffectedNodes);
    static public STArrayField SignerEntries = starrayField(Field.SignerEntries);
    static public STArrayField Signers = starrayField(Field.Signers);

    static public STArrayField Template = starrayField(Field.Template);
    static public STArrayField Necessary = starrayField(Field.Necessary);
    static public STArrayField Sufficient = starrayField(Field.Sufficient);
    
    static public STArrayField Memos = starrayField(Field.Memos);
}
