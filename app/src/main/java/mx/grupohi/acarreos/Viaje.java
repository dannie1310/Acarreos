package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

/**
 * Creado por JFEsquivel on 10/10/2016.
 */

public class Viaje {

    Integer idViaje;
    private Integer idMaterial;
    private Integer idTiro;
    private Integer idOrigen;
    private Integer idCamion;
    private Integer idRuta;
    String fechaSalida;
    String horaSalida;
    String fechaLlegada;
    String horaLlegada;
    String observaciones;
    Camion camion;
    Material material;
    Origen origen;
    Tiro tiro;
    Ruta ruta;

    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;

    private Context context;

    Viaje(Context context) {
        this.context = context;
        this.camion =new Camion(context);
        this.origen = new Origen(context);
        this.tiro=new Tiro(context);
        this.material=new Material(context);
        this.ruta = new Ruta(context);
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = null;
        result = db.insert("viajesnetos", null, data) > -1;

        if (result) {
            Cursor c = db.rawQuery("SELECT ID FROM viajesnetos WHERE Code = '" + data.getAsString("Code") + "'", null);
            try {
                if(c != null && c.moveToFirst()) {
                    this.idViaje = c.getInt(0);
                }
            } finally {
                c.close();
                db.close();
            }
        }
        return result;
    }

    public Viaje find (Integer idViaje) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM viajesnetos WHERE ID = '" + idViaje + "'", null);
        try {
            if (c != null && c.moveToFirst()) {
                this.idCamion = c.getInt(4);
                this.idViaje = c.getInt(0);
                this.idMaterial=c.getInt(11);
                this.idOrigen=c.getInt(5);
                this.idTiro =c.getInt(8);
                this.idRuta = c.getInt(15);
                this.fechaLlegada = c.getString(9);
                this.horaLlegada = c.getString(10);
                this.fechaSalida = c.getString(6);
                this.horaSalida = c.getString(7);
                this.observaciones = c.getString(12);
                this.camion= this.camion.find(this.idCamion);
                this.material=this.material.find(this.idMaterial);
                this.origen = this.origen.find(idOrigen);
                this.tiro = this.tiro.find(idTiro);
                this.ruta = this.ruta.find(idRuta);

                return this;
            } else {
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    @Override
    public String toString() {
        return "Viaje{" +
                "ID='" + idViaje + '\'' +
                ", Camion='" + camion.economico  + '\'' +
                ", Material='" + material.descripcion + '\'' +
                ", Origen='" + origen.descripcion + '\'' +
                ", Destino='"+ tiro.descripcion + '\'' +
                '}';
    }

    public static Integer id() {
        db = db_sca.getWritableDatabase();
        Integer x = 0;
        Cursor c = db.rawQuery("SELECT id FROM viajesnetos ORDER BY id ASC LIMIT 1", null);
        try {
                if(c != null && c.moveToFirst()) {
                     x = c.getInt(0);
                }
            }catch (Exception e){
                e.printStackTrace();
            } finally {
            c.close();
            db.close();
        }
        return  x;
    }

    static JSONObject getJSON(Context context) {
        JSONObject JSON = new JSONObject();
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM viajesnetos ORDER BY id", null);
        try {
            if(c != null && c.moveToFirst()) {
                Integer i = 0;
                do {

                        JSONObject json = new JSONObject();

                        json.put("FechaCarga", c.getString(1));
                        json.put("HoraCarga", c.getString(2));
                        json.put("IdProyecto", c.getString(3));
                        json.put("IdCamion", c.getInt(4));
                        json.put("IdOrigen", c.getString(5));
                        json.put("FechaSalida", c.getString(6));
                        json.put("HoraSalida", c.getString(7));
                        json.put("IdTiro", c.getString(8));
                        json.put("FechaLlegada", c.getString(9));
                        json.put("HoraLlegada", c.getString(10));
                        json.put("IdMaterial", c.getString(11));
                        json.put("Observaciones", c.getString(12));
                        json.put("Creo", c.getString(13));
                        json.put("Estatus", c.getString(14));
                        json.put("Code", c.getString(16));
                        json.put("uidTAG", c.getString(17));
                        json.put("IMEI", c.getString(18));
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
        Cursor c = db.rawQuery("SELECT * FROM viajesnetos",null);
        try {
            return c.getCount();
        } finally {
            c.close();
            db.close();
        }
    }

    public static List<Viaje> getViajes(Context context){
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM viajesnetos ORDER BY 'ID' ASC",null);
        ArrayList  viajes = new ArrayList<Viaje>();
        try {
            if (c != null){
                while (c.moveToNext()){
                    Viaje viaje = new Viaje(context);
                    viaje = viaje.find(c.getInt(0));
                    viajes.add(viaje);
                }
                Collections.sort(viajes, new Comparator<Viaje>() {
                    @Override
                    public int compare(Viaje v1, Viaje v2) {
                        return Integer.valueOf(v2.idViaje).compareTo(Integer.valueOf(v1.idViaje));
                    }
                });

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

    public static String getCode(int idViaje){
        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT Code FROM viajesnetos WHERE id = '" + idViaje + "'", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getString(0);
            }
            else {
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    static void sync(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        db.execSQL("DELETE FROM viajesnetos");
        db.execSQL("DELETE FROM coordenadas");
        db.execSQL("DELETE FROM imagenes_viaje");

        db.close();
    }


    static Boolean isSync(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        Boolean result = true;
        Cursor c = db.rawQuery("SELECT * FROM viajesnetos", null);
        try {
            if(c != null && c.moveToFirst()) {
                result = false;
            }
            return result;
        } finally {
            c.close();
            db.close();
        }
    }
}
