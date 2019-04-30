package com.example.mobilesaucychat.Shared;

public class Variables {
    // static variable single_instance of type Variables
    private static Variables single_instance = null;

    public String EMAIL_INFO;
    public String PASSWORD_INFO;

    private Variables()
    {
        EMAIL_INFO = "EmailInfo";
        PASSWORD_INFO = "PasswordInfo";
    }

    // static method to create instance of Singleton class
    public static Variables getInstance()
    {
        if (single_instance == null)
            single_instance = new Variables();

        return single_instance;
    }
}
