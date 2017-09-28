package com.example.thuanpc.sdsupracticequestions.API;

/**
 * Created by thuanPC on 4/19/2017.
 */

public class Message {
    public String text;
    public String email;
    public String name;
    public long createdAt;

    public Message(){}

    public Message(String text, String email, String name, long createdAt){
        this.text = text;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
