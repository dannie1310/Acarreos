package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
}
