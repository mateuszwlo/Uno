package com.mateusz.uno.data;

public class UserData {

    private String name;
    private int photoId;

    public UserData(int photoId, String name) {
        this.photoId = photoId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPhotoId() {
        return photoId;
    }
}
