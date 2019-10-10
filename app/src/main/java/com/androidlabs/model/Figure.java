package com.androidlabs.model;

public class Figure {
    private Integer drawableId;
    private String figureName;
    private Integer fragmentId;

    public Figure(Integer drawableId, String figureName, Integer fragmentId) {
        this.drawableId = drawableId;
        this.figureName = figureName;
        this.fragmentId = fragmentId;
    }

    public Integer getDrawableId() {
        return drawableId;
    }

    public String getFigureName() {
        return figureName;
    }

    public Integer getFragmentId() {
        return fragmentId;
    }
}
