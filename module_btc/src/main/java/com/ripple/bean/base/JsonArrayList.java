package com.ripple.bean.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Http json格式转换列表数据
 * @author laijp
 * @date 2014-6-13
 * @email 451360508@qq.com
 */
public class JsonArrayList<T extends BaseDao> extends ArrayList<T> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6279092799396446519L;
	Class<T> tClass;
    public JsonArrayList(Class<T> tClass){
        this.tClass = tClass;
    }


    /**
     * 网络请求转化
     * @param array
     */
    public JsonArrayList<T> fromJsonArray(JSONArray array){
        int count = array.length();
        for(int i = 0;i < count; i++){
            try {
                JSONObject obj = array.getJSONObject(i);
                T item = tClass.newInstance();
                item.fromJsonObject(obj);
                add(item);
            } catch (JSONException e) {
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            }
        }
        return this;
    }

    /**
     * 转化为网络请求数据
     * @return
     */
    public JSONArray toJsonArray(){
        JSONArray array = new JSONArray();
        for (T item: this){
                JSONObject obj = item.toJsonObject();
                array.put(obj);
        }
        return array;
    }
}














