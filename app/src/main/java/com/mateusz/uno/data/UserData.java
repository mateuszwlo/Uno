package com.mateusz.uno.data;

import com.google.firebase.firestore.Exclude;

public class UserData {

    private String id;
    private String name;
    private int photoId;

    public UserData(){
        //No Argument Constructor
    }


    public UserData(String id, int photoId, String name) {
        this.id = id;
        this.photoId = photoId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
