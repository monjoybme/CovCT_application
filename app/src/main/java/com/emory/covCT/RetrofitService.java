package com.emory.covCT;

import com.emory.covCT.DataModels.ImageOnly;
import com.emory.covCT.DataModels.SaliencyModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitService {

    @Headers("Content-Type: application/json")
    @POST("prediction")
    Call<SaliencyModel> uploadFile(@Body ImageOnly contentImage);
}
