package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by DBENITEZ on 21/04/2017.
 */

public class InicioViaje {

    Camion camion;
    Origen origen;
    Material material;

    Integer id;
    Integer idcamion;
    Integer idmaterial;
    Integer idorigen;
    String fecha_origen;
    Integer idusuario;
    String uidTAG;
    String IMEI;
    String version;
    Integer estatus;
    private static SQLiteDatabase db;
    private static DBScaSqlite db_sca;
    private Context context;

    InicioViaje(Context context){
        this.context = context;
        this.camion =new Camion(context);
        this.origen = new Origen(context);
        this.material=new Material(context);
        db_sca = new DBScaSqlite(this.context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = null;
        result = db.insert("inicio_viajes", null, data) > -1;
        if (result) {
            Cursor c = db.rawQuery("SELECT ID FROM inicio_viajes WHERE fecha_origen = '" + data.getAsString("fecha_origen") + "'", null);
            try {
                if(c != null && c.moveToFirst()) {
                    this.id = c.getInt(0);
                }
            } finally {
                c.close();
                db.close();
            }
        }
        return result;
    }

    public InicioViaje find (Integer idViaje) {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM inicio_viajes WHERE ID = '" + idViaje + "'", null);
        try {
            if (c != null && c.moveToFirst()) {
                this.idcamion = c.getInt(c.getColumnIndex("idcamion"));
                this.idmaterial=c.getInt(c.getColumnIndex("idmaterial"));
                this.idorigen=c.getInt(c.getColumnIndex("idorigen"));
                this.fecha_origen =c.getString(c.getColumnIndex("fecha_origen"));
                this.camion= this.camion.find(this.idcamion);
                this.material=this.material.find(this.idmaterial);
                this.origen = this.origen.find(idorigen);
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
