package com.inadsavir.blockprocesos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.opengl.Visibility;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Stat;
import com.jaredrummler.android.processes.models.Statm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SuppressLint("ValidFragment")
public class LFragment extends Fragment {

    private String nombrePaquete, nombreProceso;
    OnDataPass dataPasser;
    Switch elector;
    TextView txtnombPaquete, txtnombProceso;

    @SuppressLint("ValidFragment")
    public LFragment(String nombrePaquete, String nombreProceso) {
        this.nombrePaquete = nombrePaquete;
        this.nombreProceso = nombreProceso;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_l, container, false);
        txtnombPaquete = (TextView) vista.findViewById(R.id.textPaquete);
        txtnombProceso = (TextView) vista.findViewById(R.id.textProceso);
        txtnombPaquete.setText(nombrePaquete);
        txtnombProceso.setText(nombreProceso);

        ((Button) vista.findViewById(R.id.aceptar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().
                        remove(getFragmentManager().findFragmentById(R.id.contendorlista)).commit();
            }
        });

        elector = (Switch) vista.findViewById(R.id.swElector);
        elector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    passData(nombrePaquete);
                }
            }
        });
        return vista;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dataPasser = (OnDataPass) activity;
    }

    public void passData(String data) {
        dataPasser.onDataPass(data);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        Intent intent=new Intent(getContext(),MainActivity.class);
//        intent.putExtra("AppVigilada",nombrePaquete);
        // passData(nombrePaquete);
        // startActivity(intent);
//        getFragmentManager().beginTransaction().
//                remove(getFragmentManager().findFragmentById(R.id.contendorlista)).commit();
    }
}
