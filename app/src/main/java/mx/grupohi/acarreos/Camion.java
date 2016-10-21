package mx.grupohi.acarreos;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.renderscript.Double2;
import android.util.Log;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Camion {

    public Integer idCamion;
    public String placas;
    public String marca;
    public String modelo;
    public Double ancho;
    public Double largo;
    public Double alto;
    public String economico;
    public Integer capacidad;
    public Integer numero_viajes;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Camion(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Log.i("JSON", data.toString());
        return db.insert("camiones", null, data) > -1;
    }

    public Camion find(Integer idCamion) {
        Cursor c = db.rawQuery("SELECT * FROM camiones WHERE idcamion = '" + idCamion + "'", null);
        if(c != null && c.moveToFirst()) {
            this.idCamion   = c.getInt(0);
            this.placas     = c.getString(1);
            this.marca      = c.getString(2);
            this.modelo     = c.getString(3);
            this.ancho      = c.getDouble(4);
            this.largo      = c.getDouble(5);
            this.alto       = c.getDouble(6);
            this.economico  = c.getString(7);
            this.capacidad  = c.getInt(8);
            this.numero_viajes = c.getInt(9);

            return this;
        } else {
            return null;
        }
    }
}
