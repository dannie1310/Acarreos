package mx.grupohi.acarreos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;

import java.util.GregorianCalendar;

/**
 * Creado por JFEsquivel on 05/10/2016.
 */
 class DBScaSqlite extends SQLiteOpenHelper {


    DBScaSqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static String[] queries = new String[] {
            "CREATE TABLE user (idusuario INTEGER, idproyecto INTEGER, nombre, TEXT, base_datos TEXT, descripcion_database TEXT, user TEXT, pass TEXT, empresa TEXT, logo INTEGER, imagen BLOB, tipo_permiso INTEGER, idorigen INTEGER, idtiro INTEGER)",
            "CREATE TABLE camiones (idcamion INTEGER, placas TEXT, placasCaja TEXT, marca TEXT, modelo TEXT, ancho REAL, largo REAL, alto REAL, economico TEXT, capacidad INTEGER, numero_viajes INTEGER, estatus INTEGER)",
            "CREATE TABLE tiros (idtiro INTEGER, descripcion TEXT)",
            "CREATE TABLE origenes (idorigen INTEGER, descripcion TEXT, estado INTEGER)",
            "CREATE TABLE rutas (idruta INTEGER, clave TEXT, idorigen INTEGER, idtiro INTEGER, totalkm TEXT)",
            "CREATE TABLE materiales (idmaterial INTEGER, descripcion TEXT)",
            "CREATE TABLE tags (uid TEXT, idcamion INTEGER, idproyecto INTEGER,economico TEXT, estatus INTEGER)",
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
                    "Code TEXT, " +
                    "uidTAG TEXT, " +
                    "IMEI TEXT," +
                    "CodeImagen TEXT," +
                    "deductiva TEXT," +
                    "FolioRandom TEXT," +
                    "idMotivo INTEGER," +
                    "primerToque TEXT," +
                    "cubicacion TEXT," +
                    "tipoEsquema INTEGER," +
                    "numImpresion INTEGER," +
                    "idperfil INTEGER,"+
                    "folio_mina TEXT," +
                    "folio_seguimiento TEXT," +
                    "deductiva_origen TEXT," +
                    "idmotivo_origen INT" +
                    "deductiva_entrada TEXT" +
                    "idmotivo_entrada INT," +
                    "tipoViaje INT);",
            "CREATE TABLE coordenadas (IMEI TEXT, idevento INT, latitud TEXT, longitud TEXT, fecha_hora TEXT, code TEXT)",
            "CREATE TABLE camion_tag (ID INTEGER PRIMARY KEY AUTOINCREMENT, IMEI TEXT, id_camion INT, id_tags TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);",
            "CREATE TABLE tipos_imagenes (id INTEGER, descripcion TEXT);",
            "CREATE TABLE imagenes_viaje (ID INTEGER PRIMARY KEY AUTOINCREMENT, idviaje_neto INTEGER, idtipo_imagen TEXT, url TEXT, imagen BLOB, code TEXT, estatus INTEGER);",
            "CREATE TABLE motivos (id INTEGER, descripcion TEXT);",
            "CREATE TABLE configuraciones (ID INTEGER PRIMARY KEY AUTOINCREMENT, validacion_placas INTEGER);",
            "CREATE TABLE checadores (idChecador INTEGER PRIMARY KEY, nombre TEXT);",
            "CREATE TABLE inicio_viajes (ID INTEGER PRIMARY KEY AUTOINCREMENT, idcamion INTEGER, idmaterial INTEGER, idorigen INTEGER, fecha_origen VARCHAR(8), idusuario INTEGER, uidTAG TEXT, IMEI TEXT, tipoEsquema INTEGER, estatus INTEGER, idperfil INTEGER, folio_mina TEXT, folio_seguimiento TEXT, volumen INTEGER, tipo_suministro INTEGER, Code TEXT, numImpresion INTEGER, deductiva TEXT, idMotivo INTEGER, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);",
            "CREATE TABLE entrada_tiros (ID INTEGER PRIMARY KEY AUTOINCREMENT, idcamion INTEGER, idtiro INTEGER, fecha_entrada VARCHAR(8), idusuario INTEGER, uidTAG TEXT, IMEI TEXT,  estatus INTEGER, created_at DATETIME DEFAULT CURRENT_TIMESTAMP );",
            "CREATE TABLE celular_impresora (ID INTEGER PRIMARY KEY AUTOINCREMENT, IMEI TEXT, MAC TEXT);",
    };

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String query: queries){
            db.execSQL(query);
        }    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");

        db.execSQL("DROP TABLE IF EXISTS camiones");
        db.execSQL("DROP TABLE IF EXISTS origenes");
        db.execSQL("DROP TABLE IF EXISTS rutas");
        db.execSQL("DROP TABLE IF EXISTS materiales");
        db.execSQL("DROP TABLE IF EXISTS tiros");
        db.execSQL("DROP TABLE IF EXISTS tags");
        db.execSQL("DROP TABLE IF EXISTS viajesnetos");
        db.execSQL("DROP TABLE IF EXISTS coordenadas");
        db.execSQL("DROP TABLE IF EXISTS camion_tag");
        db.execSQL("DROP TABLE IF EXISTS tipos_imagenes");
        db.execSQL("DROP TABLE IF EXISTS imagenes_viaje");
        db.execSQL("DROP TABLE IF EXISTS motivos");
        db.execSQL("DROP TABLE IF EXISTS configuraciones");
        db.execSQL("DROP TABLE IF EXISTS checadores");
        db.execSQL("DROP TABLE IF EXISTS inicio_viajes");
        db.execSQL("DROP TABLE IF EXISTS entrada_tiros");
        db.execSQL("DROP TABLE IF EXISTS celular_impresora");

        for (String query: queries){
            db.execSQL(query);
        }

        db.close();
    }

    void deleteCatalogos() {
        SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL("DELETE FROM user");
        db.execSQL("DELETE FROM camiones");
        db.execSQL("DELETE FROM tiros");
        db.execSQL("DELETE FROM origenes");
        db.execSQL("DELETE FROM rutas");
        db.execSQL("DELETE FROM materiales");
        db.execSQL("DELETE FROM tags");
        db.execSQL("DELETE FROM tipos_imagenes");
        db.execSQL("DELETE FROM imagenes_viaje");
        db.execSQL("DELETE FROM motivos");
        db.execSQL("DELETE FROM configuraciones");
        db.execSQL("DELETE FROM checadores");
        db.execSQL("DELETE FROM inicio_viajes");
        db.execSQL("DELETE FROM entrada_tiros");
        db.execSQL("DELETE FROM celular_impresora");

        db.close();
    }

    void descargaCatalogos() {
        SQLiteDatabase db = this.getReadableDatabase();
        System.out.println("desc: ");
        db.execSQL("DELETE FROM tiros");
        db.execSQL("DELETE FROM origenes");
        db.execSQL("DELETE FROM rutas");
        db.execSQL("DELETE FROM materiales");
        db.execSQL("DELETE FROM tags");
        db.execSQL("DELETE FROM camiones");
        db.execSQL("DELETE FROM motivos");
        db.execSQL("DELETE FROM configuraciones");
        db.execSQL("DELETE FROM checadores");
        db.execSQL("DELETE FROM celular_impresora");

        db.close();
    }
}
