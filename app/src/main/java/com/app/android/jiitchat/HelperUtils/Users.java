package com.app.android.jiitchat.HelperUtils;

import android.widget.ImageView;

/**
 * Created by Kashish on 19-02-2018.
 */

public class Users {

    private String Name;
    private String Image;
    private String Status;
    private String Thumb;

    public Users(){

    }

    public Users(String name, String image, String status, String thumbimage){
        this.Name = name;
        this.Image = image;
        this.Status = status;
        this.Thumb = thumbimage;
    }

    public String getName(){
        return Name;
    }

    public void setName(String name){
        this.Name = name;
    }

    public String getImage(){
        return Image;
    }

    public void setImage(String image){
        this.Image = image;
    }

    public String getStatus(){
        return Status;
    }

    public void setStatus(String status){
        this.Status = status;
    }

    public void setThumb(String thumbimage){
        this.Thumb = thumbimage;
    }
    public String getThumb(){
        return Thumb;
    }

}
