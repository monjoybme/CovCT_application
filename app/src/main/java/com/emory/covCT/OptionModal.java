package com.emory.covCT;


/*
Code designed and Written by : ARYAN VERMA
                               GSOC (Google Summer of Code 2021)
Mail :                         aryanverma19oct@gmail.com
*/
public class OptionModal {
    int image;
    String title;
    String desc;

    public OptionModal(int image, String title, String desc) {
        this.image = image;
        this.title = title;
        this.desc = desc;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
