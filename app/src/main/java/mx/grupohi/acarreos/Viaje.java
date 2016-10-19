package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Creado por JFEsquivel on 10/10/2016.
 */

public class Viaje {

    public Integer idViaje;
    public Integer idMaterial;
    public Integer idTiro;
    public Integer idOrigen;
    public Integer idCamion;
    public Camion camion;
    public Material material;
    public Origen origen;
    public Tiro tiro;

    private static HashMap <String, Viaje> viajes;
    private static SQLiteDatabase db;
    private DBScaSqlite db_sca;

    private static Context context;

    Viaje(Context context) {
        this.context = context;
        this.camion =new Camion(context);
        this.origen = new Origen(context);
        this.tiro=new Tiro(context);
        this.material=new Material(context);
        db_sca = new DBScaSqlite(context, "sca", null, 1);
        db = db_sca.getWritableDatabase();
        viajes= new HashMap<>();
    }

    Boolean create(ContentValues data) {
        Boolean result = db.insert("viajesnetos", null, data) > -1;
        if (result) {
            Cursor c = db.rawQuery("SELECT ID FROM viajesnetos WHERE Code = '" + data.getAsString("Code") + "'", null);
            if(c != null && c.moveToFirst()) {
                this.idViaje = c.getInt(0);
            }
        }
        return result;
    }
    public Viaje find (Integer idViaje) {
        Cursor c = db.rawQuery("SELECT * FROM viajesnetos WHERE ID = '" + idViaje + "'", null);
        if (c != null && c.moveToFirst()) {
            this.idCamion = c.getInt(4);
            this.idViaje = c.getInt(0);
            this.idMaterial=c.getInt(11);
            this.idOrigen=c.getInt(5);
            this.idTiro =c.getInt(8);
            this.camion= this.camion.find(this.idCamion);
            this.material=this.material.find(this.idMaterial);
            this.origen = this.origen.find(idOrigen);
            this.tiro = this.tiro.find(idTiro);

            return this;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Viaje{" +
                "ID='" + idViaje + '\'' +
                ", Camion='" + camion.economico  + '\'' +
                ", Material='" + material.descripcion + '\'' +
                ", Origen='" + origen.descripcion + '\'' +
                ", Destino='"+ tiro.descripcion + '\'' +
                '}';
    }

    public static List<Viaje> getViajes(Context con){

        DBScaSqlite db_sca = new DBScaSqlite(con, "sca", null, 1);
        db = db_sca.getWritableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM viajesnetos",null);
        Viaje viaje = new Viaje(con);
        if (c!=null){
            while (c.moveToNext()){
                viaje=viaje.find(c.getInt(0));
                viajes.put(viaje.idViaje.toString(), viaje);
            }
            return new ArrayList<>(viajes.values());
        }
        else {
            return new ArrayList<>();
        }
    }

}
