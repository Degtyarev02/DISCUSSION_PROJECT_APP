package com.example.messenger_project;

public class Messages
{
    private String from, message, type, name;

    public Messages()
    {

    }

    public Messages(String from, String message, String type, String name)
    {
        this.from = from;
        this.message = message;
        this.type = type;
        this.name = name;
    }



    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
