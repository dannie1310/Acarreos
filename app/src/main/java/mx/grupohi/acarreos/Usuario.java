package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Creado por JFEsquivel on 05/10/2016.
 */

class Usuario {

    private Integer idUsuario;
    private Integer idProyecto;
    String usr;
    String pass;
    private String nombre;
    String baseDatos;
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

    public Integer getId() {
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        if(c != null && c.moveToFirst()) {
            return c.getInt(0);
        } else {
            return null;
        }
    }

    Usuario getUsuario() {
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        if(c != null && c.moveToFirst()) {
            this.idUsuario = c.getInt(c.getColumnIndex("idusuario"));
            this.idProyecto = c.getInt(c.getColumnIndex("idproyecto"));
            this.nombre = c.getString(c.getColumnIndex("nombre"));
            this.baseDatos = c.getString(c.getColumnIndex("base_datos"));
            this.descripcionBaseDatos = c.getString(c.getColumnIndex("descripcion_database"));
            this.usr = c.getString(c.getColumnIndex("user"));
            this.pass = c.getString(c.getColumnIndex("pass"));

            Log.i("ID", String.valueOf(this.idUsuario));
            Log.i("IDP", String.valueOf(this.idProyecto));
            Log.i("NOMBRE", this.nombre);
            Log.i("BASDATOS", this.baseDatos);
            Log.i("DESC", this.descripcionBaseDatos);
            Log.i("USER", this.usr);
            Log.i("PASS", this.pass);
            c.close();
            return this;
        } else {
            return null;
        }
    }
}

