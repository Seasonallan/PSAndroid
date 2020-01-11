package com.season.lib.view;

public class LayerInfoImp implements LayerInfoI
{
    private long mViewId;

    public LayerInfoImp() {
        mViewId = System.currentTimeMillis();
    }

    @Override
    public long getViewId() {
        return mViewId;
    }

    @Override
    public void setViewId(long id) {
        mViewId = id;
    }

//    @Override
//    public boolean revoke() {
//        return false;
//    }
//
//    @Override
//    public boolean redo() {
//        return false;
//    }
}
