package com.mateusz.uno.data;

public class UserData {

    private String name;
    private int id;

    public UserData(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
