package com.example.mobilesaucychat.models;

import com.google.firebase.Timestamp;

public class Message {
    private String userId, text, imageId;
    private Timestamp time;

    public Message() {
    }

    public Message(
            String userId,
            String text,
            Timestamp time
    ) {
        this.userId = userId;
        this.text = text;
        this.time = time;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public String getImageId() { return imageId; }

    public void setImageId(String imageId) { this.imageId = imageId; }

    public Timestamp getTime() { return time; }

    public void setTime(Timestamp time) { this.time = time; }
}
