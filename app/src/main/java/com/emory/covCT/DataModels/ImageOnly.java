package com.emory.covCT.DataModels;

import com.google.gson.annotations.SerializedName;

public class ImageOnly {
    @SerializedName("input_image")
    String input_image;

    public ImageOnly(String input_image) {
        this.input_image = input_image;
    }

    public String getInput_image() {
        return input_image;
    }

    public void setInput_image(String input_image) {
        this.input_image = input_image;
    }
}
