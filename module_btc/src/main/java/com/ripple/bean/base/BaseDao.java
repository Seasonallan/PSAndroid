package com.ripple.bean.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

/**
 * 实体基类, 需要存在默认构造函数 用以反射
 *
 * @author laijp
 * @date 2014-6-13
 * @email 451360508@qq.com
 */
public class BaseDao implements IProguardFilterOnlyPublic {

    /**
     * JSONObject类型转换为该实体
     *
     * @param object
     * @return
     */
    public void fromJsonObject(JSONObject object) {
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isAccessible())
                field.setAccessible(true);
            try {
                Json annotationColumn = field.getAnnotation(Json.class);
                if (annotationColumn != null) {
                    field.set(this, getValueFromJsonObject(object, field));
                }
            } catch (JSONException e) {
            } catch (IllegalAccessException e) {
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 转换为可用于网络请求操作的键值对
     *
     * @return
     * @throws IllegalAccessException
     */
    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        for (Field field : getClass().getDeclaredFields()) {
            try {
                Json annotationColumn = field.getAnnotation(Json.class);
                if (annotationColumn != null) {
                    putInJsonObject(obj, field);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }



    /**
     * 存储某一对键值
     *
     * @param object
     * @param field
     * @throws IllegalAccessException
     */
    private void putInJsonObject(JSONObject object, Field field) throws JSONException, IllegalAccessException {
        if (!field.isAccessible())
            field.setAccessible(true); // for private variables
        Object fieldValue = field.get(this);
        String key = getJsonName(field);
        if (fieldValue instanceof Long) {
            object.put(key, Long.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof JsonArrayList) {
            JsonArrayList<?> list = (JsonArrayList<?>) fieldValue;
            JSONArray array = list.toJsonArray();
            object.put(key, array);
        } else if (fieldValue instanceof String) {
            object.put(key, fieldValue.toString());
        } else if (fieldValue instanceof Integer) {
            object.put(key, Integer.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Float) {
            object.put(key, Float.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Byte) {
            object.put(key, Byte.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Short) {
            object.put(key, Short.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Boolean) {
            object.put(key, Boolean.parseBoolean(fieldValue.toString()));
        } else if (fieldValue instanceof Double) {
            object.put(key, Double.valueOf(fieldValue.toString()));
        } else if (fieldValue instanceof Byte[] || fieldValue instanceof byte[]) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                        outputStream);
                objectOutputStream.writeObject(fieldValue);
                object.put(key, outputStream.toByteArray());
                objectOutputStream.flush();
                objectOutputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
            }
        } else if (fieldValue instanceof BaseDao) {
            object.put(key, ((BaseDao) fieldValue).toJsonObject());
        }
    }


    // Get content from specific types
    private Object getValueFromJsonObject(JSONObject object, Field field)
            throws JSONException, IllegalAccessException, InstantiationException {
        Class<?> fieldType = field.getType();
        String columnIndex = getJsonName(field);
        if (fieldType.isAssignableFrom(JsonArrayList.class)) {
            Json fieldEntityAnnotation = field.getAnnotation(Json.class);
            @SuppressWarnings({"unchecked", "rawtypes"})
            JsonArrayList<?> list = new JsonArrayList(fieldEntityAnnotation.className());
            list.fromJsonArray(object.getJSONArray(columnIndex));
            return list;
        } else if (fieldType.isAssignableFrom(Long.class)
                || fieldType.isAssignableFrom(long.class)) {
            return object.getLong(columnIndex);
        } else if (fieldType.isAssignableFrom(String.class)) {
            return object.getString(columnIndex);
        } else if ((fieldType.isAssignableFrom(Integer.class) || fieldType
                .isAssignableFrom(int.class))) {
            return object.getInt(columnIndex);
        } else if ((fieldType.isAssignableFrom(Byte[].class) || fieldType
                .isAssignableFrom(byte[].class))) {
            return object.getInt(columnIndex);
        } else if ((fieldType.isAssignableFrom(Double.class) || fieldType
                .isAssignableFrom(double.class))) {
            return object.getDouble(columnIndex);
        } else if ((fieldType.isAssignableFrom(Float.class) || fieldType
                .isAssignableFrom(float.class))) {
            return object.getDouble(columnIndex);
        } else if ((fieldType.isAssignableFrom(Short.class) || fieldType
                .isAssignableFrom(short.class))) {
            return object.getInt(columnIndex);
        } else if (fieldType.isAssignableFrom(Byte.class)
                || fieldType.isAssignableFrom(byte.class)) {
            return (byte) object.getInt(columnIndex);
        } else if (fieldType.isAssignableFrom(Boolean.class)
                || fieldType.isAssignableFrom(boolean.class)) {
            return object.getBoolean(columnIndex);
        } else if (fieldType.newInstance() instanceof BaseDao) {
            BaseDao model = (BaseDao) fieldType.newInstance();
            model.fromJsonObject(object.getJSONObject(columnIndex));
            return model;
        }
        return null;
    }


    /**
     * 获取参数名
     *
     * @param field
     * @return
     */
    private String getJsonName(Field field) {
        Json annotationColumn = field.getAnnotation(Json.class);
        String column = null;
        if (annotationColumn != null) {
            if (annotationColumn.name().equals("")) {
                column = field.getName();
            } else {
                column = annotationColumn.name();
            }
        }
        return column;
    }
}












