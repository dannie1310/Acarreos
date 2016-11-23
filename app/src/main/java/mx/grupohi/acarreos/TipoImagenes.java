package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Usuario on 22/11/2016.
 */

public class TipoImagenes {

    Context context;
    private static SQLiteDatabase db;
    private DBScaSqlite db_sca;
    Integer id;
    String descripcion;

    TipoImagenes(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("tipos_imagenes", null, data) > -1;
        if (result) {
            this.id = data.getAsInteger("id");
            this.descripcion = data.getAsString("descripcion");
        }
        db.close();
        return result;
    }

    ArrayList<String> getArrayListTipos() {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT * FROM tipos_imagenes ORDER BY id ASC", null);
        try {
            if (c != null && c.moveToFirst()) {
                if (c.getCount() == 1) {
                    data.add(c.getString(0) + " - " + c.getString(1));
                } else {
                    data.add("-- Seleccione --");
                    data.add(c.getString(0) + " - " + c.getString(1));
                    while (c.moveToNext()) {
                        data.add(c.getString(0) + " - " + c.getString(1));
                    }
                }
            }
        } finally {
            c.close();
            db.close();
        }
        return data;
    }

    ArrayList<String> getArrayListId() {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT * FROM tipos_imagenes ORDER BY id ASC", null);
        try {
            if (c != null && c.moveToFirst()) {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("id")));
                } else {
                    data.add("0");
                    data.add(c.getString(c.getColumnIndex("id")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("id")));
                    }
                }
            }
        } finally {
            c.close();
            db.close();
        }
        return data;
    }
}
