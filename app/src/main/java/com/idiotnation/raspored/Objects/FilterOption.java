package com.idiotnation.raspored.Objects;

public class FilterOption{
    String filter;
    boolean value;

    public FilterOption(String filter, boolean value) {
        this.filter = filter;
        this.value = value;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "filterOption{" +
                "filter='" + filter + '\'' +
                ", value=" + value +
                '}';
    }
}