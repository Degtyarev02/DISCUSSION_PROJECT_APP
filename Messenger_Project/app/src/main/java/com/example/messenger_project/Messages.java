package com.example.messenger_project;

public class Messages
{
    private String from;
    private String message;
    private String type;
    private String name;
    private String to;
    private String messageID;
    private String time;
    private String fileName;


    public Messages()
    {

    }

    public Messages(String from, String message, String type, String name, String to, String messageID, String time, String fileName) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.name = name;
        this.to = to;
        this.messageID = messageID;
        this.time = time;
        this.fileName = fileName;
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

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
