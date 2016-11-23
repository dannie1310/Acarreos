package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

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
    private int idDrawable;
    private String nombre;


    ImagenesViaje(Context context) {
        this.context = context;
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }
    ImagenesViaje(String nombre, int idDrawable){
        this.nombre = nombre;
        this.idDrawable = idDrawable;
    }
    Boolean create(ContentValues data) {
        db = db_sca.getWritableDatabase();
        Boolean result = db.insert("imagenes_viaje", null, data) > -1;
       db.close();
        return result;
    }

    public static ImagenesViaje[] ITEMS = {
            new ImagenesViaje("Prueba", R.drawable.logo_ghi),
            new ImagenesViaje("Prueba2", R.drawable.img_success),
    };

    public int getId() {
        return nombre.hashCode();
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

    public int getIdDrawable() {
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

}
