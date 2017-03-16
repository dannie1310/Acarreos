package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Usuario on 16/03/2017.
 */

public class Configuraciones {

    private Integer id;
    public Integer validacion;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Configuraciones(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("configuraciones", null, data) > -1;
        if (result) {
            this.validacion = data.getAsInteger("validacion_placas");
        }
        db.close();
        return result;
    }

    public Configuraciones find(){
        db = db_sca.getWritableDatabase();
        Cursor c= db.rawQuery("SELECT * FROM configuraciones", null);
        try {
            if( c != null && c.moveToFirst()){
                this.id=c.getInt(0);
                this.validacion=c.getInt(1);
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
