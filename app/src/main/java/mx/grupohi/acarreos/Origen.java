package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Origen {

    public Integer idOrigen;
    public String descripcion;
    public Integer estado;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Origen(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        try{
            return db.insert("origenes", null, data) > -1;
        } finally {
            db.close();
        }
    }

    Origen find(Integer idOrigen) {
        db = db_sca.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM origenes WHERE idorigen = '" + idOrigen + "'", null);
        try {
            if (c != null && c.moveToFirst()) {
                this.idOrigen = c.getInt(0);
                this.descripcion = c.getString(1);
                this.estado = c.getInt(2);

                return this;
            } else {
                return null;
            }
        } finally {
            assert c != null;
            c.close();
            db.close();
        }
    }

    ArrayList<String> getArrayListDescripciones() {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM origenes ORDER BY descripcion ASC", null);
        try {
            if (c != null && c.moveToFirst()) {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("descripcion")));
                } else {
                    data.add("-- Seleccione --");
                    data.add(c.getString(c.getColumnIndex("descripcion")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("descripcion")));
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
        db = db_sca.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM origenes ORDER BY descripcion ASC", null);
        try {
            if (c != null && c.moveToFirst()) {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("idorigen")));
                } else {
                    data.add("0");
                    data.add(c.getString(c.getColumnIndex("idorigen")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("idorigen")));
                    }
                }
            }
        } finally {
            c.close();
            db.close();
        }
        return data;
    }

    ArrayList<String> getArrayListDescripcionesOrigenTiro(Integer idtiro) {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM origenes WHERE idorigen IN (SELECT idorigen FROM rutas WHERE idtiro = '"+idtiro+"') ORDER BY descripcion ASC", null);
        try {
            if (c != null && c.moveToFirst()) {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("descripcion")));
                } else {
                    data.add("-- Seleccione --");
                    data.add(c.getString(c.getColumnIndex("descripcion")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("descripcion")));
                    }
                }
            }
        } finally {
            c.close();
            db.close();
        }
        return data;
    }
    ArrayList<String> getArrayListId(Integer idtiro) {
        ArrayList<String> data = new ArrayList<>();
        db = db_sca.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM origenes WHERE idorigen IN (SELECT idorigen FROM rutas WHERE idtiro = '"+idtiro+"') ORDER BY descripcion ASC", null);
        try {
            if (c != null && c.moveToFirst()) {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("idorigen")));
                } else {
                    data.add("0");
                    data.add(c.getString(c.getColumnIndex("idorigen")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("idorigen")));
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
