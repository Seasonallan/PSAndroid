package com.season.lib.bean;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class LayerEntity implements Serializable {


    private float width;
    private float height;
    private LayerBackground backInfoModel;
    private List<LayerItem> itemArray = new ArrayList<>();


    public float getHeight() {
        return height;
    }
    public void setHeight(float height) {
        this.height = height;
    }

    public LayerBackground getBackInfoModel() {
        return backInfoModel;
    }

    public void setBackInfoModel(LayerBackground backInfoModel) {
        this.backInfoModel = backInfoModel;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }


    public List<LayerItem> getItemArray() {
        return itemArray;
    }

    public void setItemArray(List<LayerItem> itemArray) {
        if (itemArray != null) {
            for (LayerItem bean : itemArray) {
                this.itemArray.add(bean);
            }
        } else {
            if (this.itemArray != null) this.itemArray.clear();
        }
    }

}
