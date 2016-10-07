package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Ruta {

    private Integer idRuta;
    private String clave;
    private Integer idOrigen;
    private Integer idTiro;
    private Integer totalKm;


    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

     Ruta(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("rutas", null, data) > -1;
        if (result) {
            this.idRuta = data.getAsInteger("idorigen");
            this.clave = data.getAsString("clave");
            this.idOrigen = data.getAsInteger("idorigen");
            this.idTiro  = data.getAsInteger("idtiro");
            this.totalKm = data.getAsInteger("totalkm");
        }
        return result;
    }
}
