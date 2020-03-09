package com.season.lib.view.gif;

import android.graphics.Canvas;

public abstract class GifPlugin {

    public static GifPlugin getPlugin(boolean isTransparent, String file, boolean autoPlay){
        GifPlugin plugin;
        if (true){
            plugin = new FramePlugin();
            plugin.init(file);
            return plugin;
        }
        if (!isTransparent || autoPlay){
            plugin = new MoviePlugin();
            if (plugin.init(file)){
                return plugin;
            }
            plugin.onRelease();
        }
        plugin = new FramePlugin();
        plugin.init(file);
        return plugin;
    }

    public abstract int getDelay();
    public abstract int getDuration() ;
    public abstract void onRelease();
    public abstract boolean init(String file);
    public abstract void drawCanvasTime(Canvas canvas, int time);
    public abstract String getDescription();
}
