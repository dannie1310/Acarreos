package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Usuario on 22/11/2016.
 */

public class ImagenesViaje {

    Context context;
    private static SQLiteDatabase db;
    private DBScaSqlite db_sca;
    Integer id;
    String descripcion;
    private ContentValues data;
    private String idDrawable;
    private String nombre;
    private String imagen;
    public static ImagenesViaje[] ITEMS;


    ImagenesViaje(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }
    ImagenesViaje(int id, String nombre, String idDrawable){
        this.id=id;
        this.nombre = nombre;
        this.idDrawable = idDrawable;
    }
    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("imagenes_viaje", null, data) > -1;
       db.close();
        return result;
    }


    public int getId() {
        return id.hashCode();
    }


    public void createItem(Integer idviaje){
        getImagen(idviaje);

    }
    public static ImagenesViaje getItem(int id) {
        for (ImagenesViaje item : ITEMS) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public String getIdDrawable() {
        return idDrawable;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getCount(Integer idViaje) {
            DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
            SQLiteDatabase db = db_sca.getWritableDatabase();
            Cursor c = db.rawQuery("SELECT * FROM imagenes_viaje WHERE idviaje_neto = '" + idViaje + "'",null);
            try {
                return c.getCount();
            } finally {
                c.close();
                db.close();
            }
    }

    public static Integer getCount(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM imagenes_viaje WHERE estatus = 1",null);
        try {
            return c.getCount();
        } finally {
            c.close();
            db.close();
        }
    }

    public void getImagen(Integer idViaje) {
        db = db_sca.getWritableDatabase();
        int i=0;
        Cursor c = db.rawQuery("SELECT imagenes_viaje.id as id, imagenes_viaje.url as url, tipos_imagenes.descripcion as tipo_imagen FROM imagenes_viaje left join tipos_imagenes on(tipos_imagenes.id = imagenes_viaje.idtipo_imagen)  WHERE idviaje_neto = '" + idViaje + "'",null);

        ImagenesViaje [] ITEMS= new ImagenesViaje[c.getCount()];

        try {
          if (c != null && c.moveToFirst()) {
              do {
                  ITEMS[i] = new ImagenesViaje(c.getInt(0), c.getString(2), c.getString(1));
                  i++;
              } while (c.moveToNext());
          }

        } finally {
            c.close();
            db.close();
        }
        this.ITEMS = ITEMS;
    }

    public String getImagen(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT imagen FROM imagenes_viaje LIMIT 1", null);
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
    public static List<ImagenesViaje> getImagen(Context context){
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT imagen FROM imagenes_viaje",null);
        ArrayList lista = new ArrayList<ImagenesViaje>();
        try {
            if (c != null){
                while (c.moveToNext()){

                    lista.add(c.getString(0));
                }

                return lista;
            }
            else {
                return new ArrayList<>();
            }
        } finally {
            c.close();
            db.close();
        }
    }


    static JSONObject getJSONImagenes(Context context) {
        JSONObject JSON = new JSONObject();
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM imagenes_viaje WHERE estatus = 1 ORDER BY id ASC LIMIT 80" , null);
        try {
            if(c != null && c.moveToFirst()) {
                Integer i = 0;
                do {
                   // System.out.println("**---"+i+" ---"+c.getInt(0)+"-----**"+c.getString(2)+"**----"+c.getString(5)+"----**");
                    JSONObject json = new JSONObject();
                    json.put("idImagen", c.getInt(0));
                    json.put("CodeImagen", c.getString(5));
                    json.put("idtipo_imagen", c.getString(2));
                    json.put("imagen", c.getString(4));
                    JSON.put(i + "", json);
                    i++;
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
        return JSON;
    }

    public static void syncLimit(Context context,int id) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Integer idviaje = getIdViaje(context, id);
        Cursor c = db.rawQuery("SELECT * FROM imagenes_viaje WHERE idviaje_neto = '" + idviaje +"'", null);

        try {

            Integer imagenes = c.getCount();
           // System.out.println("ELIMINAR*****" + id + "viaje num : "+idviaje+" faltan: "+imagenes);
            if(imagenes == 1){
                db.execSQL("DELETE FROM viajesnetos WHERE id= " + idviaje );
            }

            db.execSQL("DELETE FROM imagenes_viaje WHERE id= " + id);

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }

    public static Integer getIdViaje(Context context, Integer idViaje) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Integer resp = 0;
        Cursor c = db.rawQuery("SELECT idviaje_neto FROM imagenes_viaje WHERE id = '" + idViaje + "'", null);
        try {
            if (c != null && c.moveToFirst()) {
                resp=c.getInt(0);
            }

        } catch (Exception e) {
            //System.out.println("ERRORSYCNLIMIT");
            e.printStackTrace();

        } finally {
            c.close();
            db.close();
            return resp;
        }
    }

    public static void cambioEstatus(Context context,int id) {//editar ********************
        ContentValues data = new ContentValues();

        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        try {
            data.put("estatus", "0");
            db.update("imagenes_viaje", data,"id = '"+id+"'", null);
           // System.out.println("imagenes_errores: "+id +"datos: "+ data.toString());
        }catch (Exception e) {
            e.printStackTrace();
        } finally {

            db.close();
        }
    }

    public static Integer getCountErrores(Context context) {
        DBScaSqlite db_sca = new DBScaSqlite(context, "sca", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM imagenes_viaje WHERE estatus = 0 ",null);
        try {
           // System.out.println("error = "+ c.getCount());
            return c.getCount();
        } finally {
            c.close();
            db.close();
        }
    }
}
