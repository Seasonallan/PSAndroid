package com.season.lib.epub.paser.html.tag;

import android.text.Editable;


public interface TagHandler {
    /**
     * This method will be called whenn the HTML parser encounters a tag
     * that it does not know how to interpret.
     * @return 已处理放回true
     */
    public boolean handleTag(TagInfo tagInfo, Editable editable, boolean isStart);

    public boolean isFilter(TagInfo tagInfo, boolean isStar);

    public boolean handleCharacters(TagInfo tagInfo, Editable editable, StringBuilder charContainer);
}
