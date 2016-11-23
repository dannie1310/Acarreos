package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

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
                System.out.println("Item: "+item);
                return item;
            }
        }
        return null;
    }

    public String getIdDrawable() {
        System.out.println("idDrawa: "+idDrawable);
        return idDrawable;
    }

    public String getNombre() {
        System.out.println("Nombre: "+nombre);
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

    public void getImagen(Integer idViaje) {
        db = db_sca.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT id, imagen FROM imagenes_viaje WHERE idviaje_neto = '" + idViaje + "'",null);

        ImagenesViaje [] ITEMS= new ImagenesViaje[c.getCount()];
        try {
          if (c != null && c.moveToFirst()) {

                for(int i=0; i<c.getCount();i++) {
                    ITEMS[i] = new ImagenesViaje(c.getInt(0),"prueba21", c.getString(1));
                }

            }

        } finally {
            c.close();
            db.close();
        }
        this.ITEMS = ITEMS;
        System.out.println("Imagen: "+ITEMS);
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

                System.out.println(lista);
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
}
