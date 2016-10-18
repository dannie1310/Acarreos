package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;

/**
 * Creado por JFEsquivel on 10/10/2016.
 */

public class Viaje {

    public Integer idViaje;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    private Context context;

    Viaje(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("viajesnetos", null, data) > -1;
        if (result) {
            Cursor c = db.rawQuery("SELECT ID FROM viajesnetos WHERE Code = '" + data.getAsString("Code") + "'", null);
            if(c != null && c.moveToFirst()) {
                this.idViaje = c.getInt(0);
            }
        }
        return result;
    }
}
