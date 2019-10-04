package com.mateusz.uno;

public class Card {

    private Colour colour;
    private String value;
    private int id;

    public Card(int id, Colour colour, String value) {
        this.id = id;
        this.colour = colour;
        this.value = value;
    }

    public Colour getColour() {
        return colour;
    }

    public String getValue() {
        return value;
    }

    public int getId() {
        return id;
    }

    public enum Colour{
        RED,
        BLUE,
        GREEN,
        YELLOW,
        BLACK
    }

}
