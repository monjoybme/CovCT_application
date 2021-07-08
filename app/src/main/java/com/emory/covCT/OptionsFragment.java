package com.emory.covCT;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emory.covCT.Adapters.RecyclerOptionAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class OptionsFragment extends Fragment {

    ArrayList<OptionModal> options ;
    RecyclerOptionAdapter adapter ;
    RecyclerView recyclerView;

    public OptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        options = new ArrayList<>();
        options.add(new OptionModal(R.drawable.lungs_option,"Analyse Lungs CT Scan","Distinguish between the pneumonia and covid-19 using AI"));
        options.add(new OptionModal(R.drawable.xray_option,"Chest X-Ray Scan analysis","Analyse chest x-ray scans for the presence of Covid-19"));
        adapter = new RecyclerOptionAdapter(getContext(),options);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_options, container, false);
        recyclerView = v.findViewById(R.id.recylerview_option);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return v;
    }

}
