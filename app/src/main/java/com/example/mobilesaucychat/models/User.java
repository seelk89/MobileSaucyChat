package com.example.mobilesaucychat.models;

public class User {
    private long m_id;
    private String m_username;
    private String m_password;
    private double m_latLocation;
    private double m_lonLocation;
    private String m_displayName;
    private String m_imageId;

    public User(
            String username,
            String password,
            String displayName
    ) {
        m_username = username;
        m_password = password;
        m_displayName = displayName;
    }

    public long getId() {
        return m_id;
    }

    public void setId(long id) {
        m_id = id;
    }

    public String getUsername() {
        return m_username;
    }

    public void setuserName(String username) {
        m_username = username;
    }

    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public double getLatLocation() {
        return m_latLocation;
    }

    public void setLatLocation(double latLocation) {
        m_latLocation = latLocation;
    }

    public double getLonLocation() {
        return m_lonLocation;
    }

    public void setLonLocation(double lonLocation) {
        m_lonLocation = lonLocation;
    }

    public String getDisplayName() {
        return m_displayName;
    }

    public void setDisplayName(String displayName) {
        m_displayName = displayName;
    }

    public String getImageId() { return m_imageId; }

    public void setImageId(String imageId) { m_imageId = imageId; }
}
