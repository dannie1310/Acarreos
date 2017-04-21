package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by DBENITEZ on 21/04/2017.
 */

public class EntradaTiro {

    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;
    private Context context;
    Camion camion;
    Tiro tiro;
    Integer id;
    Integer idcamion;
    Integer idtiro;
    String fecha_entrada;
    Integer idusuario;
    String uidTAG;
    String IMEI;
    String version;
    Integer estatus;

    EntradaTiro(Context context){
        this.context = context;
        this.camion =new Camion(context);
        this.tiro = new Tiro(context);
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = null;
        result = db.insert("entrada_tiros", null, data) > -1;
        return result;
    }

    public EntradaTiro find (Integer id) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM entrada_tiros WHERE ID = '" + id + "'", null);
        try {
            if (c != null && c.moveToFirst()) {
                this.idcamion = c.getInt(c.getColumnIndex("idcamion"));
                this.idtiro=c.getInt(c.getColumnIndex("idtiro"));
                this.fecha_entrada =c.getString(c.getColumnIndex("fecha_entrada"));
                this.camion= this.camion.find(this.idcamion);
                this.tiro = this.tiro.find(idtiro);
                this.idusuario = c.getInt(c.getColumnIndex("idusuario"));
                this.uidTAG = c.getString(c.getColumnIndex("uidTAG"));
                this.IMEI = c.getString(c.getColumnIndex("IMEI"));
                this.version = c.getString(c.getColumnIndex("version"));
                this.estatus = c.getInt(c.getColumnIndex("estatus"));

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
