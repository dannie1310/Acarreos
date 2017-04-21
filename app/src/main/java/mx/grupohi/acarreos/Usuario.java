package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Base64DataException;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Creado por JFEsquivel on 05/10/2016.
 */

class Usuario {

    private Integer idUsuario;
    private Integer idProyecto;
    String usr;
    String pass;
    String nombre;
    String baseDatos;
    String descripcionBaseDatos;
    String empresa;
    Integer logo;
    String imagen;
    Integer tipo_permiso;

    private Context context;

    private SQLiteDatabase db;
    private DBScaSqlite db_sca;

    Usuario(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }

    boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("user", null, data) > -1;
        if (result) {
            this.idUsuario = Integer.valueOf(data.getAsString("idusuario"));
            this.idProyecto = Integer.valueOf(data.getAsString("idproyecto"));
            this.nombre = data.getAsString("nombre");
            this.baseDatos = data.getAsString("base_datos");
            this.descripcionBaseDatos = data.getAsString("descripcion_database");
            this.empresa = data.getAsString("empresa");
            this.logo = data.getAsInteger("logo");
            this.imagen = data.getAsString("imagen");
            this.tipo_permiso = data.getAsInteger("tipo_permiso");
        }
        db.close();
        return result;
    }

    void destroy() {
        db = db_sca.getWritableDatabase();
        db.execSQL("DELETE FROM user");
        db.close();
    }

    boolean isAuth() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        try {
            return c != null && c.moveToFirst();
        } finally {
            c.close();
            db.close();
        }
    }

    public Integer getId() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        try {
            if(c != null && c.moveToFirst()) {
                this.idUsuario = c.getInt(0);
            }
            return this.idUsuario;
        } finally {
            c.close();
            db.close();
        }
    }

    Usuario getUsuario() {
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM user LIMIT 1", null);
        try {
            if(c != null && c.moveToFirst()) {
                this.idUsuario = c.getInt(c.getColumnIndex("idusuario"));
                this.idProyecto = c.getInt(c.getColumnIndex("idproyecto"));
                this.nombre = c.getString(c.getColumnIndex("nombre"));
                this.baseDatos = c.getString(c.getColumnIndex("base_datos"));
                this.descripcionBaseDatos = c.getString(c.getColumnIndex("descripcion_database"));
                this.usr = c.getString(c.getColumnIndex("user"));
                this.pass = c.getString(c.getColumnIndex("pass"));
                this.tipo_permiso = c.getInt(c.getColumnIndex("tipo_permiso"));

                return this;
            } else {
                return null;
            }
        }finally {
            c.close();
            db.close();
        }
    }

    String getNombre(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT nombre FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                this.nombre =  c.getString(0);
            }
            return this.nombre;
        } finally {
            c.close();
            db.close();
        }
    }

    public String getDescripcion(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT descripcion_database FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getString(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public String getEmpresa(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT empresa FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getString(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public Integer getLogo(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT logo FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getInt(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }
    public Integer getProyecto(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT idproyecto FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getInt(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public String getImagen(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT imagen FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                return c.getString(0);
            }
            else{
                return null;
            }
        } finally {
            c.close();
            db.close();
        }
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        Bitmap imagen=null;
        try {
            imagen = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            return imagen;
        }
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static String encodeToBase64Imagen(Bitmap image, int quality)
    {

        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        String respuesta = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
        respuesta = respuesta.replace("\n","");
        return respuesta;
    }


    static boolean updateLogo(String logo, Context context) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

            try{

                data.put("imagen", logo);

                db.update("user", data, "", null);
                resp = true;
            } finally {
                db.close();
            }
        return resp;
    }

    static boolean updatePass(String pass, Context context) {
        boolean resp=false;
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        try{

            data.put("pass", pass);

            db.update("user", data, "", null);
            resp = true;
        } finally {
            db.close();
        }
        return resp;
    }

    public Integer getTipo_permiso(){
        db = db_sca.getWritableDatabase();
        Integer tipo;
        Cursor c = db.rawQuery("SELECT tipo_permiso FROM user LIMIT 1", null);
        try {
            if(c!=null && c.moveToFirst()){
                tipo = c.getInt(0);
                if(tipo == 1 || tipo == 3){
                    return 0; //Origen
                }else{
                    return 1;
                }
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

