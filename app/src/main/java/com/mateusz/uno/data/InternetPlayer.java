package com.mateusz.uno.data;

public class InternetPlayer{

    private String name;
    private int photoId;
    private int cardCount = 0;

    public InternetPlayer(){
        //No Argument Constructor
    }

    public InternetPlayer(int photoId, String name) {
      this.photoId = photoId;
        this.name = name;
    }

    public int getPhotoId() {
        return photoId;
    }

    public String getName() {
        return name;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }
}
