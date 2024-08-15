package com.inadsavir.blockprocesos;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServicioAviso extends Service {

    Hora hora;

    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Servicio Aviso Horario creado", Toast.LENGTH_SHORT).show();
        hora = new Hora();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //     super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, 1);
        Toast.makeText(this, "Se ha iniciado el servicio de hora", Toast.LENGTH_SHORT).show();
        Log.d("Servicio", "Iniciado Hora");
        hora.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Se ha detenido el servicio de hora", Toast.LENGTH_SHORT).show();
        Log.d("Servicio", "Detenido Hora");
        hora.cancel(true);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Sin implementar");
        //return null;
    }

    private class Hora extends AsyncTask<String, String, String> implements com.inadsavir.blockprocesos.MyTask {
        private DateFormat dateFormat;
        private String date;
        private boolean cent;

        @Override protected void onPreExecute() {
            super.onPreExecute();
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            cent = true;
        }

        @Override protected String doInBackground(String... params) {
            while (cent){
                date = dateFormat.format(new Date());
                try {
                    publishProgress(date);
                    // Stop 5s
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(getApplicationContext(), "Hora actual: " + values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            cent = false;
        }
    }
}
