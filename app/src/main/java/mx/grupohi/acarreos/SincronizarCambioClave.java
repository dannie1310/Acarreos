package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URL;

/**
 * Created by Usuario on 27/03/2017.
 */

public class SincronizarCambioClave extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private ProgressDialog progressDialog;
    private Usuario usuario;


    private String IMEI;
    private String NuevaClave;

    private JSONObject JSONVIAJES;
    private JSONObject JSON;

    SincronizarCambioClave(Context context, ProgressDialog progressDialog, String clavenueva) {

        this.context = context;
        this.progressDialog = progressDialog;
        this.NuevaClave = clavenueva;
        usuario = new Usuario(context);
        usuario = usuario.getUsuario();

    }

    @Override
    protected Boolean doInBackground(Void... params) {

            TelephonyManager phneMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            IMEI = phneMgr.getDeviceId();

            ContentValues values = new ContentValues();

            values.clear();

            values.put("metodo", "ActualizarAcceso");
            values.put("usr", usuario.usr);
            values.put("pass", usuario.pass);
            values.put("idusuario", usuario.getId());
            values.put("bd", usuario.baseDatos);
            values.put("IMEI", IMEI);
            values.put("Version", String.valueOf(BuildConfig.VERSION_NAME));
            values.put("NuevaClave", NuevaClave);

            try {

                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                JSONVIAJES = HttpConnection.POST(url, values);
                Log.i("josn", String.valueOf(JSONVIAJES));
                Log.i("jsonviajes:  ",String.valueOf(values));


            }catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return false;
            }
            return true;

    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        progressDialog.dismiss();
        if(aBoolean) {
            try {
                if (JSONVIAJES.has("error")) {
                    Toast.makeText(context, (String) JSONVIAJES.get("error"), Toast.LENGTH_SHORT).show();
                } else if(JSONVIAJES.has("msj")) {
                    Usuario.updatePass(NuevaClave,context);
                    Toast.makeText(context, (String) JSONVIAJES.get("msj"), Toast.LENGTH_LONG).show();

                }
            } catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

}
