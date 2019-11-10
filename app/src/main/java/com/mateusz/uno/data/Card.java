package com.mateusz.uno.data;

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

    @Override
    public String toString() {
        return "Card{" +
                "colour=" + colour +
                ", value='" + value + '\'' +
                ", id=" + id +
                '}';
    }
}
