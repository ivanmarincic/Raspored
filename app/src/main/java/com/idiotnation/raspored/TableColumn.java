package com.idiotnation.raspored;

public class TableColumn {
    int x, y;
    int width, height;

    public TableColumn(int x, int y, int width, int height) {
        this.height = height;
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "tableColumn[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
    }
}
