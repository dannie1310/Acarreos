package mx.grupohi.acarreos;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.PACKAGE_USAGE_STATS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import mx.grupohi.acarreos.Oauth.ErpClient;
import mx.grupohi.acarreos.Oauth.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    protected PowerManager.WakeLock wakeLock;
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView userText;
    private EditText passText;
    private TextInputLayout loginFormLayout;
    private ProgressDialog loginProgressDialog;
    private DBScaSqlite db_sca;
    Intent mainActivity;
    Usuario usuario;
    Integer tipo;
    Button loginButton;

    // GPSTracker class
    GPSTracker gps;
    double latitude;
    double longitude;
    public String IMEI;

    ///Oauth 2.0
    public String CLIENT_ID = "1";
    public String SECRET = "u12k5tax8zOQR53eRZdglLG2gpg5EuYsQqxLcOud";
    public String SECRET_DEV = "M8w73visooB9co9pJFdImbHv90mU8MuMRpR2DIUl";
    public String URL_API = "http://portal-aplicaciones.grupohi.mx/";
    public String ROUTE_CODE = URL_API + "api/movil?response_type=code&redirect_uri=/auth&client_id=" + CLIENT_ID + "&";
//    public String MOVIL = "http://192.168.0.187:8000/api/movil?client_id=11&response_type=code&redirect_uri=/auth&usuario=jlopeza&clave=123456";
    public String token_resp = "";
    private GetCode code = null;
    private JSONObject resp = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //mainActivity = new Intent(this, MainActivity.class);
        usuario = new Usuario(this);
        tipo = usuario.getTipo_permiso();

        final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "etiqueta");
        wakeLock.acquire();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.app_name));
        gps = new GPSTracker(LoginActivity.this);
        checkPermissions();
        // Set up the login form.
        userText = (AutoCompleteTextView) findViewById(R.id.userText);
        passText = (EditText) findViewById(R.id.passText);
        loginFormLayout = (TextInputLayout) findViewById(R.id.layout);

        db_sca = new DBScaSqlite(getApplicationContext(), "sca", null, 1);

        loginButton = (Button) findViewById(R.id.loginButton);
        assert loginButton != null;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions()) {
                    mAuthTask = null;
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    TelephonyManager phneMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                    IMEI = phneMgr.getDeviceId();
                    attemptLogin();
                    /* if(latitude + longitude == 0) {
                        Snackbar snackbar;
                        snackbar = Snackbar.make(view, "No se puede detectar la ubicación. ¡Espere un momento por favor e intentelo de nuevo!", Snackbar.LENGTH_SHORT);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(Color.RED);
                        snackbar.show();
                    } else {
                        attemptLogin();
                    }*/
//                    attemptLogin();
                }
            }
        });

        Crashlytics.setUserIdentifier(String.valueOf(usuario.getId()));
        Crashlytics.setUserName(usuario.getNombre());

        userText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    passText.requestFocus();
                }
                return false;
            }
        });

        passText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginButton.performClick();
                }
                return false;
            }
        });
        if (usuario.isAuth()) {
            if (tipo== null){
                Toast.makeText(LoginActivity.this, R.string.error_usuario, Toast.LENGTH_LONG).show();
                Intent intent = getIntent();
                usuario.destroy();
                startActivity(intent);
            }
            else if(tipo == 0){
                mainActivity = new Intent(this, SetOrigenActivity.class);
                startActivity(mainActivity);
            }else if(tipo == 1){
                mainActivity = new Intent(this, MainActivity.class);
                startActivity(mainActivity);
            }
        }
    }
    private Boolean checkPermissions() {
        Boolean permission_fine_location = true;
        Boolean permission_read_phone_state = true;
        Boolean permission_camara = true;
        Boolean permission_read_external = true;
        Boolean permission_write_external = true;
        Boolean _gps = true;
        Boolean internet = true;

        if(ContextCompat.checkSelfPermission(LoginActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            permission_fine_location = false;
        }

        if(ContextCompat.checkSelfPermission(LoginActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
            permission_read_phone_state =  false;
        }

        if(ContextCompat.checkSelfPermission(LoginActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
            permission_camara =  false;
        }
        if(ContextCompat.checkSelfPermission(LoginActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            permission_read_external =  false;
            permission_write_external = false;
        }

        if(!Util.isGpsEnabled(getApplicationContext())) {
            gps.showSettingsAlert();
            gps = new GPSTracker(LoginActivity.this);
            _gps = false;
        }

        if(!Util.isNetworkStatusAvialable(getApplicationContext())) {
            Toast.makeText(LoginActivity.this, R.string.error_internet, Toast.LENGTH_LONG).show();
            internet = false;
        }
        return (permission_fine_location && permission_read_phone_state && _gps && internet && permission_camara && permission_read_external && permission_write_external);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    protected void onResume(){
        super.onResume();
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Resetear errores.
        userText.setError(null);
        passText.setError(null);

        // Store values at the time of the login attempt.
        String user = userText.getText().toString();
        String pass = passText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(user)) {
            userText.setError(getResources().getString(R.string.error_field_required));
            focusView = userText;
            cancel = true;
        } else if (TextUtils.isEmpty(pass)) {
            passText.setError(getResources().getString(R.string.error_field_required));
            focusView = passText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            loginProgressDialog = ProgressDialog.show(LoginActivity.this, "Autenticando", "Por favor espere...", true);
//            mAuthTask = new UserLoginTask(user, pass);
//            mAuthTask.execute((Void) null);

            code = new GetCode(user, pass);
            code.execute();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String user;
        private final String pass;

        UserLoginTask(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            ContentValues data = new ContentValues();
            data.put("usuario", user);
            data.put("clave", pass);
            data.put("IMEI", IMEI);

            try {
//                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                URL url = new URL(URL_API + "api/acarreos/viaje-neto/catalogo?access_token=" + token_resp);
                final JSONObject JSON = HttpConnection.POST(url, data);
                db_sca.deleteCatalogos();
                if (JSON.has("error")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginFormLayout.setErrorEnabled(true);
                            try {
                                loginFormLayout.setError((String) JSON.get("error"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.log("Se ha producido el siguiente error en el Login-error: " + e.getMessage());
                                Crashlytics.logException(e);
                            }
                        }
                    });
                    return false;
                } else {

                    if(JSON.getString("IdPerfil") == "null"){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginFormLayout.setErrorEnabled(true);
                                loginFormLayout.setError(getString(R.string.error_usuario));
                            }

                        });
                        return false;
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginProgressDialog.setTitle("Actualizando");
                                loginProgressDialog.setMessage("Actualizando datos de usuario...");
                            }
                        });

                        Usuario usuario = new Usuario(getApplicationContext());

                        data.clear();
                        data.put("idusuario", JSON.getString("IdUsuario"));
                        data.put("idproyecto", JSON.getString("IdProyecto"));
                        data.put("nombre", JSON.getString("Nombre"));
                        data.put("user", user);
                        data.put("pass", pass);
                        data.put("base_datos", JSON.getString("base_datos"));
                        data.put("descripcion_database", JSON.getString("descripcion_database"));
                        data.put("empresa", JSON.getString("empresa"));
                        data.put("logo", JSON.getInt("tiene_logo"));
                        data.put("imagen", JSON.getString("logo"));
                        data.put("tipo_permiso", JSON.getString("IdPerfil"));//idperfil
                        data.put("idorigen", JSON.getString("IdOrigen"));//idorigen
                        data.put("idtiro", JSON.getString("IdTiro"));//idtiro
                        data.put("token", token_resp);//token

                        if (!usuario.create(data)) {
                            return false;
                        }

                        //Camiones
                        Camion camion = new Camion(getApplicationContext());
                        try {
                            final JSONArray camiones = new JSONArray(JSON.getString("Camiones"));
                            for (int i = 0; i < camiones.length(); i++) {
                                final int finalI = i + 1;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de camiones... \n Camion " + finalI + " de " + camiones.length());
                                    }
                                });
                                JSONObject info = camiones.getJSONObject(i);

                                data.clear();
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
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Login-camion: " + e.getMessage());
                            Crashlytics.logException(e);
                        }

                        //Tiros

                        Tiro tiro = new Tiro(getApplicationContext());
                        try {
                            final JSONArray tiros = new JSONArray(JSON.getString("Tiros"));
                            for (int i = 0; i < tiros.length(); i++) {
                                final int finalI = i + 1;
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
                            Crashlytics.log("Se ha producido el siguiente error en el Login-tiro: " + e.getMessage());
                            Crashlytics.logException(e);
                        }

                        //Origenes

                        Origen origen = new Origen(getApplicationContext());
                        try {
                            final JSONArray origenes = new JSONArray(JSON.getString("Origenes"));
                            for (int i = 0; i < origenes.length(); i++) {
                                final int finalI = i + 1;
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
                            Crashlytics.log("Se ha producido el siguiente error en el Login-origen: " + e.getMessage());
                            Crashlytics.logException(e);
                        }

                        //Tags

                        TagModel tag = new TagModel(getApplicationContext());
                        try {
                            final JSONArray tags = new JSONArray(JSON.getString("Tags"));
                            for (int i = 0; i < tags.length(); i++) {
                                final int finalI = i + 1;
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
                            Crashlytics.log("Se ha producido el siguiente error en el Login-tags: " + e.getMessage());
                            Crashlytics.logException(e);
                        }

                        //Rutas

                        Ruta ruta = new Ruta(getApplicationContext());
                        try {
                            final JSONArray rutas = new JSONArray(JSON.getString("Rutas"));
                            for (int i = 0; i < rutas.length(); i++) {
                                final int finalI = i + 1;
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
                            Crashlytics.log("Se ha producido el siguiente error en el Login-rutas: " + e.getMessage());
                            Crashlytics.logException(e);
                        }

                        //Materiales

                        Material material = new Material(getApplicationContext());
                        try {
                            final JSONArray materiales = new JSONArray(JSON.getString("Materiales"));
                            for (int i = 0; i < materiales.length(); i++) {
                                final int finalI = i + 1;
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
                            Crashlytics.log("Se ha producido el siguiente error en el Login-material: " + e.getMessage());
                            Crashlytics.logException(e);
                        }

                        //Tipos Imagenes

                        TipoImagenes tipo = new TipoImagenes(getApplicationContext());
                        try {
                            final JSONArray tags = new JSONArray(JSON.getString("TiposImagenes"));
                            for (int i = 0; i < tags.length(); i++) {
                                final int finalI = i + 1;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressDialog.setMessage("Actualizando catálogo de Tipos de Imagenes... \n  " + finalI + " de " + tags.length());
                                    }
                                });
                                JSONObject info = tags.getJSONObject(i);

                                data.clear();
                                data.put("id", info.getString("id"));
                                data.put("descripcion", info.getString("descripcion"));

                                if (!tipo.create(data)) {
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Login-tiposimagen: " + e.getMessage());
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
                            Crashlytics.log("Se ha producido el siguiente error en el Login-motivos: " + e.getMessage());
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

                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.log("Se ha producido el siguiente error en el Login-configuraciones: " + e.getMessage());
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
                            Crashlytics.log("Se ha producido el siguiente error en el Login-checadores: " + e.getMessage());
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
                            Crashlytics.log("Se ha producido el siguiente error en el Login-celulares: " + e.getMessage());
                            Crashlytics.logException(e);
                        }

                        data.clear();
                        data.put("IMEI", IMEI);
                        data.put("idevento", 1);
                        data.put("latitud", latitude);
                        data.put("longitud", longitude);
                        data.put("fecha_hora", Util.timeStamp());
                        data.put("code", "");

                        if (!Coordenada.create(data, LoginActivity.this)) {
                            return false;
                        }
                        wakeLock.release();
                        return true;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.log("Se ha producido el siguiente error en el Login: " + e.getMessage());
                Crashlytics.logException(e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            loginProgressDialog.dismiss();
            if (success) {
                mAuthTask = null;
                if (usuario.isAuth()) {
                    tipo = usuario.getTipo_permiso();
                    if(tipo == 0){
                        mainActivity = new Intent(getApplicationContext(), SetOrigenActivity.class);
                        startActivity(mainActivity);
                    }else if(tipo == 1){
                        mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(mainActivity);
                    }
                }
            }else{
                Toast.makeText(getApplicationContext(), "Error al iniciar sesion",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            loginProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private class GetCode extends AsyncTask<Void, Void, Boolean> {

        private final String user;
        private final String pass;

        GetCode(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }
        protected Boolean doInBackground(Void... urls) {
            String body = " ";

            try {
                URL url = new URL(ROUTE_CODE + "usuario=" + user + "&clave=" + pass);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                String codigoRespuesta = Integer.toString(urlConnection.getResponseCode());
                if(codigoRespuesta.equals("200")){//Vemos si es 200 OK y leemos el cuerpo del mensaje.
                    body = readStream(urlConnection.getInputStream());
                    resp = new JSONObject(body);
                    String codec = resp.get("code").toString();

                    Retrofit.Builder builder = new Retrofit.Builder()
                            .baseUrl(URL_API)
                            .addConverterFactory(GsonConverterFactory.create());
                    Retrofit retrofit = builder.build();

                    ErpClient client = retrofit.create(ErpClient.class);
                    Call<Token> getAccessToken =  client.getToken(
                            CLIENT_ID,
                            SECRET,
                            codec,
                            "authorization_code",
                            "/auth"
                    );
                    getAccessToken.enqueue(new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            token_resp = response.body().getAccessToken();
                            mAuthTask = new UserLoginTask(user, pass);
                            mAuthTask.execute((Void) null);
//                            Toast.makeText(LoginActivity.this, "Yes" + response.body().getAccessToken(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "Error al obtener Token", Toast.LENGTH_SHORT).show();
                            loginProgressDialog.dismiss();
                        }
                    });
                    urlConnection.disconnect();

                }else{
                    return false;
                }


            } catch (Exception e) {
                e.getStackTrace();//Error diferente a los anteriores.
            }
            return true;
        }


        protected void onPostExecute(Boolean result) {
            if(!result){
                Toast.makeText(LoginActivity.this, "Error al obtener Token", Toast.LENGTH_SHORT).show();
                loginProgressDialog.dismiss();
            }
        }
    }
    private static String readStream(InputStream in) throws IOException {

        BufferedReader r = null;
        r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        if(r != null){
            r.close();
        }
        in.close();
        return total.toString();
    }
}

