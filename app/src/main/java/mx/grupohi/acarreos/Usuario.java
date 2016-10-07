package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Creado por JFEsquivel on 05/10/2016.
 */

class Usuario {

    private Integer idUsuario;
    private Integer idProyecto;
    private String nombre;
    private String baseDatos;
    private String descripcionBaseDatos;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Usuario(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
    }

    boolean create(ContentValues data) {
        Boolean result = db.insert("user", null, data) > -1;
        if (result) {
            this.idUsuario = Integer.valueOf(data.getAsString("idusuario"));
            this.idProyecto = Integer.valueOf(data.getAsString("idproyecto"));
            this.nombre = data.getAsString("nombre");
            this.baseDatos = data.getAsString("base_datos");
            this.descripcionBaseDatos = data.getAsString("descripcion_database");
        }
        return result;
    }

    public void destroy() { db.execSQL("DELETE FROM user"); }

    public boolean isAuth() {
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        Boolean result = c != null && c.moveToFirst();
        assert c != null;
        c.close();
        return result;
    }
}
