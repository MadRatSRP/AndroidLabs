package com.androidlabs.model;

public class History {
    private int id;
    private int dataId;
    private String figureName;
    private Double area;
    private Double perimeter;

    public History(int id, int dataId, String figureName, Double area, Double perimeter) {
        this.id = id;
        this.dataId = dataId;
        this.figureName = figureName;
        this.area = area;
        this.perimeter = perimeter;
    }

    public int getId() {
        return id;
    }

    public String getFigureName() {
        return figureName;
    }

    public Double getArea() {
        return area;
    }

    public Double getPerimeter() {
        return perimeter;
    }

    public int getDataId() {
        return dataId;
    }
}
