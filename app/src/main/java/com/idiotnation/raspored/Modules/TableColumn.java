package com.idiotnation.raspored.Modules;

import java.util.Date;

public class TableColumn {
    int width, colCount;
    float top, left, height;
    Date start, end;
    String text;
    boolean visible;

    public TableColumn() {
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getColCount() {
        return colCount;
    }

    public void setColCount(int colCount) {
        this.colCount = colCount;
    }

    public float getTop() {
        return top;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisibility(boolean visibility) {
        this.visible = visibility;
    }

    @Override
    public String toString() {
        return "TableColumn{" +
                "width=" + width +
                ", colCount=" + colCount +
                ", top=" + top +
                ", left=" + left +
                ", height=" + height +
                ", start=" + start +
                ", end=" + end +
                ", text='" + text + '\'' +
                ", visible=" + visible +
                '}';
    }
}
