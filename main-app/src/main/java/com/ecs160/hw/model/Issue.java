package com.ecs160.hw.model;

import com.ecs160.persistence.annotations.*;

@PersistableObject
public class Issue {

    @Id
    @PersistableField
    private String title;
    
    @PersistableField
    private String body; //issue description
    
    @PersistableField
    private String state;
    
    @PersistableField
    private String createdAt;
    
    @PersistableField
    private String updatedAt;

    public Issue() {} // for redisDB reconstruction

    public Issue(String title, String body, String state, String createdAt, String updatedAt) {
        this.title = title;
        this.body = body;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public String getTitle() {
        return title;
    }
    public String getBody() {
        return body;
    }
    public String getState() {
        return state;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }
    //HOw to connect with them?
    /*
     * create an issue using the gson pasring
     * add the issue to the relevant repo
     * 
     * 
     */

}
