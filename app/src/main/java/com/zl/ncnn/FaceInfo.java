package com.zl.ncnn;

import android.graphics.Rect;

import java.io.Serializable;

public class FaceInfo implements Serializable {
    private Rect faceRect;
    private float score;

    public FaceInfo() {
    }

    public Rect getFaceRect() {
        return faceRect;
    }

    public void setFaceRect(Rect faceRect) {
        this.faceRect = faceRect;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "FaceInfo{" +
                "faceRect=" + faceRect.toString() +
                ", score=" + score +
                '}';
    }
}
