package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

/**
 * Creado por JFEsquivel on 11/10/2016.
 */

public class Coordenada {

    Context context;
    static SQLiteDatabase db;
    DBScaSqlite db_sca;

    Coordenada (Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    public static Boolean create(ContentValues data) {
        Boolean result = db.insert("coordenadas", null, data) > -1;
        db.close();
        return result;
    }

    static JSONObject getJSON() {
        JSONObject JSON = new JSONObject();
        try {
            Cursor c = db.rawQuery("SELECT * FROM coordenadas", null);
            if(c != null && c.moveToFirst()) {
                Integer i = 0;
                do {
                    JSONObject json = new JSONObject();

                    json.put("IMEI", c.getString(1));
                    json.put("idevento", c.getString(2));
                    json.put("latitud", c.getString(3));
                    json.put("longitud", c.getInt(4));
                    json.put("fecha_hora", c.getString(5));
                    json.put("code", c.getString(6));

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
}
