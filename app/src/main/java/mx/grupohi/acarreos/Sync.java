package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

/**
 * Creado por JFEsquivel on 19/10/2016.
 */

class Sync extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private ProgressDialog progressDialog;
    private Usuario usuario;

    // GPSTracker class
    private GPSTracker gps;
    private double latitude;
    private double longitude;
    private String IMEI;
    Integer idviaje;
    Integer imagenesTotales = 0;
    Integer imagenesRegistradas = 0;

    private JSONObject JSONVIAJES;
    private JSONObject JSON;

    Sync(Context context, ProgressDialog progressDialog) {

        this.context = context;
        this.progressDialog = progressDialog;
        usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        gps = new GPSTracker(context);

    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            Util.copyDataBaseSyns(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        latitude = gps.getLatitude();
        longitude = gps.getLongitude();
        TelephonyManager phneMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = phneMgr.getDeviceId();

        ContentValues values = new ContentValues();

        values.put("IMEI", IMEI);
        values.put("idevento", 4);
        values.put("latitud", latitude);
        values.put("longitud", longitude);
        values.put("fecha_hora", Util.timeStamp());
        values.put("code", "");

        Coordenada.create(values, context);

        values.clear();

        values.put("metodo", "captura");
        values.put("usr", usuario.usr);
        values.put("pass", usuario.pass);
        values.put("bd", usuario.baseDatos);
        values.put("idusuario", usuario.getId());
        values.put("Version", String.valueOf(BuildConfig.VERSION_NAME));

        if (Viaje.getCount(context) != 0) {
            values.put("carddata", String.valueOf(Viaje.getJSON(context)));
        }
        if (Coordenada.getCount(context) != 0) {
            values.put("coordenadas", String.valueOf(Coordenada.getJSON(context)));
        }
        if (InicioViaje.getCount(context) != 0) {
            values.put("inicioCamion", String.valueOf(InicioViaje.getJSON(context)));
        }

        try {

            URL url = new URL("http://sca.grupohi.mx/android20160923.php");
            JSONVIAJES = HttpConnection.POST(url, values);
            Log.i("jsonviajes:  ", String.valueOf(values));
            ContentValues aux = new ContentValues();
            imagenesTotales = ImagenesViaje.getCount(context);
            int i = 0;
            //System.out.println("imagenes totales: "+imagenesTotales);
            while (ImagenesViaje.getCount(context) != 0) {
                i++;
                JSON = null;
                //System.out.println("Existen imagenes para sincronizar: " + ImagenesViaje.getCount(context));
                aux.put("metodo", "cargaImagenesViajes");
                aux.put("usr", usuario.usr);
                aux.put("pass", usuario.pass);
                aux.put("bd", usuario.baseDatos);
                aux.put("Imagenes", String.valueOf(ImagenesViaje.getJSONImagenes(context)));

                try {
                    JSON = HttpConnection.POST(url, aux);
                    Log.i("json ", String.valueOf(aux));
                    try {
                        if (JSON.has("imagenes_registradas")) {
                            final JSONArray imagenes = new JSONArray(JSON.getString("imagenes_registradas"));
                            for (int r = 0; r < imagenes.length(); r++) {
                                ImagenesViaje.syncLimit(context, imagenes.getInt(r));
                                imagenesRegistradas++;
                            }
                        }

                        //System.out.println("imagenesRegiustradas: "+imagenesRegistradas);

                        if (JSON.has("imagenes_no_registradas_sv")) {
                            final JSONArray errores = new JSONArray(JSON.getString("imagenes_no_registradas_sv"));
                            //System.out.println("Errores1: " + errores);
                            for (int r = 0; r < errores.length(); r++) {
                                ImagenesViaje.cambioEstatus(context, errores.getInt(r));
                            }
                        }
                        if (JSON.has("imagenes_no_registradas")) {
                            final JSONArray errores = new JSONArray(JSON.getString("imagenes_no_registradas"));
                            // System.out.println("Errores2: " + errores);
                            for (int r = 0; r < errores.length(); r++) {
                                ImagenesViaje.cambioEstatus(context, errores.getInt(r));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        Integer errores = ImagenesViaje.getCountErrores(context);
        if(aBoolean) {
            try {
                progressDialog.dismiss();
                if (JSONVIAJES.has("error")) {
                    System.out.println("error");
                    Toast.makeText(context, (String) JSONVIAJES.get("error"), Toast.LENGTH_SHORT).show();
                } else if(JSONVIAJES.has("msj")) {
                    Viaje.sync(context);
                    Toast.makeText(context, (String) JSONVIAJES.get("msj") + "Imagenes Registradas: "+imagenesRegistradas+" de "+imagenesTotales, Toast.LENGTH_LONG).show();
                    System.out.println("mensaje");
                }
            } catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}