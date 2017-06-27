package com.southernbox.infwiki.entity;

/**
 * Created by SouthernBox on 2017/6/27 0027.
 * 冰与火维基分类数据
 */
public class WikiTab {

    private String type;
    private String tabTitle;
    private String title;

    public WikiTab(String type, String tabTitle, String title) {
        this.type = type;
        this.tabTitle = tabTitle;
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
