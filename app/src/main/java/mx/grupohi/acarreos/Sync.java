package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.net.URL;

/**
 * Creado por JFEsquivel on 19/10/2016.
 */

public class Sync extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private Usuario usuario;

    private JSONObject JSON;

    Sync(Context context) {
        this.context = context;

        usuario = new Usuario(context);
        usuario = usuario.getUsuario();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        ContentValues values = new ContentValues();

        values.put("metodo", "captura");
        values.put("usr", usuario.usr);
        values.put("pass", usuario.pass);
        values.put("bd", usuario.baseDatos);
        if(!Viaje.getJSON().equals(null)) {
            values.put("carddata", String.valueOf(Viaje.getJSON()));
        }
        if(!Coordenada.getJSON().equals(null)) {
            values.put("coordenadas", String.valueOf(Coordenada.getJSON()));
        }

        try {
            URL url = new URL("http://sca.grupohi.mx/android20160923.php");
            JSON = HttpConnection.POST(url, values);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }
}