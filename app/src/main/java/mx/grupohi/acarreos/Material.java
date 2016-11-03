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

class Material {

    public Integer idMaterial;
    public String descripcion;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Material(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("materiales", null, data) > -1;
        if (result) {
            this.idMaterial = data.getAsInteger("idmaterial");
            this.descripcion = data.getAsString("descripcion");
        }
        db.close();
        return result;
    }

    Material find(Integer idMaterial) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM materiales WHERE idmaterial = '" + idMaterial + "'", null);
        try {
            if (c != null && c.moveToFirst()) {
                this.idMaterial = c.getInt(0);
                this.descripcion = c.getString(1);
                return this;
            } else {
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    ArrayList<String> getArrayListDescripciones() {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM materiales ORDER BY descripcion ASC", null);
        if (c != null && c.moveToFirst())
            try {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("descripcion")));
                } else {
                    data.add("-- Seleccione --");
                    data.add(c.getString(c.getColumnIndex("descripcion")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("descripcion")));
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
        Cursor c = db.rawQuery("SELECT * from materiales ORDER BY descripcion ASC", null);
        if (c != null && c.moveToFirst())
            try {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("idmaterial")));
                } else {
                    data.add("0");
                    data.add(c.getString(c.getColumnIndex("idmaterial")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("idmaterial")));
                    }
                }
            } finally {
                c.close();
                db.close();
            }
        return data;
    }
}
