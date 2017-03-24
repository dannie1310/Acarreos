package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Usuario on 24/03/2017.
 */

public class Checador {

    Integer idCrecador;
    String nombre;
    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Checador(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("checadores", null, data) > -1;
        if (result) {
            this.idCrecador = Integer.valueOf(data.getAsString("idChecador"));

        }
        db.close();
        return result;
    }

    void destroy() {
        db = db_sca.getWritableDatabase();
        db.execSQL("DELETE FROM checadores");
        db.close();
    }

    String findNombre(Integer id) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM checadores WHERE idChecador = '" + id + "'", null);
        try {
            if (c != null && c.moveToFirst()) {
               String nombre = c.getString(1);
                return nombre;
            } else {
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

}
