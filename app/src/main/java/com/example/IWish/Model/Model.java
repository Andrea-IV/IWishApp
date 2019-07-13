package com.example.IWish.Model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Model {

    public long id;
    public long createdAt;
    public long updatedAt;

    public Model() {
    }

    public abstract JSONObject getModelDefinition() throws JSONException;

    public Map<String, String> toMap(boolean includeRelations) {
        Map<String, String> map = new HashMap<>();
        for ( Field field : getClass().getDeclaredFields() ) {
            try {
                Object fieldValue = field.get(this);
                String fieldName = field.getName();

                if ( fieldValue != null && !("serialVersionUID".equals(fieldName)) ) {
                    if ( fieldValue instanceof List ) {
                        if ( includeRelations ) {
                            List<?> fieldValueList = (List) fieldValue;
                            if (!fieldValueList.isEmpty() && fieldValueList.get(0) instanceof Model) {
                                List<Long> valuesLongs = new ArrayList<>(fieldValueList.size());
                                for (Object o : fieldValueList) {
                                    Model inst = (Model) o;
                                    Long longValue = inst.id;
                                    valuesLongs.add(longValue);
                                }
                                map.put(fieldName, valuesLongs.toString());
                            }
                        }
                    }
                    else {
                        map.put(fieldName, fieldValue.toString());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    public Map<String, String> toMap() {
        return toMap(true);
    }
}
