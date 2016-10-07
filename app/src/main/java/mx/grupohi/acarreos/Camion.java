package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.renderscript.Double2;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Camion {

    private Integer idCamion;
    private String placas;
    private String marca;
    private String modelo;
    private Double ancho;
    private Double largo;
    private Double alto;
    private String economico;
    private Integer capacidad;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Camion(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("camiones", null, data) > -1;
        if (result) {
            this.idCamion = data.getAsInteger("idcamion");
            this.placas = data.getAsString("placas");
            this.marca = data.getAsString("marca");
            this.modelo = data.getAsString("modelo");
            this.ancho = data.getAsDouble("ancho");
            this.largo = data.getAsDouble("largo");
            this.alto = data.getAsDouble("alto");
            this.economico = data.getAsString("economico");
            this.capacidad = data.getAsInteger("capacidad");
        }
        return result;
    }
}
