package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Origen {

    private Integer idOrigen;
    private String descripcion;
    private Integer estado;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Origen(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("origenes", null, data) > -1;
        if (result) {
            this.idOrigen = data.getAsInteger("idorigen");
            this.descripcion = data.getAsString("descripcion");
            this.estado = data.getAsInteger("estado");
        }
        return result;
    }
}
