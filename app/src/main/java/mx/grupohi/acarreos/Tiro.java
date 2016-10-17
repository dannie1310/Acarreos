package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Tiro {

    private Integer idTiro;
    private String descripcion;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Tiro(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("tiros", null, data) > -1;
        if (result) {
            this.idTiro = Integer.valueOf(data.getAsString("idtiro"));
            this.descripcion = data.getAsString("descripcion");
        }
        return result;
    }

    ArrayList<String> getArrayListDescripciones(Integer idOrigen) {
        ArrayList<String> data = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM tiros WHERE idtiro IN (SELECT idtiro FROM rutas WHERE idorigen = '" + idOrigen + "') ORDER BY descripcion ASC", null);
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

    ArrayList<String> getArrayListId(Integer idOrigen) {
        ArrayList<String> data = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM tiros WHERE idtiro IN (SELECT idtiro FROM rutas WHERE idorigen = '" + idOrigen + "') ORDER BY descripcion ASC", null);
        if (c != null && c.moveToFirst())
            try {
                data.add("0");
                while (c.moveToNext()) {
                    data.add(c.getString(c.getColumnIndex("idtiro")));
                }
            } finally {
                c.close();
            }
        return data;
    }
}
