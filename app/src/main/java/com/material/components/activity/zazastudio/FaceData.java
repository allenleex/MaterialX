package com.material.components.activity.zazastudio;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

@SuppressLint("DefaultLocale")
public class FaceData {
    private Integer id;
    private Float x;
    private Float y;
    private Float z;

    public FaceData(Integer id, Float x, Float y, Float z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getZ() {
        return z;
    }

    public void setZ(Float z) {
        this.z = z;
    }
    
    @NonNull
    @Override
    public String toString() {
        return String.format("{\"id\":%d, \"x\":%f, \"y\":%f, \"z\":%f}", id, x, y, z);
    }
}
