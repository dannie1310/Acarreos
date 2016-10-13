package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Material {

    private Integer idMaterial;
    private String descripcion;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Material(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("materiales", null, data) > -1;
        if (result) {
            this.idMaterial = data.getAsInteger("idmaterial");
            this.descripcion = data.getAsString("descripcion");
        }
        return result;
    }

    Material find(String descripcion) {
        Cursor c = db.rawQuery("SELECT * FROM materiales WHERE descripcion = '" + descripcion + "'", null);
        if (c != null && c.moveToFirst()) {
            this.idMaterial = c.getInt(0);
            this.descripcion = c.getString(1);
            return this;
        } else {
            return null;
        }
    }

    ArrayList<String> getArrayListDescripciones() {
        ArrayList<String> data = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM materiales ORDER BY descripcion ASC", null);
        if (c != null && c.moveToFirst())
            try {
                data.add("-- Seleccione --");
                while (c.moveToNext()) {
                    data.add(c.getString(c.getColumnIndex("descripcion")));
                }
            } finally {
                c.close();
            }
        return data;
    }

    ArrayList<String> getArrayListId() {
        ArrayList<String> data = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * from materiales ORDER BY descripcion ASC", null);
        if (c != null && c.moveToFirst())
            try {
                data.add("0");
                while (c.moveToNext()) {
                    data.add(c.getString(c.getColumnIndex("idmaterial")));
                }
            } finally {
                c.close();
            }
        return data;
    }
}
