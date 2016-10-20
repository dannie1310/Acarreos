package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;

import org.json.JSONObject;

/**
 * Creado por JFEsquivel on 10/10/2016.
 */

class Viaje {

    public Integer idViaje;
    public Integer idMaterial;
    public Integer idTiro;
    public Integer idOrigen;
    public Integer idCamion;
    public Integer idRuta;
    public String fechaSalida;
    public String horaSalida;
    public String fechaLlegada;
    public String horaLlegada;
    public String observaciones;
    public Camion camion;
    public Material material;
    public Origen origen;
    public Tiro tiro;
    public Ruta ruta;


    private static SQLiteDatabase db;
    private DBScaSqlite db_sca;

    private Context context;

    Viaje(Context context) {
        this.context = context;
        this.camion =new Camion(context);
        this.origen = new Origen(context);
        this.tiro=new Tiro(context);
        this.material=new Material(context);
        this.ruta = new Ruta(context);
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("viajesnetos", null, data) > -1;
        if (result) {
            Cursor c = db.rawQuery("SELECT ID FROM viajesnetos WHERE Code = '" + data.getAsString("Code") + "'", null);
            if(c != null && c.moveToFirst()) {
                this.idViaje = c.getInt(0);
            }
        }
        return result;
    }
    public Viaje find (Integer idViaje) {
        Cursor c = db.rawQuery("SELECT * FROM viajesnetos WHERE ID = '" + idViaje + "'", null);
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
        }
        return null;
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

    static JSONObject getJSON() {
        JSONObject JSON = new JSONObject();
        try {
            Cursor c = db.rawQuery("SELECT * FROM viajesnetos", null);
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

                    JSON.put(i + "", json);
                    i++;
                } while (c.moveToNext());
            }
            assert c != null;
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSON;
    }

    static void sync() {
        db.execSQL("DELETE FROM viajesnetos");
        db.execSQL("DELETE FROM coordenadas");
    }

    static Boolean isSync() {
        Boolean result = true;
        try (Cursor c = db.rawQuery("SELECT * FROM viajesnetos", null)) {
            if(c != null && c.moveToFirst()) {
                result = false;
            }
        }
        return result;
    }
}
