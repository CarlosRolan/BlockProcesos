package com.inadsavir.blockprocesos;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorLista extends BaseAdapter {

    private ArrayList<Aplicacion> listaApps;
    private Context context;
    private PackageManager pack;

    public AdaptadorLista(Activity context, ArrayList<Aplicacion> listaApps) {
        this.context = context;
        this.listaApps = listaApps;
    }

    static class ViewHolder {
        TextView nombre;
        ImageView icono;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            holder = new AdaptadorLista.ViewHolder();//inflater.inflate(R.layout.lista,null);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.lista,null);
            holder.nombre = (TextView) convertView.findViewById((R.id.txtNombre));
            holder.icono = (ImageView) convertView.findViewById((R.id.imIcono));
            convertView.setTag(holder);
        }else{
            holder = (AdaptadorLista.ViewHolder) convertView.getTag();
            Aplicacion apli = listaApps.get(position);
        }
      //  Bitmap fotoIconoResize = Bitmap.createScaledBitmap(fotoIcono, 50, 50, false);
        try {
            holder.nombre.setText(listaApps.get(position).getNombre());
            holder.icono.setImageResource(listaApps.get(position).getIcono());
         //   ImageView appIcon = (ImageView)convertView.findViewById(R.id.item_app_icon);
            String packageName = listaApps.get(position).getNombre();
            try {
                pack= context.getPackageManager();
                Drawable icon = pack.getApplicationIcon(packageName);
                holder.icono.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return listaApps.size();
    }

    @Override
    public Object getItem(int posicion) {
        return listaApps.get(posicion);
    }

    @Override
    public long getItemId(int posicion) {
        return posicion;
    }
}
