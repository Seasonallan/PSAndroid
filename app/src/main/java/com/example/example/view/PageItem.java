package com.example.example.view;

import java.util.ArrayList;
import java.util.List;

public class PageItem {

    public int color;
    public String title;
    public String time;
    public int pageNumber;
    public List<PageContent> contentList;

    public String getPageNumber() {
        if (pageNumber < 10) {
            return "0" + pageNumber;
        }
        return pageNumber + "";
    }

    public static class PageContent {
        public String title;
        public String[] items;
    }

    public static PageItem create(String title, String time) {
        PageItem pageItem = new PageItem();
        pageItem.time = time;
        pageItem.title = title;
        return pageItem;
    }

    public PageItem decorateContent(String title, String... items) {
        if (contentList == null) {
            contentList = new ArrayList<>();
        }
        PageContent pageContent = new PageContent();
        pageContent.title = title;
        pageContent.items = items;
        contentList.add(pageContent);
        return this;
    }

    public PageItem page(int page) {
        this.pageNumber = page;
        return this;
    }

    public PageItem color(int color) {
        this.color = color;
        return this;
    }
}
