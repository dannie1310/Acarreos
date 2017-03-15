package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Usuario on 14/03/2017.
 */

class Motivo {

    private Integer id;
    public String descripcion;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Motivo(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("motivos", null, data) > -1;
        if (result) {
            this.id= Integer.valueOf(data.getAsString("id"));
            this.descripcion = data.getAsString("descripcion");
        }
        db.close();
        return result;
    }

    public Motivo find(Integer idTiro){
        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT * FROM motivos WHERE id = '" + idTiro + "'", null);
        try {
            if( c != null && c.moveToFirst()){
                this.id=c.getInt(0);
                this.descripcion=c.getString(1);
                return this;
            }
            else{
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
        Cursor c = db.rawQuery("SELECT * FROM motivos ORDER BY descripcion ASC", null);
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
        Cursor c = db.rawQuery("SELECT * FROM motivos ORDER BY descripcion ASC", null);
        if (c != null && c.moveToFirst())
            try {
                if (c.getCount() == 1) {
                    data.add(c.getString(c.getColumnIndex("id")));
                } else {
                    data.add("0");
                    data.add(c.getString(c.getColumnIndex("id")));
                    while (c.moveToNext()) {
                        data.add(c.getString(c.getColumnIndex("id")));
                    }
                }
            } finally {
                c.close();
                db.close();
            }
        return data;
    }
}