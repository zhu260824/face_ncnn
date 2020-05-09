package com.zl.face;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class FaceInfo implements Parcelable {
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
                "faceRect=" + faceRect +
                ", score=" + score +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.faceRect, flags);
        dest.writeFloat(this.score);
    }

    protected FaceInfo(Parcel in) {
        this.faceRect = in.readParcelable(Rect.class.getClassLoader());
        this.score = in.readFloat();
    }

    public static final Parcelable.Creator<FaceInfo> CREATOR = new Parcelable.Creator<FaceInfo>() {
        @Override
        public FaceInfo createFromParcel(Parcel source) {
            return new FaceInfo(source);
        }

        @Override
        public FaceInfo[] newArray(int size) {
            return new FaceInfo[size];
        }
    };
}
