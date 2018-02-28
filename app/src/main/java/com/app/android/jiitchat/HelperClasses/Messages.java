package com.app.android.jiitchat.HelperClasses;

/**
 * Created by Kashish on 24-02-2018.
 */

public class Messages {

    private String message;
    private String type;
    private String from;
    private long time;
    private boolean seen;
    private String image;
    private String thumb;



    public Messages(String message, String type, long time, boolean seen, String from, String image, String thumb) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
        this.from = from;
        this.image = image;
        this.thumb = thumb;
    }

    public Messages(){

    }

    public String getImage() {
        return image;
    }

    public String getThumb() {
        return thumb;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    public boolean isSeen() {
        return seen;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

}
