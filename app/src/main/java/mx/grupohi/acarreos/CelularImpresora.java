package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by DBENITEZ on 11/05/2017.
 */

public class CelularImpresora {

    private Context context;
    private SQLiteDatabase db;
    private DBScaSqlite db_sca;
    Integer id;
    String IMEI;
    String MAC;

    CelularImpresora (Context context){
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("celular_impresora", null, data) > -1;
        if (result) {
            this.IMEI = data.getAsString("IMEI");
        }
        db.close();
        return result;
    }

    public CelularImpresora find(String IMEI){
        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT * FROM celular_impresora WHERE IMEI = '" + IMEI + "'", null);
        try {
            if( c != null && c.moveToFirst()){
                this.id=c.getInt(0);
                this.IMEI=c.getString(1);
                this.MAC= c.getString(2);
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

}
