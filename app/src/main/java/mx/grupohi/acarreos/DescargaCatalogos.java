package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * Created by Usuario on 16/12/2016.
 */

public class DescargaCatalogos extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private ProgressDialog progressDialog;
    private Usuario usuario;
    private DBScaSqlite db_sca;
    private JSONObject JSON;


    DescargaCatalogos(Context context, ProgressDialog progressDialog) {
        this.context = context;
        this.progressDialog = progressDialog;
        usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        db_sca = new DBScaSqlite(context, "sca", null, 1);
    }


    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.

        ContentValues data = new ContentValues();
        data.put("usr", usuario.usr);
        data.put("pass", usuario.pass);

        try {
            URL url = new URL("http://sca.grupohi.mx/android20160923.php");
            JSON = HttpConnection.POST(url, data);

            if (JSON.has("error")) {
                Toast.makeText(context, "Error al descargar ", Toast.LENGTH_LONG).show();
                return false;
            } else {
                System.out.println("DESCARGANDO");
                db_sca.descargaCatalogos();
                //Camiones
                Camion camion = new Camion(context);
                try {
                    final JSONArray camiones = new JSONArray(JSON.getString("Camiones"));
                    for (int i = 0; i < camiones.length(); i++) {
                        final int finalI = i + 1;

                       // Toast.makeText(context, "Actualizando catálogo de camiones...", Toast.LENGTH_SHORT).show();

                        JSONObject info = camiones.getJSONObject(i);

                        data.clear();
                        if(!Camion.findId(Integer.valueOf(info.getString("idcamion")),context)){

                            data.put("idcamion", info.getString("idcamion"));
                            data.put("placas", info.getString("placas"));
                            data.put("marca", info.getString("marca"));
                            data.put("modelo", info.getString("modelo"));
                            data.put("ancho", info.getString("ancho"));
                            data.put("largo", info.getString("largo"));
                            data.put("alto", info.getString("alto"));
                            data.put("economico", info.getString("economico"));
                            data.put("capacidad", info.getString("capacidad"));
                            System.out.println("camion: " + data);
                            if (!camion.create(data)) {
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Tiros

                Tiro tiro = new Tiro(context);
                try {
                    final JSONArray tiros = new JSONArray(JSON.getString("Tiros"));
                    for (int i = 0; i < tiros.length(); i++) {
                        final int finalI = i + 1;
                       // Toast.makeText(context, "Actualizando catálogo de tiros...", Toast.LENGTH_SHORT).show();

                        JSONObject info = tiros.getJSONObject(i);
                        System.out.println("tiros: "+tiros.getJSONObject(i));
                        data.clear();
                        data.put("idtiro", info.getString("idtiro"));
                        data.put("descripcion", info.getString("descripcion"));
                        System.out.println("tiro: " + data);
                        if (!tiro.create(data)) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Origenes

                Origen origen = new Origen(context);
                try {
                    final JSONArray origenes = new JSONArray(JSON.getString("Origenes"));
                    for (int i = 0; i < origenes.length(); i++) {
                        final int finalI = i + 1;
                       // Toast.makeText(context, "Actualizando catálogo de Origenes... ", Toast.LENGTH_SHORT).show();
                        JSONObject info = origenes.getJSONObject(i);
                        System.out.println("origenes: "+origenes.getJSONObject(i));
                        data.clear();
                        data.put("idorigen", info.getString("idorigen"));
                        data.put("descripcion", info.getString("descripcion"));
                        data.put("estado", info.getString("estado"));
                        System.out.println("origenes: " + data);

                        if (!origen.create(data)) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Rutas

                Ruta ruta = new Ruta(context);
                try {
                    final JSONArray rutas = new JSONArray(JSON.getString("Rutas"));
                    for (int i = 0; i < rutas.length(); i++) {
                        final int finalI = i + 1;
                        //Toast.makeText(context, "Actualizando catálogo de Rutas... ", Toast.LENGTH_SHORT).show();
                        System.out.println("rutas: "+rutas.getJSONObject(i));
                        JSONObject info = rutas.getJSONObject(i);

                        data.clear();
                        data.put("idruta", info.getString("idruta"));
                        data.put("clave", info.getString("clave"));
                        data.put("idorigen", info.getString("idorigen"));
                        data.put("idtiro", info.getString("idtiro"));
                        data.put("totalkm", info.getString("totalkm"));
                        System.out.println("rutas: " + data);
                        if (!ruta.create(data)) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Materiales

                Material material = new Material(context);
                try {
                    final JSONArray materiales = new JSONArray(JSON.getString("Materiales"));
                    for (int i = 0; i < materiales.length(); i++) {
                        final int finalI = i + 1;
                        //Toast.makeText(context, "Actualizando catálogo de Materiales...", Toast.LENGTH_SHORT).show();
                        System.out.println("materiAL: "+materiales.getJSONObject(i));
                        JSONObject info = materiales.getJSONObject(i);

                        data.clear();
                        data.put("idmaterial", info.getString("idmaterial"));
                        data.put("descripcion", info.getString("descripcion"));
                        System.out.println("material: " + data);
                        if (!material.create(data)) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Tags

                TagModel tag = new TagModel(context);
                try {
                    final JSONArray tags = new JSONArray(JSON.getString("Tags"));
                    for (int i = 0; i < tags.length(); i++) {
                        final int finalI = i + 1;
                       // Toast.makeText(context, "Actualizando catálogo de Tags...  ", Toast.LENGTH_SHORT).show();
                        System.out.println("tags: "+tags.getJSONObject(i));
                        JSONObject info = tags.getJSONObject(i);

                        data.clear();
                        data.put("uid", info.getString("uid"));
                        data.put("idcamion", info.getString("idcamion"));
                        data.put("idproyecto", info.getString("idproyecto"));
                        System.out.println("tag: " + data);
                        if (!tag.create(data)) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        progressDialog.dismiss();
        System.out.println("MEnsaje "+aBoolean);
        if (aBoolean) {
           Toast.makeText(context, "Se Actualizaron los Catálogos", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
        }
    }

}
