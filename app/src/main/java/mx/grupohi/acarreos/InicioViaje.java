package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by DBENITEZ on 21/04/2017.
 */

public class InicioViaje {

    Camion camion;
    Origen origen;
    Material material;

    Integer id;
    Integer idcamion;
    Integer idmaterial;
    Integer idorigen;
    String fecha_origen;
    Integer idusuario;
    String uidTAG;
    String IMEI;
    Integer estatus;
    Integer tipoEsquema;
    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;
    private Context context;

    InicioViaje(Context context){
        this.context = context;
        this.camion =new Camion(context);
        this.origen = new Origen(context);
        this.material=new Material(context);
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = null;
        result = db.insert("inicio_viajes", null, data) > -1;
        if (result) {
            Cursor c = db.rawQuery("SELECT ID FROM inicio_viajes WHERE fecha_origen = '" + data.getAsString("fecha_origen") + "'", null);
            try {
                if(c != null && c.moveToFirst()) {
                    this.id = c.getInt(0);
                }
            } finally {
                c.close();
                db.close();
            }
        }
        return result;
    }

    public InicioViaje find (Integer idViaje) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM inicio_viajes WHERE ID = '" + idViaje + "'", null);
        try {
            if (c != null && c.moveToFirst()) {
                this.id = c.getInt(c.getColumnIndex("ID"));
                this.idcamion = c.getInt(c.getColumnIndex("idcamion"));
                this.idmaterial=c.getInt(c.getColumnIndex("idmaterial"));
                this.idorigen=c.getInt(c.getColumnIndex("idorigen"));
                this.fecha_origen =c.getString(c.getColumnIndex("fecha_origen"));
                this.camion= this.camion.find(this.idcamion);
                this.material=this.material.find(this.idmaterial);
                this.origen = this.origen.find(idorigen);
                this.idusuario = c.getInt(c.getColumnIndex("idusuario"));
                this.uidTAG = c.getString(c.getColumnIndex("uidTAG"));
                this.IMEI = c.getString(c.getColumnIndex("IMEI"));
                this.estatus = c.getInt(c.getColumnIndex("estatus"));
                this.tipoEsquema = c.getInt(c.getColumnIndex("tipoEsquema"));

                return this;
            } else {
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public static List<InicioViaje> getViajes(Context context){
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM inicio_viajes ORDER BY 'ID' ASC",null);
        ArrayList viajes = new ArrayList<InicioViaje>();
        try {
            if (c != null){
                while (c.moveToNext()){
                    InicioViaje viaje = new InicioViaje(context);
                    viaje = viaje.find(c.getInt(0));
                    viajes.add(viaje);
                }
                return viajes;
            }
            else {
                return new ArrayList<>();
            }
        } finally {
            c.close();
            db.close();
        }
    }

    static JSONObject getJSON(Context context) {
        JSONObject JSON = new JSONObject();
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM inicio_viajes ORDER BY id", null);
        try {
            if(c != null && c.moveToFirst()) {
                Integer i = 0;
                do {

                    JSONObject json = new JSONObject();

                    json.put("idcamion", c.getString(c.getColumnIndex("idcamion")));
                    json.put("idmaterial", c.getString(c.getColumnIndex("idmaterial")));
                    json.put("idorigen", c.getString(c.getColumnIndex("idorigen")));
                    json.put("fecha_origen", c.getString(c.getColumnIndex("fecha_origen")));
                    json.put("idusuario", c.getString(c.getColumnIndex("idusuario")));
                    json.put("uidTAG", c.getString(c.getColumnIndex("uidTAG")));
                    json.put("IMEI", c.getString(c.getColumnIndex("IMEI")));
                    json.put("estatus", c.getString(c.getColumnIndex("estatus")));
                    json.put("tipoEsquema", c.getString(c.getColumnIndex("tipoEsquema")));


                    JSON.put(i + "", json);
                    i++;

                } while (c.moveToNext());


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            db.close();
        }

        return JSON;
    }

    static Integer getCount(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM inicio_viajes",null);
        try {
            return c.getCount();
        } finally {
            c.close();
            db.close();
        }
    }

}
