package mx.grupohi.acarreos;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuAdapter;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class DescargaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    protected PowerManager.WakeLock wakeLock;
    //Referencias UI
    private LinearLayout infoLayout;
    private LinearLayout origenLayout;
    private Button actionButton;
    private ImageView nfcImage;
    private TextView infoTag;
    private ProgressDialog progressDialogSync;
    private TextInputLayout loginFormLayout;
    private ProgressDialog loginProgressDialog;
    private NFCTag nfc;
    private NFCUltralight nfcUltra;
    private NfcAdapter nfc_adapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private Snackbar snackbar;
    private String UID;
    private Integer idOrigen;
    private Intent mainActivity;
    private Intent listaViajes;
    Usuario usuario;
    Viaje viaje;
    Coordenada coordenada;


    // GPSTracker class
    GPSTracker gps;
    public String IMEI = "N/A";

    public String URL_API = "http://192.168.0.187:8000/";

    private DescargaCatalogos descargaCatalogos = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        mainActivity = new Intent(this, MainActivity.class);
        final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "etiqueta");
        wakeLock.acquire();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        TelephonyManager phneMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if(phneMgr.getDeviceId() != null){
            IMEI = phneMgr.getDeviceId();
        }
        if(drawer != null)
            drawer.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < drawer.getChildCount(); i++) {
                        View child = drawer.getChildAt(i);
                        TextView tvp = (TextView) child.findViewById(R.id.textViewProyecto);
                        TextView tvu = (TextView) child.findViewById(R.id.textViewUser);
                        TextView tpe = (TextView) child.findViewById(R.id.textViewPerfil);
                        TextView tvv = (TextView) child.findViewById(R.id.textViewVersion);
                        TextView tim = (TextView) child.findViewById(R.id.textViewImpresora);

                        Integer impresora = CelularImpresora.getId(getApplicationContext());
                        if (tim != null){
                            if(impresora == 0){
                                tim.setTextColor(Color.RED);
                                tim.setText("Sin Impresora Asignada");
                            }else{
                                tim.setText("Impresora "+impresora);
                            }
                        }
                        if (tvp != null) {
                            tvp.setText(usuario.descripcionBaseDatos);
                        }
                        if (tvu != null) {
                            tvu.setText(usuario.nombre);
                        }
                        if (tpe != null){
                            if(usuario.origen_name == "0"){
                                tpe.setText("PERFIL: "+usuario.getNombreEsquema()+" - "+usuario.tiro_name);
                            }else if(usuario.tiro_name == "0"){
                                tpe.setText("PERFIL: "+usuario.getNombreEsquema()+" - "+usuario.origen_name);
                            }
                        }
                        if (tvv != null) {
                            tvv.setText(getString(R.string.app_name)+"     "+"Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                        }
                    }
                }
            });
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(!Util.isNetworkStatusAvialable(getApplicationContext())) {
            Toast.makeText(DescargaActivity.this, R.string.error_internet, Toast.LENGTH_LONG).show();

        }

        loginProgressDialog = ProgressDialog.show(DescargaActivity.this, "Descargando", "Por favor espere...", true);
        descargaCatalogos = new DescargaActivity.DescargaCatalogos(getApplicationContext(), progressDialogSync);
        descargaCatalogos.execute((Void) null);



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent mainActivity;
            Integer tipo = usuario.getTipo_permiso();
            if(tipo == 0){
                mainActivity = new Intent(getApplicationContext(), SetOrigenActivity.class);
                startActivity(mainActivity);
            }else if(tipo == 1){
                mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivity);
            }

        } else if (id == R.id.nav_sync) {
            new AlertDialog.Builder(DescargaActivity.this)
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                                    progressDialogSync = ProgressDialog.show(DescargaActivity.this, "Sincronizando datos", "Por favor espere...", true);
                                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);

                                } else {
                                    Toast.makeText(getApplicationContext(), "No es necesaria la sincronización en este momento", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.error_internet, Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .create()
                    .show();

        } else if (id == R.id.nav_list) {
            startActivity(listaViajes);
        }else if (id == R.id.nav_desc) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                new AlertDialog.Builder(DescargaActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(DescargaActivity.this, "Sincronizando datos", "Por favor espere...", true);
                                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);
                                    Intent login_activity = new Intent(getApplicationContext(), LoginActivity.class);
                                    usuario.destroy();
                                    startActivity(login_activity);
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.error_internet, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .create()
                        .show();
            }
           else{
                Intent login_activity = new Intent(getApplicationContext(), LoginActivity.class);
                usuario.destroy();
                startActivity(login_activity);
            }
        }else if(id == R.id.nav_cambio){
            Intent cambio = new Intent(this, CambioClaveActivity.class);
            startActivity(cambio);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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
            data.put("usuario", usuario.usr);
            data.put("clave", usuario.pass);
            data.put("IMEI", IMEI);

            try {
//                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                URL url = new URL(URL_API + "api/acarreos/viaje-neto/catalogo?access_token=" + usuario.token);
                JSON = HttpConnection.POST(url, data);

                if (JSON.has("error")) {
                    Toast.makeText(context, "Error al descargar.", Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    if (JSON.getString("IdPerfil") == "null") {
                        return false;
                    } else {
                        try {
                            Util.copyDataBaseSyns(context);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga");
                            Crashlytics.logException(e);
                        }
                        db_sca.descargaCatalogos();
                        try {
                            String logo = JSON.getString("logo");
                            String perfil = JSON.getString("IdPerfil");//idperfil
                            String origen = JSON.getString("IdOrigen");//idorigen
                            String tiro = JSON.getString("IdTiro");//idtiro

                            Usuario.updateLogo(logo, perfil, origen, tiro, getApplicationContext());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-Usuario: ");
                            Crashlytics.logException(e);
                        }
                        //Camiones
                        Camion camion = new Camion(context);
                        try {
                            final JSONArray camiones = new JSONArray(JSON.getString("Camiones"));
                            for (int i = 0; i < camiones.length(); i++) {
                                final int finalI = i + 1;

                                // Toast.makeText(context, "Actualizando catálogo de camiones...", Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de camiones... \n Camion " + finalI + " de " + camiones.length());
                                    }
                                });
                                JSONObject info = camiones.getJSONObject(i);

                                data.clear();
                                // if (!Camion.findId(Integer.valueOf(info.getString("idcamion")), context)) { (Validar si existe el camión)

                                data.put("idcamion", info.getString("idcamion"));
                                data.put("placas", info.getString("placas"));
                                data.put("marca", info.getString("marca"));
                                data.put("modelo", info.getString("modelo"));
                                data.put("ancho", info.getString("ancho"));
                                data.put("largo", info.getString("largo"));
                                data.put("alto", info.getString("alto"));
                                data.put("economico", info.getString("economico"));
                                data.put("capacidad", info.getString("capacidad"));
                                data.put("placasCaja", info.getString("placas_caja"));
                                data.put("estatus", info.getString("estatus"));

                                if (!camion.create(data)) {
                                    return false;
                                }
                                // }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-camion: ");
                            Crashlytics.logException(e);
                        }

                        //Tiros

                        Tiro tiro = new Tiro(context);
                        try {
                            final JSONArray tiros = new JSONArray(JSON.getString("Tiros"));
                            for (int i = 0; i < tiros.length(); i++) {
                                final int finalI = i + 1;
                                // Toast.makeText(context, "Actualizando catálogo de tiros...", Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de tiros... \n Tiro " + finalI + " de " + tiros.length());
                                    }
                                });
                                JSONObject info = tiros.getJSONObject(i);

                                data.clear();
                                data.put("idtiro", info.getString("idtiro"));
                                data.put("descripcion", info.getString("descripcion"));

                                if (!tiro.create(data)) {
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-tiro: ");
                            Crashlytics.logException(e);
                        }

                        //Origenes

                        Origen origen = new Origen(context);
                        try {
                            final JSONArray origenes = new JSONArray(JSON.getString("Origenes"));
                            for (int i = 0; i < origenes.length(); i++) {
                                final int finalI = i + 1;
                                // Toast.makeText(context, "Actualizando catálogo de Origenes... ", Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de Origenes... \n Origen " + finalI + " de " + origenes.length());
                                    }
                                });
                                JSONObject info = origenes.getJSONObject(i);

                                data.clear();
                                data.put("idorigen", info.getString("idorigen"));
                                data.put("descripcion", info.getString("descripcion"));
                                data.put("estado", info.getString("estado"));


                                if (!origen.create(data)) {
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-origen: " );
                            Crashlytics.logException(e);
                        }

                        //Rutas

                        Ruta ruta = new Ruta(context);
                        try {
                            final JSONArray rutas = new JSONArray(JSON.getString("Rutas"));
                            for (int i = 0; i < rutas.length(); i++) {
                                final int finalI = i + 1;
                                //Toast.makeText(context, "Actualizando catálogo de Rutas... ", Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de Rutas... \n Ruta " + finalI + " de " + rutas.length());
                                    }
                                });
                                JSONObject info = rutas.getJSONObject(i);

                                data.clear();
                                data.put("idruta", info.getString("idruta"));
                                data.put("clave", info.getString("clave"));
                                data.put("idorigen", info.getString("idorigen"));
                                data.put("idtiro", info.getString("idtiro"));
                                data.put("totalkm", info.getString("totalkm"));
                                if (!ruta.create(data)) {
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-ruta: ");
                            Crashlytics.logException(e);
                        }

                        //Materiales

                        Material material = new Material(context);
                        try {
                            final JSONArray materiales = new JSONArray(JSON.getString("Materiales"));
                            for (int i = 0; i < materiales.length(); i++) {
                                final int finalI = i + 1;
                                //Toast.makeText(context, "Actualizando catálogo de Materiales...", Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de Materiales... \n Material " + finalI + " de " + materiales.length());
                                    }
                                });
                                JSONObject info = materiales.getJSONObject(i);

                                data.clear();
                                data.put("idmaterial", info.getString("idmaterial"));
                                data.put("descripcion", info.getString("descripcion"));

                                if (!material.create(data)) {
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-material: ");
                            Crashlytics.logException(e);
                        }

                        //Checadores

                        Checador checador = new Checador(getApplicationContext());
                        try {
                            final JSONArray ch = new JSONArray(JSON.getString("Checadores"));
                            for (int i = 0; i < ch.length(); i++) {
                                final int finalI = i + 1;
                                // Toast.makeText(context, "Actualizando catálogo de tiros...", Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de Checadores... \n Checador " + finalI + " de " + ch.length());
                                    }
                                });
                                JSONObject info = ch.getJSONObject(i);

                                data.clear();
                                data.put("idChecador", info.getString("id"));
                                data.put("nombre", info.getString("descripcion"));

                                if (!checador.create(data)) {
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga: ");
                            Crashlytics.logException(e);
                        }


                        //Motivo Deduccion

                        Motivo motivo = new Motivo(getApplicationContext());
                        try {
                            final JSONArray motivos = new JSONArray(JSON.getString("MotivosDeductiva"));
                            for (int i = 0; i < motivos.length(); i++) {
                                final int finalI = i + 1;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de motivos... \n Motivo " + finalI + " de " + motivos.length());
                                    }
                                });
                                JSONObject info = motivos.getJSONObject(i);

                                data.clear();
                                data.put("id", info.getString("id"));
                                data.put("descripcion", info.getString("motivo"));

                                if (!motivo.create(data)) {
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-motivo: ");
                            Crashlytics.logException(e);
                        }

                        //Configuraciones

                        Configuraciones co = new Configuraciones(getApplicationContext());
                        try {

                            JSONObject aq = new JSONObject(JSON.getString("Configuracion"));

                            data.clear();
                            data.put("validacion_placas", aq.getString("ValidacionPlacas"));

                            if (!co.create(data)) {
                                return false;
                            }
                            // }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-config: ");
                            Crashlytics.logException(e);
                        }

                        //Tags

                        TagModel tag = new TagModel(context);
                        try {
                            final JSONArray tags = new JSONArray(JSON.getString("Tags"));
                            for (int i = 0; i < tags.length(); i++) {
                                final int finalI = i + 1;
                                // Toast.makeText(context, "Actualizando catálogo de Tags...  ", Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de Tags... \n Tag " + finalI + " de " + tags.length());
                                    }
                                });
                                JSONObject info = tags.getJSONObject(i);

                                data.clear();
                                data.put("uid", info.getString("UID"));
                                data.put("idcamion", info.getString("idcamion"));
                                data.put("idproyecto", info.getString("idproyecto"));
                                data.put("economico", info.getString("Economico"));
                                data.put("estatus", info.getString("Estatus"));

                                if (!tag.create(data)) {
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-tag: ");
                            Crashlytics.logException(e);
                        }
                        //Celulares

                        CelularImpresora celular = new CelularImpresora(getApplicationContext());
                        try {
                            final JSONArray celulares = new JSONArray(JSON.getString("Celulares"));
                            for (int i = 0; i < celulares.length(); i++) {
                                final int finalI = i + 1;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de celulares... \n Celular " + finalI + " de " + celulares.length());
                                    }
                                });
                                JSONObject info = celulares.getJSONObject(i);

                                data.clear();
                                data.put("ID", info.getString("id"));
                                data.put("IMEI", info.getString("IMEI"));
                                data.put("MAC",  info.getString("MAC"));

                                if (!celular.create(data)) {
                                    return true;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Descarga-celulares: ");
                            Crashlytics.logException(e);
                        }

                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.log("Se ha producido el siguiente error en el Descarga");
                Crashlytics.logException(e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            descargaCatalogos = null;
            loginProgressDialog.dismiss();
            if (success) {
                Intent mainActivity;
                Integer tipo = usuario.getTipo_permiso();
                if(tipo == 0){
                    mainActivity = new Intent(getApplicationContext(), SetOrigenActivity.class);
                    startActivity(mainActivity);
                }else if(tipo == 1){
                    mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainActivity);
                }
            }else{
            Toast.makeText(getApplicationContext(), "Error al iniciar sesion",Toast.LENGTH_SHORT).show();
            }
        }
    }


}
