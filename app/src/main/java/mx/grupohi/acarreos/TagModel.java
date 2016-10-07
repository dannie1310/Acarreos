package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class TagModel {

    private String UID;
    private Integer idCamion;
    private Integer idProyecto;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    TagModel(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("tags", null, data) > -1;
        if (result) {
            this.UID = data.getAsString("uid");
            this.idCamion = data.getAsInteger("idcamion");
            this.idProyecto = data.getAsInteger("idproyecto");
        }
        return result;
    }
}
