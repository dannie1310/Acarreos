package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;
import com.crashlytics.android.Crashlytics;
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

    public Integer idViaje;
    Integer idMaterial;
    Integer idTiro;
    Integer idOrigen;
    Integer idCamion;
    Integer idRuta;
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
    String deductiva;
    String deductiva_origen;
    String deductiva_entrada;
    Integer idmotivo_origen;
    Integer idmotivo_entrada;
    String FolioRandom;
    Integer idmotivo;
    String primerToque;
    String cubicacion;
    Integer tipoEsquema;
    Integer numImpresion;
    Integer idperfil;
    String creo;
    String uidTAG;
    String folio_mina;
    String folio_seguimiento;
    Integer tipoViaje;
    String latitud_origen;
    String longitud_origen;
    String latitud_tiro;
    String longitud_tiro;
    public Integer Estatus;

    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;

    private Context context;

    public Viaje(Context context) {
        this.context = context;
        this.camion =new Camion(context);
        this.origen = new Origen(context);
        this.tiro=new Tiro(context);
        this.material=new Material(context);
        this.ruta = new Ruta(context);
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
    }

    public Boolean create(ContentValues data) {
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
                if(idOrigen != 0){
                    this.origen = this.origen.find(idOrigen);
                }
                if(idRuta != 0){
                    this.ruta = this.ruta.find(idRuta);
                }
                this.camion= this.camion.find(this.idCamion);
                this.material=this.material.find(this.idMaterial);
                this.tiro = this.tiro.find(idTiro);
                this.deductiva = c.getString(c.getColumnIndex("deductiva"));
                this.FolioRandom = c.getString(c.getColumnIndex("FolioRandom"));
                this.idmotivo = c.getInt(c.getColumnIndex("idMotivo"));
                this.primerToque = c.getString(c.getColumnIndex("primerToque"));
                this.cubicacion = c.getString(c.getColumnIndex("cubicacion"));
                this.tipoEsquema = c.getInt(c.getColumnIndex("tipoEsquema"));
                this.numImpresion = c.getInt(c.getColumnIndex("numImpresion"));
                this.idperfil = c.getInt(c.getColumnIndex("idperfil"));
                this.creo = c.getString(c.getColumnIndex("Creo"));
                this.uidTAG = c.getString(c.getColumnIndex("uidTAG"));
                this.folio_seguimiento = c.getString(c.getColumnIndex("folio_seguimiento"));
                this.folio_mina = c.getString(c.getColumnIndex("folio_mina"));
                this.deductiva_origen = c.getString(c.getColumnIndex("deductiva_origen"));
                this.deductiva_entrada = c.getString(c.getColumnIndex("deductiva_entrada"));
                this.idmotivo_origen = c.getInt(c.getColumnIndex("idmotivo_origen"));
                this.idmotivo_entrada = c.getInt(c.getColumnIndex("idmotivo_entrada"));
                this.tipoViaje = c.getInt(c.getColumnIndex("tipoViaje"));
                this.Estatus = c.getInt(c.getColumnIndex("Estatus"));
                this.latitud_origen = c.getString(c.getColumnIndex("latitud_origen"));
                this.longitud_origen = c.getString(c.getColumnIndex("longitud_origen"));
                this.latitud_tiro = c.getString(c.getColumnIndex("latitud_tiro"));
                this.longitud_tiro = c.getString(c.getColumnIndex("longitud_tiro"));
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
                        json.put("CodeImagen",c.getString(19));
                        json.put("CodeRandom", c.getString(21));
                        json.put("CreoPrimerToque", c.getString(c.getColumnIndex("primerToque")));
                        json.put("CubicacionCamion", c.getString(c.getColumnIndex("cubicacion")));
                        json.put("IdPerfil", c.getInt(c.getColumnIndex("idperfil")));
                        json.put("folioMina", c.getString(c.getColumnIndex("folio_mina")));
                        json.put("folioSeguimiento", c.getString(c.getColumnIndex("folio_seguimiento")));
                        json.put("numImpresion", c.getString(c.getColumnIndex("numImpresion")));
                        json.put("volumen_origen", c.getString(c.getColumnIndex("deductiva_origen")));
                        json.put("volumen_entrada", c.getString(c.getColumnIndex("deductiva_entrada")));
                        json.put("volumen", c.getString(c.getColumnIndex("deductiva")));
                        json.put("tipoViaje", c.getString(c.getColumnIndex("tipoViaje")));
                        json.put("latitud_origen", c.getString(c.getColumnIndex("latitud_origen")));
                        json.put("longitud_origen", c.getString(c.getColumnIndex("longitud_origen")));
                        json.put("latitud_tiro", c.getString(c.getColumnIndex("latitud_tiro")));
                        json.put("longitud_tiro", c.getString(c.getColumnIndex("longitud_tiro")));

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
        Cursor c = db.rawQuery("SELECT * FROM viajesnetos WHERE Estatus = 1 ORDER BY ID DESC",null);
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

    public static String getCodeImagen(int idViaje){
        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT CodeImagen FROM viajesnetos WHERE id = '" + idViaje + "'", null);
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
        db.execSQL("DELETE FROM inicio_viajes");

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

    static boolean updateImpresion(Integer idViaje,Integer numImpresion, Context context) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        try{

            data.put("numImpresion", numImpresion+1);

            db.update("viajesnetos", data, "ID = "+idViaje, null);
            resp = true;
        } finally {
            db.close();
        }
        return resp;
    }

    static Integer numImpresion(Integer idViaje, Context context) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();


        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT numImpresion FROM viajesnetos WHERE id = '" + idViaje + "'", null);
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

    public static Integer findCode(String Code){
        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT ID FROM viajesnetos WHERE Code = '" + Code + "'", null);
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
        return db.delete("viajesnetos", "ID = "+inicio, null) > 0;
    }

    public boolean updateEstado(Integer idViaje,Integer estado) {
        boolean resp=false;
        ContentValues data = new ContentValues();
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        try{
            data.put("Estatus", estado);
            db.update("viajesnetos", data, "ID = "+idViaje, null);
            resp = true;
        }catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
            resp = false;
        }
        db.close();
        return resp;
    }

    public static Integer findViajeInconcluso(Integer idCamion, String fecha, String horaInicio, String horaFinal){
        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT ID FROM viajesnetos WHERE IdCamion = "+idCamion +" and Estatus in (3,4) and FechaCarga = '"+fecha+"' and HoraCarga between '"+horaInicio+"' and '"+horaFinal+"'", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getInt(0);
            }
            else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
            return null;
        }  finally{
            c.close();
            db.close();
        }
    }

}
