package com.example.mobilesaucychat.models;

import java.util.Date;

public class Message {
    private long m_id;
    private long m_userId;
    private String m_message;
    private String m_imageId;
    private Date m_time;

    public Message(
            long userId,
            String message,
            String imageId,
            Date time
    ) {
        m_userId = userId;
        m_message = message;
        m_imageId = imageId;
        m_time = time;
    }

    public long getId() {
        return m_id;
    }

    public void setId(long id) {
        m_id = id;
    }

    public long getUserId() { return m_userId; }

    public void setUserId(long userId) {
        m_userId = userId;
    }

    public String getMessage() { return m_message; }

    public void setMessage(String message) { m_message = message; }

    public String getImageId() { return m_imageId; }

    public void setImageId(String imageId) { m_imageId = imageId; }

    public Date getTime() { return m_time; }

    public void setTime(Date time) { m_time = time; }
}
