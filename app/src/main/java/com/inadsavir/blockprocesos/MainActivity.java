package com.inadsavir.blockprocesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Stat;
import com.jaredrummler.android.processes.models.Statm;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDataPass {

    private PackageManager packageMager;
    private ListView listview;
    private Context ctx;
    private PackageManager packageManager;
    private Boolean sistema = true;
    public TextView txtEleccion, txtTamanho, txtHistorial;
    private Button btnApps, btnPids;

    ProgressDialog progress = null;
    //    ArrayAdapter adaptador;
    AdaptadorLista adaptador2;
    ArrayList<Aplicacion> lista = null;
    ArrayList<String> appsBloqueadas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

        packageManager = getPackageManager();
        btnApps = (Button) findViewById(R.id.btnApps);
        btnPids = (Button) findViewById(R.id.btnPids);
        listview = (ListView) findViewById(R.id.listaApps);
        txtEleccion = (TextView) findViewById(R.id.txtEleccion);
        txtTamanho = (TextView) findViewById(R.id.txtTamanho);
        txtHistorial = (TextView) findViewById(R.id.textHistorial);
        txtHistorial.setVisibility(View.INVISIBLE);

        btnApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtHistorial.setVisibility(View.INVISIBLE);
                sistema = null;
                ListarApps();
                txtEleccion.setText("TODAS");
            }
        });
        btnPids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    txtHistorial.setVisibility(View.INVISIBLE);
                    verPids();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String nombrePaquete = lista.get(i).nombre;
                String nombreProceso = "Pendiente";
//                lista.get(i).icono
                CargarFragmento(new LFragment(nombrePaquete, nombreProceso));
//                appsBloqueadas.add(getIntent().getStringExtra("AppVigilada"));
            }
        });
    }

    @Override
    public void onDataPass(String data) {
        appsBloqueadas.add(data);
        SharedPreferences prefBloqueos = getSharedPreferences("Bloqueos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefBloqueos.edit();
        editor.putString("Bloqueo_" + appsBloqueadas.size(), data);
        editor.putInt("TamañoBloqueo", appsBloqueadas.size());
        editor.apply();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        //   appsBloqueadas.add(getIntent().getStringExtra("AppVigilada"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                txtHistorial.setVisibility(View.INVISIBLE);
                Volver();
                break;
            case R.id.sistema:
                txtHistorial.setVisibility(View.INVISIBLE);
                sistema = true;
                ListarApps();
                //               btnApps.performClick();
                txtEleccion.setText("SISTEMA");
                break;
            case R.id.terceros:
                txtHistorial.setVisibility(View.INVISIBLE);
                sistema = false;
                ListarApps();
                //               btnApps.performClick();
                txtEleccion.setText("TERCEROS");
                break;
            case R.id.activar:
                activar();
                break;
            case R.id.desactivar:
                desactivar();
                break;
            case R.id.procesoMain:
                txtHistorial.setText(" PROCESO PRINCIPAL ");
                txtHistorial.setVisibility(View.VISIBLE);
                try {
                    Detalles();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.historial:
                txtHistorial.setText(" HISTORIAL ");
                txtHistorial.setVisibility(View.VISIBLE);
                Historial();
                break;
            case R.id.proBloqueados:
                txtHistorial.setText(" PROCESOS BLOQUEADOS");
                txtHistorial.setVisibility(View.VISIBLE);
                Bloqueados();
                break;
            case R.id.activarHora:
                activarHora();
                break;
            case R.id.desactivarHora:
                desactivarHora();
                break;
            case R.id.on:
                enciende();
                break;
            case R.id.off:
                apaga();
                break;
            case R.id.ajustes:
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
                break;
            case R.id.salir:
                finishAffinity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Detalles() throws PackageManager.NameNotFoundException, IOException {
        // Get a list of running apps
        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        ArrayList<String> listaDetalles = new ArrayList<>();
        for (AndroidAppProcess process : processes) {
            // Get some information about the process
            String processName = process.name;
            Stat stat = process.stat();
            int pid = stat.getPid();
            int parentProcessId = stat.ppid();
            long startTime = stat.stime();
            int policy = stat.policy();
            char state = stat.state();
            Statm statm = process.statm();
            long totalSizeOfProcess = statm.getSize();
            long residentSetSize = statm.getResidentSetSize();

            PackageInfo packageInfo = process.getPackageInfo(this, 0);
            String appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            listaDetalles.add("\n"+"Nombre Proceso " +"\n\n\t\t"+ processName + "\n\n\n\t\t\t\t"+ "PID " + pid + "\n\n\t\t\t\t"
                    + "ID Padre " + parentProcessId+ "\n\n\t\t\t\t"+ "Start time " + startTime + "\n\n\t\t\t\t" + "Política "
                    + policy + "\n\n\t\t\t\t" + "Estado " + state + "\n\n\t\t\t\t"+ "Tamañon del proceso "+ totalSizeOfProcess
                    + "\n\n\t\t\t\t" + "Tamaño residente " + residentSetSize);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaDetalles);
        listview.setAdapter(arrayAdapter);
    }

    private void Bloqueados() {
        if (appsBloqueadas.size() == 0) {
            Toast.makeText(this, "No hay bloqueos", Toast.LENGTH_SHORT).show();
        } else {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, appsBloqueadas);
            //  adaptador2 = new AdaptadorLista(MainActivity.this, appsBloqueadas);
            listview.setAdapter(arrayAdapter);
        }
    }

    private void CargarFragmento(Fragment fragment) {
        // create a FragmentManager
        FragmentManager fm = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.contendorlista, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    private void Historial() {
        ArrayList<String> listaProc = new ArrayList<>();
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        listaProc.clear();
        int tamanho = mSharedPreference1.getInt("TamañoListaProcesosAbiertos", 0);
        for (int i = 0; i < tamanho - 1; i++) {
            listaProc.add(mSharedPreference1.getString("Proceso_" + i, null));
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaProc);
        listview.setAdapter(arrayAdapter);
    }

//    public RunningAppProcessInfo verPids() throws IOException, InterruptedException {
//        final int PROCESS_STATE_TOP = 2;
//        RunningAppProcessInfo currentInfo = null;
//        Field field = null;
//        try {
//            field = RunningAppProcessInfo.class.getDeclaredField("processState");
//        } catch (Exception ignored) {
//        }
//        ActivityManager am = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
//        List<RunningAppProcessInfo> appList = am.getRunningAppProcesses();
//        for (RunningAppProcessInfo app : appList) {
//            if (app.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
//                    && app.importanceReasonCode == RunningAppProcessInfo.REASON_UNKNOWN) {
//                Integer state = null;
//                try {
//                    state = field.getInt(app);
//                } catch (Exception e) {
//                }
//                if (state != null && state == PROCESS_STATE_TOP) {
//                    currentInfo = app;
//                    break;
//                }
//            }
//        }
//        return currentInfo;
//    }

    public void verPids() throws IOException, InterruptedException {
        ArrayList pids = new ArrayList();
        Process p = Runtime.getRuntime().exec("ps "); //| findstr "+"com.whatsapp");  //   -A -o PID,USER,NAME");//"sh -c su -c kill -9 pid");
        p.waitFor();
        StringBuffer sb = new StringBuffer();
        InputStreamReader isr = new InputStreamReader(p.getInputStream());
        int ch;
        char[] buf = new char[1024];
        while ((ch = isr.read(buf)) != -1) {
            sb.append(buf, 0, ch);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pids);

        HashMap pMap = new HashMap<String, Integer>();
        String[] processLinesAr = sb.toString().split("\n");
        for (String line : processLinesAr) {
            String[] comps = line.split("[\\s]+");
            if (comps.length != 9)
                return;
            pids.add(line);
            Log.d("APPLIc", line);
            // int pid = Integer.parseInt(comps[1]);
            String pid = comps[1];
            String packageName = comps[2];
            pMap.put(packageName, pid);
            listview.setAdapter(arrayAdapter);
        }
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,
//                pids );

//        lista.setAdapter(arrayAdapter);

////        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
////        List<ActivityManager.RunningAppProcessInfo> services = manager.getRunningAppProcesses();
////        String service1name = services.get(0).processName;
////        consola.setText(services.size());
////
    }

    public void Volver() {
        moveTaskToBack(true);
        apaga();
    }

    private void activarHora() {
        Intent inte = new Intent(this, ServicioAviso.class);
        startService(inte);
    }

    private void desactivarHora() {
        Intent inte = new Intent(this, ServicioAviso.class);
        stopService(inte);
    }

    private void desactivar() {
        Intent inte = new Intent(this, ServicioVigilancia.class);
        stopService(inte);
    }

    private void activar() {
        Intent inte = new Intent(this, ServicioVigilancia.class);
        startService(inte);
    }

    private void apaga() {
        Intent inte = new Intent(this, ServicioIniciado.class);
        stopService(inte);
    }

    private void enciende() {
        //Comenzar el servicio
        Intent inte = new Intent(this, ServicioIniciado.class);
        startService(inte);
    }

    public void ListarApps() {

        progress = ProgressDialog.show(MainActivity.this, null, "Cargando lista ...", false);
        if (sistema == null) {
            lista = buscaTodas(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            adaptador2 = new AdaptadorLista(MainActivity.this, lista);
        } else if (!sistema) {
            lista = buscaAppIcono(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            adaptador2 = new AdaptadorLista(MainActivity.this, lista);
        } else if (sistema) {
            lista = buscaAppIcono(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            adaptador2 = new AdaptadorLista(MainActivity.this, lista);
        }
        listview.setAdapter(adaptador2);
        progress.cancel();
        txtTamanho.setText(String.valueOf(lista.size()));
    }

    @NonNull
    private ArrayList<Aplicacion> buscaAppIcono(List<ApplicationInfo> list) {

        ArrayList<Aplicacion> aplis = new ArrayList<>();
        if (!sistema) {
            for (ApplicationInfo info : list) {
                try {
                    if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                        Intent intent = packageManager.getLaunchIntentForPackage(info.packageName);
                        ResolveInfo app = packageManager.resolveActivity(intent, 0);
                        aplis.add(new Aplicacion(app.activityInfo.applicationInfo.name, app.getIconResource())); //loadIcon(packageMager)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (ApplicationInfo info : list) {
                try {
                    if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                        Intent intent = packageManager.getLaunchIntentForPackage(info.packageName);
                        ResolveInfo app = packageManager.resolveActivity(intent, 0);
                        aplis.add(new Aplicacion(info.packageName, R.mipmap.ic_launcher));// app.getIconResource()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return aplis;
    }

    @NonNull
    private ArrayList<Aplicacion> buscaTodas(List<ApplicationInfo> list) {
        ArrayList<Aplicacion> aplis = new ArrayList<>();
        if (aplis.size() == 0) {
            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> availableActivities = getPackageManager().queryIntentActivities(i, 0);
            for (ResolveInfo ri : availableActivities) {
                Aplicacion apl = new Aplicacion();
                apl.nombre = ri.activityInfo.packageName;
                apl.icono = ri.activityInfo.getIconResource();
                aplis.add(apl);
            }
            Log.i("applist", aplis.toString());
        }
        return aplis;
    }


//    public class LoadApplications extends AsyncTask<Void, Void, Void> {
//
//        private ProgressDialog progress = null;
//        private ArrayAdapter adaptador;
//        AdaptadorLista adaptador2;
//        ArrayList<Aplicacion> lista = null;
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            if (sistema == null) {
//                lista = buscaTodas(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
//                adaptador2 = new AdaptadorLista(MainActivity.this, lista);
//            } else if (!sistema) {
//                lista = buscaAppIcono(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
//                adaptador2 = new AdaptadorLista(MainActivity.this, lista);
//            } else if (sistema) {
//                lista = buscaAppIcono(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
//                adaptador2 = new AdaptadorLista(MainActivity.this, lista);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPreExecute() {
////            super.onPreExecute();
////            progress = ProgressDialog.show(MainActivity.this, null, "Cargando lista ...", false);
//        }
//
//        @Override
//        protected void onPostExecute(Void unused) {
//            super.onPostExecute(unused);
//            listview.setAdapter(adaptador2);
//            progress.cancel();
//            txtTamanho.setText(String.valueOf(lista.size()));
//        }
//
//        @NonNull
//        private ArrayList<Aplicacion> buscaAppIcono(List<ApplicationInfo> list) {
//            ArrayList<Aplicacion> aplis = new ArrayList<>();
//            if (!sistema) {
//                for (ApplicationInfo info : list) {
//                    try {
//                        if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
//                            Intent intent = packageManager.getLaunchIntentForPackage(info.packageName);
//                            ResolveInfo app = packageManager.resolveActivity(intent, 0);
//                            //                    items.add(info.packageName);
////                            aplis.add(new Aplicacion(app.activityInfo.applicationInfo.packageName,app.getIconResource()));
//                            aplis.add(new Aplicacion(app.activityInfo.applicationInfo.name, app.getIconResource())); //loadIcon(packageMager)));
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                for (ApplicationInfo info : list) {
//                    try {
//                        if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
//                            Intent intent = packageManager.getLaunchIntentForPackage(info.packageName);
//                            ResolveInfo app = packageManager.resolveActivity(intent, 0);
//                            //                    items.add(info.packageName);
//                            aplis.add(new Aplicacion(info.packageName, R.mipmap.ic_launcher));// app.getIconResource()));
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            return aplis;
//        }
//
//        @NonNull
//        private ArrayList<Aplicacion> buscaTodas(List<ApplicationInfo> list) {
//            ArrayList<Aplicacion> aplis = new ArrayList<>();
//            if (aplis.size() == 0) {
//                Intent i = new Intent(Intent.ACTION_MAIN, null);
//                i.addCategory(Intent.CATEGORY_LAUNCHER);
//                List<ResolveInfo> availableActivities = getPackageManager().queryIntentActivities(i, 0);
//                for (ResolveInfo ri : availableActivities) {
//                    Aplicacion apl = new Aplicacion();
//                    apl.nombre = ri.activityInfo.packageName;
//                    apl.icono = ri.activityInfo.getIconResource();
//                    aplis.add(apl);
//                }
//                Log.i("applist", aplis.toString());
//            }
//            return aplis;
//        }
//
////        private ArrayList<String> buscaSistema(List<ApplicationInfo> list) {
////            ArrayList<String> items = new ArrayList<>();
////            for (ApplicationInfo info : list) {
////                try {
////                    if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
////                        Intent intent = packageManager.getLaunchIntentForPackage(info.packageName);
////                        ResolveInfo app = packageManager.resolveActivity(intent, 0);
////                        items.add(info.packageName);
////                    }
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
////            }
////            return items;
////        }
//
//        //        private void loadApps(){
//        //            manager = getPackageManager();
//        //            apps = new ArrayList<AppDetail>();
//        //
//        //            if(apps.size()==0) {
//        //                Intent i = new Intent(Intent.ACTION_MAIN, null);
//        //                i.addCategory(Intent.CATEGORY_LAUNCHER);
//        //                List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
//        //                for (ResolveInfo ri : availableActivities) {
//        //                    AppDetail app = new AppDetail();
//        //                    app.label = ri.loadLabel(manager);
//        //                    app.name = ri.activityInfo.packageName;
//        //                    app.icon = ri.activityInfo.getIconResource();
//        //                    app.allowed = false;
//        //                    apps.add(app);
//        //                }
//        //                Log.i("applist", apps.toString());
//        //            }
//        //        }
//    }
}


