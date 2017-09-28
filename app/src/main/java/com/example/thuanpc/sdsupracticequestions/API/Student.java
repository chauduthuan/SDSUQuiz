package com.example.thuanpc.sdsupracticequestions.API;

/**
 * Created by thuanPC on 4/19/2017.
 */

public class Student {

    String email;
    String name;
    String redid;
    long createdAt;

    public Student(){}

    public Student(String email, String name, String redid, long createdAt){
        this.email = email;
        this.name = name;
        this.redid = redid;
        this.createdAt = createdAt;
    }

    public String getRedid() {
        return redid;
    }

    public void setRedid(String redid) {
        this.redid = redid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
