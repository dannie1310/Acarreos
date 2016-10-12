package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
}
