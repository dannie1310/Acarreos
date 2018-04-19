package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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

    public Integer id;
    Integer idcamion;
    Integer idmaterial;
    Integer idorigen;
    String fecha_origen;
    String folio_mina;
    String folio_seg;
    Integer volumen;
    Integer idusuario;
    String uidTAG;
    String IMEI;
    String Code;
    String deductiva;
    Integer idMotivo;
    Integer estatus;
    Integer tipoEsquema;
    Integer idperfil;
    Integer numImpresion;
    Integer tipo_suministro;
    Integer deductiva_entrada;
    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;
    private Context context;

    public InicioViaje(Context context){
        this.context = context;
        this.camion =new Camion(context);
        this.origen = new Origen(context);
        this.material=new Material(context);
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
    }

    public Boolean create(ContentValues data) {
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
                this.idperfil = c.getInt(c.getColumnIndex("idperfil"));
                this.folio_mina = c.getString(c.getColumnIndex("folio_mina"));
                this.folio_seg = c.getString(c.getColumnIndex("folio_seguimiento"));
                this.volumen = c.getInt(c.getColumnIndex("volumen"));
                this.Code = c.getString(c.getColumnIndex("Code"));
                this.numImpresion = c.getInt(c.getColumnIndex("numImpresion"));
                this.tipo_suministro = c.getInt(c.getColumnIndex("tipo_suministro"));
                this.deductiva = c.getString(c.getColumnIndex("deductiva"));
                this.idMotivo = c.getInt(c.getColumnIndex("idMotivo"));
                this.deductiva_entrada = c.getInt(c.getColumnIndex("deductiva_entrada"));
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
        Cursor c = db.rawQuery("SELECT * FROM inicio_viajes ORDER BY ID DESC",null);
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
                    json.put("idperfil", c.getInt(c.getColumnIndex("idperfil")));
                    json.put("foliomina",c.getString(c.getColumnIndex("folio_mina")));
                    json.put("folioseguimiento",c.getString(c.getColumnIndex("folio_seguimiento")));
                    json.put("volumen", c.getInt(c.getColumnIndex("volumen")));
                    json.put("Code",c.getString(c.getColumnIndex("Code")));
                    json.put("numImpresion", c.getInt(c.getColumnIndex("numImpresion")));
                    json.put("tipo_suministro",c.getInt(c.getColumnIndex("tipo_suministro")));
                    json.put("deductiva", c.getString(c.getColumnIndex("deductiva")));
                    json.put("idMotivo", c.getInt(c.getColumnIndex("idMotivo")));
                    json.put("deductiva_entrada", c.getInt(c.getColumnIndex("deductiva_entrada")));
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

    static Boolean isSync(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        Boolean result = true;
        Cursor c = db.rawQuery("SELECT * FROM inicio_viajes", null);
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
    public static String getCode(int ini){
        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT Code FROM inicio_viajes WHERE id = '" + ini + "'", null);
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

    static boolean updateImpresion(Integer id,Integer numImpresion, Context context) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        try{

            data.put("numImpresion", numImpresion+1);

            db.update("inicio_viajes", data, "ID = "+id, null);
            resp = true;
        } finally {
            db.close();
        }
        return resp;
    }

    static Integer numImpresion(Integer id, Context context) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();


        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT numImpresion FROM inicio_viajes WHERE id = '" + id + "'", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getInt(0);
            }
            else {
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public Boolean borrar (Integer inicio){
        db = db_sca.getWritableDatabase();
        return db.delete("inicio_viajes", "ID = "+inicio, null) > 0;
    }
}
