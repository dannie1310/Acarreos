package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Ruta {

    private Integer idRuta;
    private String clave;
    private Integer idOrigen;
    private Integer idTiro;
    private Integer totalKm;


    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

     Ruta(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("rutas", null, data) > -1;
        if (result) {
            this.idRuta = data.getAsInteger("idorigen");
            this.clave = data.getAsString("clave");
            this.idOrigen = data.getAsInteger("idorigen");
            this.idTiro  = data.getAsInteger("idtiro");
            this.totalKm = data.getAsInteger("totalkm");
        }
        db.close();
        return result;
    }

    Ruta find(Integer idRuta) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM rutas WHERE idruta = '" + idRuta + "'", null);
        try {
            if(c != null && c.moveToFirst()) {
                this.idRuta = c.getInt(0);
                this.clave = c.getString(1);
                this.idOrigen = c.getInt(2);
                this.idTiro = c.getInt(3);
                this.totalKm = c.getInt(4);
                return this;
            } else {
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    ArrayList<String> getArrayListDescripciones(Integer idOrigen, Integer idTiro) {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM rutas WHERE idorigen = '" + idOrigen + "' AND idtiro = '" + idTiro + "' ORDER BY clave ASC", null);
        try {
            if (c != null && c.moveToFirst()) {
                if (c.getCount() == 1) {
                    data.add(c.getString(1) + " - " + c.getString(0) + " " + c.getString(4) + " KM");
                } else {
                    data.add("-- Seleccione --");
                    data.add(c.getString(1) + " - " + c.getString(0) + " " + c.getString(4) + " KM");
                    while (c.moveToNext()) {
                        data.add(c.getString(1) + " - " + c.getString(0) + " " + c.getString(4) + " KM");
                    }
                }
            }
        } finally {
            c.close();
            db.close();
        }
        return data;
    }

    ArrayList<String> getArrayListId(Integer idOrigen, Integer idTiro) {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM rutas WHERE idorigen = '" + idOrigen + "' AND idtiro = '" + idTiro + "' ORDER BY clave ASC", null);
        try {
            if (c != null && c.moveToFirst()) {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("idruta")));
                } else {
                    data.add("0");
                    data.add(c.getString(c.getColumnIndex("idruta")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("idruta")));
                    }
                }
            }
        } finally {
            c.close();
            db.close();
        }
        return data;
    }

    @Override
    public String toString() {
        return clave + " - " + idRuta + ", Distancia " + totalKm + " km";
    }
}
