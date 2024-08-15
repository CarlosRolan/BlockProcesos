package com.inadsavir.blockprocesos;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ServicioVigilancia extends Service {

    ArrayList<String> listaProcesosAbiertos = new ArrayList<>();
    SharedPreferences sharedPref;
    boolean corriendo = false;

    public ServicioVigilancia() {
    }

    MyTask myTask;

    public void onCreate() {
        super.onCreate();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myTask = new MyTask();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //     super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, 1);
            Toast.makeText(this, "Se ha iniciado el servicio de vigilancia", Toast.LENGTH_SHORT).show();
            Log.d("Vigilancia", "Iniciado");
            corriendo = true;
            myTask.execute();
//        try {
//            VigilandoProcesos();
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Se ha detenido el servicio de vigilancia", Toast.LENGTH_SHORT).show();
        Log.d("Vigilancia", "Detenido");
        myTask.cancel(true);
        corriendo=false;

    }

    private class MyTask extends AsyncTask<String, String, String> implements com.inadsavir.blockprocesos.MyTask {

        private String fecha;
        private boolean continuar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            continuar = true;
        }

        @Override
        protected String doInBackground(String... params) {
            while (continuar) {
                try {
                    Log.e("Vigilancia", "Vigilando");
                    fecha = DateFormat.getDateTimeInstance().format(new Date());
                    publishProgress(fecha);
                    //  VigilandoProcesos();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            String topPackageName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                // We get usage stats for the last 10 seconds
                List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
                // Sort the stats by the last time used
                if (stats != null) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                    for (UsageStats usageStats : stats) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
                        topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        long tiempoBack = mySortedMap.get(mySortedMap.lastKey()).getTotalTimeInForeground();
                        Log.e("Vigilancia", topPackageName);
                       SharedPreferences prefBloq=getSharedPreferences("Bloqueos",Context.MODE_PRIVATE);
                       int taman=prefBloq.getInt("TamañoBloqueo",0);
//                        if (!topPackageName.equals("com.inadsavir.blockprocesos")&&!topPackageName.equals("com.android.systemui"))
                        for (int j = 0; j <= taman; j++) {
                            if (topPackageName.equals(prefBloq.getString("Bloqueo_"+ j,null))) {
                                Toast.makeText(getApplicationContext(), "Se ha abierto " + topPackageName, Toast.LENGTH_SHORT).show();
                                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                            }
                        }
                        String registro = topPackageName + "\n" + values[0] + "\n" +
                                "           en BackGround " + tiempoBack / 1000 + " segundos";
                        listaProcesosAbiertos.add(registro);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("TamañoListaProcesosAbiertos", listaProcesosAbiertos.size());
                        for (int i = 0; i < listaProcesosAbiertos.size(); i++) {
                            editor.remove("Proceso_" + i);
                            editor.putString("Proceso_" + i, listaProcesosAbiertos.get(i));
                        }
                        editor.apply();
                    }
                }
            }
        }
//        @Override
//        protected void onProgressUpdate(String... values) {
////            String[] procesos=null;
//            final ActivityManager activityManager = (ActivityManager)  getSystemService(Context.ACTIVITY_SERVICE);
//            final List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(500); //getAppTasks();
//            for (ActivityManager.RunningTaskInfo runningProcesoInfo : services) {
//                int servicio=runningProcesoInfo.numRunning;
//                String nombre= null;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                    nombre = runningProcesoInfo.taskDescription.toString();
//                    if (nombre.equals("com.whatsapp")){
//                        Toast.makeText(getApplicationContext(), "Se ha iniciado WhatsApp "+values[0], Toast.LENGTH_SHORT).show();
////                    return true;
//                    }
//                }
//
//            }
////            return false;
//        }
////            String packageActual = ActivityManager.RunningAppProcessInfo("com.whatsapp",19808,procesos);//getApplication().getPackageName(); //getBaseContext().getPackageName();// getPackageName();
////            Toast.makeText(getApplicationContext(), "Comprobando ", Toast.LENGTH_SHORT).show();
////            Log.d("Servicio", "Comprobando");
////            if (packageActual.equals("com.whatsapp")) {
////                Toast.makeText(getApplicationContext(), "Se ha iniciado WhatsApp "+ values[0], Toast.LENGTH_SHORT).show();
////                Log.d("Servicio", "WhatsApp corriendo");
////                continuar=false;
////            }
////
////        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            continuar = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//            throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
}