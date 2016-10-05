import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;

import java.util.GregorianCalendar;

/**
 * Creado por JFEsquivel on 05/10/2016.
 */

public class DBScaSqlite extends SQLiteOpenHelper {


    public DBScaSqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static String[] queries = new String[] {
            "CREATE TABLE user (iduser INTEGER, nombre TEXT, usr TEXT, pass TEXT, idproyecto INTEGER, base_datos TEXT, descripcion_database TEXT)",
            "CREATE TABLE camiones (idcamion INTEGER, placas TEXT, marca TEXT, modelo TEXT, ancho REAL, largo REAL, alto REAL, economico TEXT, capacidad INTEGER)",
            "CREATE TABLE origenes (idorigen INTEGER, descripcion TEXT, estado INTEGER)",
            "CREATE TABLE rutas (idruta INTEGER, clave TEXT, idorigen INTEGER, idtiro INTEGER, totalkm TEXT)",
            "CREATE TABLE materiales (idmaterial INTEGER, descripcion TEXT)",
            "CREATE TABLE tiros (idtiro INTEGER, descripcion TEXT)",
            "CREATE TABLE botones (idboton INTEGER, identificador TEXT)",
            "CREATE TABLE viajesnetos (ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "FechaCarga VARCHAR(8),"+
                    "HoraCarga VARCHAR(8),"+
                    "IdProyecto INTEGER,"+
                    "IdCamion INTEGER,"+
                    "IdOrigen INTEGER,"+
                    "FechaSalida VARCHAR(8),"+
                    "HoraSalida VARCHAR(8),"+
                    "IdTiro INTEGER,"+
                    "FechaLlegada VARCHAR(8),"+
                    "HoraLlegada VARCHAR(8),"+
                    "IdMaterial INTEGER,"+
                    "Observaciones TEXT,"+
                    "Creo TEXT,"+
                    "Estatus INTEGER, " +
                    "Ruta INTEGER, " +
                    "Code TEXT, uidTAG TEXT);",
            "CREATE TABLE coordenadas (IMEI TEXT, idevento INT, latitud TEXT, longitud TEXT, fecha_hora TEXT, code TEXT)",
            "CREATE TABLE camion_tag (ID INTEGER PRIMARY KEY AUTOINCREMENT, IMEI TEXT, id_camion INT, id_tags TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);"
    };

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String query: queries){
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS camiones");
        db.execSQL("DROP TABLE IF EXISTS origenes");
        db.execSQL("DROP TABLE IF EXISTS rutas");
        db.execSQL("DROP TABLE IF EXISTS materiales");
        db.execSQL("DROP TABLE IF EXISTS tiros");
        db.execSQL("DROP TABLE IF EXISTS botones");
        db.execSQL("DROP TABLE IF EXISTS viajesnetos");
        db.execSQL("DROP TABLE IF EXISTS coordenadas");
        db.execSQL("DROP TABLE IF EXISTS camion_tag");

        for (String query: queries){
            db.execSQL(query);
        }
    }
    public final static String getFechaHora() {

    }
    public boolean setCoordenada(String IMEI, int idevento, String latitud, String longitud, String code){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("INSERT INTO  coordenadas (IMEI, idevento, latitud, longitud, fecha_hora, code)" +
                "VALUES ("+
                "'"+IMEI+"',"+
                idevento+","+
                "'"+latitud+"',"+
                "'"+longitud+"',"+
                "'"+getFechaHora()+"',"+
                "'"+code+"')"
        );
        return true;
    }
    public static void alCerrarSesion (SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS camiones");
        db.execSQL("DROP TABLE IF EXISTS origenes");
        db.execSQL("DROP TABLE IF EXISTS rutas");
        db.execSQL("DROP TABLE IF EXISTS materiales");
        db.execSQL("DROP TABLE IF EXISTS tiros");
        db.execSQL("DROP TABLE IF EXISTS botones");
        db.execSQL("DROP TABLE IF EXISTS camion_tag");
        db.execSQL("DROP TABLE IF EXISTS viajesnetos");
        db.execSQL("DROP TABLE IF EXISTS coordenadas");
        //Se crea la nueva versión de la tabla
        for (String elemento: database){
            db.execSQL(elemento);
        }
        // db.execSQL(sqlCreate);
    }
}
}
