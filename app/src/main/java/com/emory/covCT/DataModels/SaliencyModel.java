package com.emory.covCT.DataModels;

import com.google.gson.annotations.SerializedName;

public class SaliencyModel {
    @SerializedName("saliency_map")
    String saliency_map;
    @SerializedName("predicted_class")
    String predicted_class;

    public SaliencyModel(String saliency_map, String predicted_class) {
        this.saliency_map = saliency_map;
        this.predicted_class = predicted_class;
    }

    public String getSaliency_map() {
        return saliency_map;
    }

    public void setSaliency_map(String saliency_map) {
        this.saliency_map = saliency_map;
    }

    public String getPredicted_class() {
        return predicted_class;
    }

    public void setPredicted_class(String predicted_class) {
        this.predicted_class = predicted_class;
    }
}
