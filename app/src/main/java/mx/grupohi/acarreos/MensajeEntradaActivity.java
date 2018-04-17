package mx.grupohi.acarreos;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import mx.grupohi.acarreos.Mina.SalidaMina;
import mx.grupohi.acarreos.TiposTag.TagNFC;

public class MensajeEntradaActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Objetos
    private Usuario usuario;
    private TagNFC tagNFC;
    private Camion c;
    private ContentValues datosVista;

    //Referencias UI
    private LinearLayout mainLayout;
    private LinearLayout lecturaTag;
    private ImageView nfcImage;
    private FloatingActionButton fabCancel;
    private ProgressDialog progressDialogSync;

    EditText deduc;
    String mensaje;

    //GPS
    private GPSTracker gps;
    private String IMEI;
    private Double latitude;
    private Double longitude;

    //NFC
    private NFCTag nfcTag;
    private NFCUltralight nfcUltra;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private Boolean writeMode;
    Certificados certificados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        certificados = new Certificados(getApplicationContext());
        setContentView(R.layout.activity_set_origen);
        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        tagNFC = (TagNFC) getIntent().getSerializableExtra("datos");
        c = new Camion(getApplicationContext());
        c = c.find(tagNFC.getIdcamion());
        gps = new GPSTracker(MensajeEntradaActivity.this);
        datosVista = new ContentValues();
        TelephonyManager phneMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        IMEI = phneMgr.getDeviceId();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final Boolean[] ok = {false};
        final android.app.AlertDialog.Builder alerta = new android.app.AlertDialog.Builder(getApplicationContext());
        View vista = getLayoutInflater().inflate(R.layout.popup, null);
        deduc = (EditText) vista.findViewById(R.id.etPopAgregar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        nfcImage = (ImageView) findViewById(R.id.imageViewNFC);
        fabCancel = (FloatingActionButton) findViewById(R.id.fabCancel);
        mainLayout = (LinearLayout) findViewById(R.id.MainLayout);
        lecturaTag = (LinearLayout) findViewById(R.id.leerTag);
        lecturaTag.setVisibility(View.GONE);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, getString(R.string.error_no_nfc), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkNfcEnabled();

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        writeTagFilters = new IntentFilter[]{tagDetected};

        alerta.setTitle("¿Desea Agregar un Nuevo Volumen?");
        alerta.setView(vista);
        alerta.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!validarCampos()) {
                    Toast.makeText(getApplicationContext(), "Por favor escribir el volumen", Toast.LENGTH_SHORT).show();
                } else {
                    checkNfcEnabled();
                    WriteModeOn();
                }
            }
        });
        alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Favor de Pasar a la Salida para Finalizar el Viaje.", Toast.LENGTH_LONG).show();
                Intent success = new Intent(getApplicationContext(), SetOrigenActivity.class);
                startActivity(success);
                dialog.cancel();
            }
        });
        alerta.show();

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteModeOff();
            }
        });

        if (drawer != null)
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
                        if (tim != null) {
                            if (impresora == 0) {
                                tim.setTextColor(Color.RED);
                                tim.setText("Sin Impresora Asignada");
                            } else {
                                tim.setText("Impresora " + impresora);
                            }
                        }
                        if (tvp != null) {
                            tvp.setText(usuario.descripcionBaseDatos);
                        }
                        if (tvu != null) {
                            tvu.setText(usuario.nombre);
                        }
                        if (tpe != null) {
                            if (usuario.origen_name == "0") {
                                tpe.setText("PERFIL: " + usuario.getNombreEsquema() + " - " + usuario.tiro_name);
                            } else if (usuario.tiro_name == "0") {
                                tpe.setText("PERFIL: " + usuario.getNombreEsquema() + " - " + usuario.origen_name);
                            }
                        }
                        if (tvv != null) {
                            tvv.setText(getString(R.string.app_name) + "     " + "Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                        }
                    }
                }
            });
    }

    private Boolean validarCampos(){
        /// validar volumen entrada
        if(deduc.getText().toString().isEmpty()){
            mensaje = "Por favor escribir el volumen";
            deduc.requestFocus();
            return false;
        }
        if(foliosSinCeros(deduc.getText().toString())){
            mensaje = "El volumen no puede ser cero.";
            deduc.requestFocus();
            return false;
        }
        if(c.capacidad != 0 && c.capacidad!= null && Integer.valueOf(deduc.getText().toString()) > c.capacidad) {
            mensaje = "El volumen es mayor a la capacidad del camión.";
            deduc.requestFocus();
            return false;
        }

        /// asignacion de valores
        datosVista.put("idmaterial", tagNFC.getIdmaterial());
        datosVista.put("idorigen", tagNFC.getIdorigen());
        datosVista.put("folio_mina", "");
        datosVista.put("folio_seguimiento", "");
        datosVista.put("idusuario", String.valueOf(usuario.getId()));
        datosVista.put("IMEI", IMEI);
        return true;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        new MensajeTarea(getApplicationContext(), intent).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNfcEnabled();
        //  WriteModeOff();
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    class MensajeTarea extends AsyncTask<Void, Void, Boolean> {
        Context context;
        Intent intent;
        String mensaje_error = "";
        Integer IdInicio;
        public MensajeTarea(Context context, Intent intent) {
            this.context = context;
            this.intent = intent;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Tag myTag;
            //// se lee el tag y se inicializa la clase con los datos
            if (writeMode) {
                if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                    myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    String[] techs = myTag.getTechList();
                    for (String t : techs) {
                        if (MifareClassic.class.getName().equals(t)) {
                            nfcTag = new NFCTag(myTag, context);
                            if (tagNFC.getUID().equals(nfcTag.byteArrayToHexString(myTag.getId()))) {
                                mensaje_error = "continuar";
                            } else {
                                mensaje_error = "¡Error! Utilice el mismo tag.";
                                return false;
                            }
                        }
                        if (MifareUltralight.class.getName().equals(t)) {
                            nfcUltra = new NFCUltralight(myTag, context);
                            if (tagNFC.getUID().equals(nfcUltra.byteArrayToHexString(myTag.getId()))) {
                                mensaje_error = "continuar";
                            } else {
                                mensaje_error = "¡Error! Utilice el mismo tag.";
                                return false;
                            }
                        }
                    }
                }
            }
            if (mensaje_error == "continuar") {
                datosVista.put("idcamion", tagNFC.getIdcamion());
                datosVista.put("fecha_origen", Util.getFormatDate(tagNFC.getFecha()));
                datosVista.put("uidTAG", tagNFC.getUID());
                datosVista.put("estatus", 1);
                datosVista.put("tipoEsquema", tagNFC.getTipo_viaje());
                datosVista.put("idperfil", usuario.tipo_permiso);
                datosVista.put("deductiva_entrada", 1);
                datosVista.put("deductiva", deduc.toString());
                datosVista.put("idMotivo", tagNFC.getIdmotivo());
                datosVista.put("numImpresion", 0);
                datosVista.put("tipo_suministro", tagNFC.getTipo_viaje());
                SalidaMina salida_mina = new SalidaMina(context, tagNFC);
                if (!salida_mina.guardarDatosDB(datosVista)) {
                    mensaje_error = "Error al guardar en Base de Datos";
                    return false;
                } else {
                    IdInicio = salida_mina.idInicio;
                    myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    if (tagNFC.getTipo() == 1) {
                        if (tagNFC.getUID().equals(nfcTag.byteArrayToHexString(myTag.getId()))) {
                            try {
                                nfcTag.writeSector(myTag, 4, 16, deduc.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje_error = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcTag.writeSector(myTag, 4, 17, tagNFC.getIdmotivo());
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje_error = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                        } else {
                            mensaje_error = "¡Error! Utilice el mismo tag.";
                            return false;
                        }
                    }
                    if (tagNFC.getTipo() == 2) {
                        if (tagNFC.getUID().equals(nfcUltra.byteArrayToHexString(myTag.getId()))) {
                            try {
                                nfcUltra.writePagina(null, 19, deduc.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje_error = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcUltra.writePagina(null, 20, tagNFC.getIdmotivo());
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje_error = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                        } else {
                            mensaje_error = "¡Error! Utilice el mismo tag.";
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean registro) {
            super.onPostExecute(registro);
            WriteModeOff();
            if (registro){
                Intent success = new Intent(getApplicationContext(), SuccessDestinoActivity.class);
                success.putExtra("idInicio", IdInicio);
                startActivity(success);
            }else {
                Toast.makeText(context, mensaje_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        mainLayout.setVisibility(View.GONE);
        lecturaTag.setVisibility(View.VISIBLE);
    }

    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
        mainLayout.setVisibility(View.VISIBLE);
        lecturaTag.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
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
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }else if(tipo == 1){
                mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivity);
            }
        } else if (id == R.id.nav_sync) {
            new AlertDialog.Builder(getApplicationContext())
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                                    progressDialogSync = ProgressDialog.show(MensajeEntradaActivity.this, "Sincronizando datos", "Por favor espere...", true);
                                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);
                                    tiempoEsperaSincronizacion();
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
            Intent listActivity = new Intent(this, ListaViajesActivity.class);
            startActivity(listActivity);

        } else if (id == R.id.nav_desc) {

            Intent descarga = new Intent(this, DescargaActivity.class);
            startActivity(descarga);

        }  else if (id == R.id.nav_logout) {
            if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                new AlertDialog.Builder(MensajeEntradaActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(MensajeEntradaActivity.this, "Sincronizando datos", "Por favor espere...", true);
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
            else {
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

    public  void tiempoEsperaSincronizacion(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                Intent mainActivity;
                Integer tipo = usuario.getTipo_permiso();
                if(tipo == 0){
                    mainActivity = new Intent(getApplicationContext(), MensajeEntradaActivity.class);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainActivity);
                }else if(tipo == 1){
                    mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainActivity);
                }
            }
        }, 8000);
    }

    private void checkNfcEnabled() {
        Boolean nfcEnabled = nfcAdapter.isEnabled();
        if (!nfcEnabled) {
            new android.app.AlertDialog.Builder(MensajeEntradaActivity.this)
                    .setTitle(getString(R.string.text_warning_nfc_is_off))
                    .setMessage(getString(R.string.text_turn_on_nfc))
                    .setCancelable(true)
                    .setPositiveButton(
                            getString(R.string.text_update_settings),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            })
                    .create()
                    .show();
        }
    }

    private Boolean foliosSinCeros(String folio){
        if(folio.replace("0","").trim().equals("")){
            return true;
        }
        return false;
    }
}
