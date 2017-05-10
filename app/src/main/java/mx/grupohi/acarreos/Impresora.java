package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by DBENITEZ on 10/05/2017.
 */

public class Impresora {

    private Context context;
    private SQLiteDatabase db;
    private DBScaSqlite db_sca;
    Integer id;
    String MAC;

    Impresora (Context context){
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("impresoras", null, data) > -1;
        if (result) {
            this.MAC = data.getAsString("MAC");
        }
        db.close();
        return result;
    }

    public Impresora find(Integer MAC){
        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT * FROM impresoras WHERE MAC = '" + MAC + "'", null);
        try {
            if( c != null && c.moveToFirst()){
                this.id=c.getInt(0);
                this.MAC=c.getString(1);
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
