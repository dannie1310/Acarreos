package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class TagModel {

    public String UID;
    public Integer idCamion;
    public Integer idProyecto;

    private Context context;

    private static SQLiteDatabase db;
    private DBScaSqlite db_sca;

    TagModel(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("tags", null, data) > -1;
        if (result) {
            this.UID = data.getAsString("uid");
            this.idCamion = data.getAsInteger("idcamion");
            this.idProyecto = data.getAsInteger("idproyecto");
        }
        db.close();
        return result;
    }

    TagModel find(String UID, Integer idCamion, Integer idProyecto) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM tags WHERE uid = '" +  UID + "' AND idcamion = '" + idCamion + "' AND idproyecto = '" + idProyecto + "'", null);
        try {
            if(c != null && c.moveToFirst()) {
                this.idProyecto = c.getInt(c.getColumnIndex("idproyecto"));
                this.idCamion = c.getInt(c.getColumnIndex("idcamion"));
                this.UID = c.getString(c.getColumnIndex("uid"));
                return this;
            } else {
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }
}
