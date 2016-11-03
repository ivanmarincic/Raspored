package com.idiotnation.raspored;

public class TableColumn {
    int height, top;
    String text;

    public TableColumn() {
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "tableColumn[height=" + height + ",text=\"" + text + "\"" + ",top=" + top + "]";
    }
}
