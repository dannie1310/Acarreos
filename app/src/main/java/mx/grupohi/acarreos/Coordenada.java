package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

/**
 * Creado por JFEsquivel on 11/10/2016.
 */

class Coordenada {

    Context context;
    SQLiteDatabase db;
    DBScaSqlite db_sca;

    Coordenada (Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    public static Boolean create(ContentValues data, Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Boolean result = db.insert("coordenadas", null, data) > -1;
        db.close();
        return result;
    }

    static JSONObject getJSON(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        JSONObject JSON = new JSONObject();
        try {
            Cursor c = db.rawQuery("SELECT * FROM coordenadas", null);
            if(c != null && c.moveToFirst()) {
                Integer i = 0;
                do {
                    JSONObject json = new JSONObject();

                    json.put("IMEI", c.getString(c.getColumnIndex("IMEI")));
                    json.put("idevento", c.getString(c.getColumnIndex("idevento")));
                    json.put("latitud", c.getString(c.getColumnIndex("latitud")));
                    json.put("longitud", c.getString(c.getColumnIndex("longitud")));
                    json.put("fecha_hora", c.getString(c.getColumnIndex("fecha_hora")));
                    json.put("code", c.getString(c.getColumnIndex("code")));

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

    static Boolean isSync(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        Boolean result = true;
        try (Cursor c = db.rawQuery("SELECT * FROM coordenadas", null)) {
            if(c != null && c.moveToFirst()) {
                result = false;
            }
        }
        return result;
    }
}
