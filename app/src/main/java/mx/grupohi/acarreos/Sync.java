package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

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

    private JSONObject JSON;

    Sync(Context context, ProgressDialog progressDialog) {

        this.context = context;
        this.progressDialog = progressDialog;
        usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        gps = new GPSTracker(context);
        System.out.println("3");

    }

    @Override
    protected Boolean doInBackground(Void... params) {

        if(!gps.canGetLocation()) {
            gps.showSettingsAlert();
            return false;
        } else {
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
                JSONObject Obj = Viaje.getJSON(context);
                values.put("carddata", String.valueOf(Obj));
            }
            if (Coordenada.getJSON(context).length() != 0) {
                values.put("coordenadas", String.valueOf(Coordenada.getJSON(context)));
            }
           /* if (ImagenesViaje.getCount(context) != 0){
                values.put("Imagenes", String.valueOf(ImagenesViaje.getJSONImagenes(context)));
            }*/
            /*try {

                FileWriter file = new FileWriter("/storage/emulated/0/Picture/Pruebas/prueba"+Util.getFechaHora()+".txt");
                file.write(values.toString());
                file.flush();
                file.close();

            } catch (IOException e) {
                e.printStackTrace();
            }*/
            try {

                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                JSON = HttpConnection.POST(url, values);
                Log.i("json ",String.valueOf(values));
                ContentValues aux = new ContentValues();
                int i = 0;
                int id;
                int past=0;
                while (ImagenesViaje.getCount(context) != 0) {
                    i++;
                    System.out.println("Existen imagenes para sincronizar: " + ImagenesViaje.getCount(context));
                    aux.put("metodo", "cargaImagenesViajes");
                    aux.put("usr", usuario.usr);
                    aux.put("pass", usuario.pass);
                    aux.put("bd", usuario.baseDatos);




                    //System.out.println("******************************count " + ImagenesViaje.getCount(context));
                    aux.put("Imagenes", String.valueOf(ImagenesViaje.getJSONImagenes(context)));
                    /*try {

                        FileWriter file = new FileWriter("/storage/emulated/0/Picture/Pruebas/prueba"+Util.getFechaHora()+".txt");
                        file.write(aux.toString());
                        file.flush();
                        file.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    try {
                        JSON = HttpConnection.POST(url, aux);
                        Log.i("json ", String.valueOf(aux));
                        for( id=past+1; id<=200*i;id++) {
                            ImagenesViaje.syncLimit(context, id);
                        }
                        past = id - 1;
                        aux.clear();
                    }catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Error de sincronizacion");
                    }
                }

            }catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        progressDialog.dismiss();
        if(aBoolean) {
            try {
                if (JSON.has("error")) {
                    Toast.makeText(context, (String) JSON.get("error"), Toast.LENGTH_SHORT).show();
                } else if(JSON.has("msj")) {
                    Viaje.sync(context);
                    Toast.makeText(context, (String) JSON.get("msj"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}