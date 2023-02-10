package com.material.components.activity.zazastudio;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.sql.Timestamp;
import java.util.List;

@SuppressLint("DefaultLocale")
public class ActionCapture {
    private Integer second;
    private Integer frame;
    private Integer face;
    private List<FaceData> data;
    private Timestamp timestamp;

    public ActionCapture(Integer second, Integer frame, Integer face, Timestamp timestamp, List<FaceData> data) {
        this.second = second;
        this.frame = frame;
        this.face = face;
        this.timestamp = timestamp;
        this.data = data;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }


    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer second) {
        this.second = second;
    }

    public Integer getFrame() {
        return frame;
    }

    public void setFrame(Integer frame) {
        this.frame = frame;
    }

    public Integer getFace() {
        return face;
    }

    public void setFace(Integer face) {
        this.face = face;
    }

    public List<FaceData> getData() {
        return data;
    }

    public void setData(List<FaceData> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
                "{\"face\":%d, \"size\":%d,\"second\":%d, \"frame\":%d, \"data\":%s, \"timestamp\":\"%s\"}",
                face, data.size(), second, frame, data, timestamp);
    }
}
