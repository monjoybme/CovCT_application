package com.emory.covCT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
/*
Code designed and Written by : ARYAN VERMA
                               GSOC (Google Summer of Code 2021)
Mail :                         aryanverma19oct@gmail.com
*/
public class LoadAnalysePredictFragment extends Fragment {


    public LoadAnalysePredictFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_load_analyse_predict, container, false);
    }

}
