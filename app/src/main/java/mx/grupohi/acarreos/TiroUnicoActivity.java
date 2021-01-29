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
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import mx.grupohi.acarreos.Destino.TiroLibre;
import mx.grupohi.acarreos.TiposTag.TagNFC;

public class TiroUnicoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Objetos
    private Usuario usuario;
    private Material material;
    private Origen origen;
    private Camion camion;
    private Ruta ruta;
    private TagNFC tagNFC;

    //Variables
    private Integer idMaterial;
    private Integer idOrigen;
    private Intent destinoSuccess;

    //Referencias UI
    Spinner materialesSpinner;
    private Spinner origenesSpinner;
    private Button escribirButton;
    private LinearLayout mainLayout;
    private ImageView nfcImage;
    private FloatingActionButton fabCancel;
    private TextView tagAlertTextView;
    private TextView text_origen;
    private EditText textDeductiva;
    private Spinner rutasSpinner;
    private EditText observaciones;
    private HashMap<String, String> spinnerMotivosMap;
    private HashMap<String, String> spinnerRutasMap;
    private TextView mensajeTextView;

    private ProgressDialog progressDialogSync;

    private Snackbar snackbar;

    //GPS
    private GPSTracker gps;
    Double latitud;
    Double longitud;
    AlertDialog alerta = null;
    private String IMEI = "N/A";

    private TextInputLayout mina,
            seg;
    private TextView textmina,
            textseg;
    //NFC
    private NFCTag nfcTag;
    private NFCUltralight nfcUltra;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private Boolean writeMode;
    private Integer idMotivo;
    private String UID;
    private Integer idcamion;
    private Integer idruta;
    private Integer error_eliminar = 0;// error de borrado de Tag (si es 0 no se ha realizado ninguna lectura, si es 1 no elimino correctamente)
    private LinearLayout tiro;
    private String mensaje = "";
    private ContentValues datosVista;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiro_unico);
        usuario = new Usuario(getApplicationContext());
        usuario = usuario.getUsuario();

        escribirButton = (Button) findViewById(R.id.buttonEscribir);

        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        material = new Material(this);
        origen = new Origen(this);
        camion = new Camion(getApplicationContext());
        gps = new GPSTracker(getApplicationContext());
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        ruta = new Ruta(getApplicationContext());

        tagNFC = (TagNFC) getIntent().getSerializableExtra("datos");
        camion = camion.find(tagNFC.getIdcamion());
        datosVista = new ContentValues();

        destinoSuccess = new Intent(this, SuccessDestinoActivity.class);
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
        if(phneMgr.getDeviceId() != null){
            IMEI = phneMgr.getDeviceId();
        }

        mina = (TextInputLayout) findViewById(R.id.textomina);
        seg = (TextInputLayout) findViewById(R.id.seg);
        textmina = (TextView) findViewById(R.id.vale_mina);
        textseg = (TextView) findViewById(R.id.seguimiento);
        tiro = (LinearLayout) findViewById(R.id.leerTag);
        tiro.setVisibility(View.GONE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        nfcImage = (ImageView) findViewById(R.id.imageViewNFC);
        fabCancel = (FloatingActionButton) findViewById(R.id.fabCancel);
        mainLayout = (LinearLayout) findViewById(R.id.MainLayout);
        tagAlertTextView = (TextView) findViewById(R.id.textViewMensaje);

        text_origen = (TextView) findViewById(R.id.textView5);
        materialesSpinner = (Spinner) findViewById(R.id.spinnerMateriales);
        origenesSpinner = (Spinner) findViewById(R.id.spinnerOrigenes);
        mensajeTextView = (TextView) findViewById(R.id.textViewMensaje);
        rutasSpinner = (Spinner) findViewById(R.id.spinnerRutass);
        textDeductiva = (EditText) findViewById(R.id.textDeductiva);
        observaciones = (EditText) findViewById(R.id.textObservaciones);

        final ArrayList<String> descripcionesOrigenes = origen.getArrayListDescripciones();
        final ArrayList<String> idsOrigenes = origen.getArrayListId();

        final String[] spinnerOrigenesArray = new String[idsOrigenes.size()];
        final HashMap<String, String> spinnerOrigenesMap = new HashMap<>();
        Crashlytics.setUserEmail("IMEI:"+IMEI);
        for (int i = 0; i < idsOrigenes.size(); i++) {
            spinnerOrigenesMap.put(descripcionesOrigenes.get(i), idsOrigenes.get(i));
            spinnerOrigenesArray[i] = descripcionesOrigenes.get(i);
            // idOrigen = usuario.idorigen; // origen o tiro asignado
        }

        final ArrayAdapter<String> arrayAdapterOrigenes = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spinnerOrigenesArray);
        arrayAdapterOrigenes.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        origenesSpinner.setAdapter(arrayAdapterOrigenes);

        origenesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String descripcion = origenesSpinner.getSelectedItem().toString();
                idOrigen = Integer.valueOf(spinnerOrigenesMap.get(descripcion));

                final ArrayList<String> descripcionesRutas = ruta.getArrayListDescripciones(idOrigen, usuario.idtiro);
                final ArrayList <String> idsRutas = ruta.getArrayListId(idOrigen,  usuario.idtiro);

                final String[] spinnerRutasArray = new String[idsRutas.size()];
                spinnerRutasMap = new HashMap<>();

                for (int i = 0; i < idsRutas.size(); i++) {
                    spinnerRutasMap.put(descripcionesRutas.get(i), idsRutas.get(i));
                    spinnerRutasArray[i] = descripcionesRutas.get(i);
                }

                final ArrayAdapter<String> arrayAdapterRutas = new ArrayAdapter<>(TiroUnicoActivity.this, R.layout.support_simple_spinner_dropdown_item, spinnerRutasArray);
                arrayAdapterRutas.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                rutasSpinner.setAdapter(arrayAdapterRutas);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        rutasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String descripcion = String.valueOf(parent.getItemAtPosition(position));
                idruta = Integer.valueOf(spinnerRutasMap.get(descripcion));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final ArrayList<String> descripcionesMateriales = material.getArrayListDescripciones();
        final ArrayList<String> idsMateriales = material.getArrayListId();

        final String[] spinnerMaterialesArray = new String[idsMateriales.size()];
        final HashMap<String, String> spinnerMaterialesMap = new HashMap<>();

        for (int i = 0; i < idsMateriales.size(); i++) {
            spinnerMaterialesMap.put(descripcionesMateriales.get(i), idsMateriales.get(i));
            spinnerMaterialesArray[i] = descripcionesMateriales.get(i);
        }

        final ArrayAdapter<String> arrayAdapterMateriales = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spinnerMaterialesArray);
        arrayAdapterMateriales.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        materialesSpinner.setAdapter(arrayAdapterMateriales);

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


        materialesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String descripcion = materialesSpinner.getSelectedItem().toString();
                idMaterial = Integer.valueOf(spinnerMaterialesMap.get(descripcion));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertNoGps();
        }

        escribirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    AlertNoGps();
                } else {
                    latitud = gps.getLatitude();
                    longitud = gps.getLongitude();

                    if (latitud.toString() != "0.0" || longitud.toString() != "0.0") {
                        if (!validarCampos()) {
                            alert(mensaje);
                        } else {
                            checkNfcEnabled();
                            WriteModeOn();
                        }
                    } else {
                        alert("ESPERE, NO SE DETECTA SU UBICACIÓN");
                    }
                }
            }
        });

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
    }

    private Boolean validarCampos(){
        if (idMaterial == 0) {
            mensaje = "Por favor seleccione un Material de la lista";
            materialesSpinner.requestFocus();
            return false;
        }
        if (idOrigen == 0 && Origen.getCount(getApplicationContext()) != 0) {
            mensaje ="Por favor seleccione un Origen de la lista";
            origenesSpinner.requestFocus();
            return false;
        }
        if (idruta == 0 && Ruta.getCount(getApplicationContext(),idOrigen, usuario.idtiro) != 0) {
            mensaje ="Por favor seleccione un Tiro de la lista";
            rutasSpinner.requestFocus();
            return false;
        }
        if(textmina.getText().toString().isEmpty()){
            mensaje = "Por favor ingrese el folio de mina";
            textmina.requestFocus();
            return false;
        }
        if(foliosSinCeros(textmina.getText().toString())){
            mensaje = "Por favor ingrese un folio de mina valido (No es permitido usar únicamente ceros).";
            textmina.requestFocus();
            return false;
        }
        if(textseg.getText().toString().isEmpty()){
            mensaje = "Por favor ingrese el folio de seguimiento";
            textseg.requestFocus();
            return false;
        }
        if(foliosSinCeros(textseg.getText().toString())){
            mensaje = "Por favor ingrese un folio de seguimiento valido (No es permitido usar únicamente ceros).";
            textseg.requestFocus();
            return false;
        }
        if (textDeductiva.getText().toString().isEmpty()){
            mensaje = "Por favor ingrese un volumen";
            textDeductiva.requestFocus();
            return false;
        }
        if(foliosSinCeros(textDeductiva.getText().toString())){
            mensaje = "El volumen no puede ser cero.";
            textDeductiva.requestFocus();
            return false;
        }
        if(camion.capacidad != 0 && camion.capacidad!= null && Integer.valueOf(textDeductiva.getText().toString()) > camion.capacidad) {
            mensaje = "El volumen es mayor a la capacidad del camión.";
            textDeductiva.requestFocus();
            return false;
        }

        /// asignacion de valores
        datosVista.put("IdMaterial", idMaterial);
        datosVista.put("IdOrigen", idOrigen);
        datosVista.put("IdTiro", usuario.idtiro);
        datosVista.put("Ruta", idruta);
        datosVista.put("deductiva", textDeductiva.getText().toString());
        datosVista.put("idmotivo", tagNFC.getIdmotivo());
        datosVista.put("folio_mina", textmina.getText().toString());
        datosVista.put("folio_seguimiento", textseg.getText().toString());
        datosVista.put("Observaciones", observaciones.getText().toString());
        datosVista.put("Creo", String.valueOf(usuario.getId()));
        datosVista.put("IMEI", IMEI);
        datosVista.put("deductiva_origen", 0);
        datosVista.put("idmotivo_origen", tagNFC.getIdmotivo());
        datosVista.put("deductiva_entrada", 0);
        datosVista.put("idmotivo_entrada", 0);
        RandomString r = new RandomString(10);
        datosVista.put("FolioRandom", r.nextString().toUpperCase());
        datosVista.put("primerToque", String.valueOf(usuario.getId()));
        datosVista.put("tipoEsquema", usuario.getTipoEsquema());
        datosVista.put("numImpresion", 0);
        datosVista.put("idperfil", usuario.tipo_permiso);
        datosVista.put("Estatus", "1");
        datosVista.put("cubicacion", textDeductiva.getText().toString());

        return true;
    }

    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Debe activar el GPS")
                .setCancelable(false)
                .setPositiveButton("ACTIVAR", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        alerta = builder.create();
        alerta.show();
    }


    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);

        mainLayout.setVisibility(View.GONE);
        tiro.setVisibility(View.VISIBLE);
    }

    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
        mainLayout.setVisibility(View.VISIBLE);
        tiro.setVisibility(View.GONE);
    }

    private void checkNfcEnabled() {
        Boolean nfcEnabled = nfcAdapter.isEnabled();
        if (!nfcEnabled) {
            new android.app.AlertDialog.Builder(TiroUnicoActivity.this)
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
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    class TiroTarea extends AsyncTask<Void, Void, Boolean> {
        TagNFC tag_nfc = new TagNFC();
        Context context;
        Intent intent;
        Integer idViaje;

        public TiroTarea(Context context, Intent intent) {
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
                            if (nfcTag.byteArrayToHexString(myTag.getId()).equals(tagNFC.getUID())) {
                                tag_nfc.setUID(tagNFC.getUID());
                                tag_nfc.setTipo(1);
                                String camion_proyecto = null;
                                try {
                                    camion_proyecto = nfcTag.readSector(null, 0, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                    return false;
                                }
                                if (camion_proyecto.length() == 8) {
                                    tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 4));
                                    tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 4));
                                } else {
                                    tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 5));
                                    tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 5));
                                }
                            }else {
                                mensaje = "¡Error! Utilice el mismo tag.";
                                return false;
                            }

                        } else if (MifareUltralight.class.getName().equals(t)) {
                            nfcUltra = new NFCUltralight(myTag, context);
                            if (nfcUltra.byteArrayToHexString(myTag.getId()).equals(tagNFC.getUID())) {
                                tag_nfc.setUID(tagNFC.getUID());
                                tag_nfc.setTipo(2);
                                String camion_proyecto = null;
                                try {
                                    camion_proyecto = nfcUltra.readDeductiva(null, 4) + nfcUltra.readDeductiva(null, 5) + nfcUltra.readDeductiva(null, 6);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                    return false;
                                }
                                if (camion_proyecto.length() == 8) {
                                    tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 4));
                                    tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 4));
                                } else {
                                    tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 8));
                                    tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 8));
                                }
                            }else {
                                mensaje = "¡Error! Utilice el mismo tag.";
                                return false;
                            }
                        }
                    }
                }
            }
            //// inicia seccion de validaciones y escritura de datos en DB
            TiroLibre tiroLibre = new TiroLibre(context, tag_nfc);
            mensaje = tiroLibre.validarDatosTag();
            if(mensaje == "continuar") { //escribir datos en DB
                String aux = Util.dateFolios();
                String fechaLlegada = Util.getFecha();
                String horaLlegada = Util.getTime();
                tag_nfc.setLatitud_tiro(latitud.toString());
                tag_nfc.setLongitud_tiro(longitud.toString());
                datosVista.put("IdProyecto", tag_nfc.getIdproyecto());
                datosVista.put("IdCamion", tag_nfc.getIdcamion());
                datosVista.put("uidTAG", tagNFC.getUID());
                datosVista.put("Code", Util.folio(aux) + String.valueOf(tag_nfc.getIdcamion()));
                datosVista.put("CodeImagen", Util.getCodeFecha(tagNFC.getIdcamion(), aux));
                datosVista.put("FechaCarga", Util.getFecha());
                datosVista.put("HoraCarga", Util.getTime());
                datosVista.put("FechaLlegada", fechaLlegada);
                datosVista.put("HoraLlegada", horaLlegada);
                datosVista.put("FechaSalida", fechaLlegada);
                datosVista.put("HoraSalida", horaLlegada);
                datosVista.put("latitud_origen","NULL");
                datosVista.put("longitud_origen", "NULL");
                datosVista.put("latitud_tiro", tagNFC.getLatitud_tiro());
                datosVista.put("longitud_tiro", tagNFC.getLongitud_tiro());
                // eliminar datos del TAG...
                if (tagNFC.getTipo() == 1) {
                    try {
                        nfcTag.cleanSector(null, 1);
                        nfcTag.cleanSector(null, 2);
                        nfcTag.cleanSector(null, 3);
                        nfcTag.cleanSector(null, 4);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                        return false;
                    }
                }
                if (tagNFC.getTipo() == 2) {
                    try {
                        nfcUltra.cleanTag(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                        return false;
                    }
                }
                // enviar datos a la BD
                if (!tiroLibre.guardarDatosDB(datosVista)) {
                    mensaje = "Error al guardar en Base de Datos";
                    return false;
                }
                idViaje = tiroLibre.idViaje;
                tiroLibre.coordenadas(datosVista.getAsString("IMEI"), datosVista.getAsString("Code"), latitud, longitud);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean registro) {
            super.onPostExecute(registro);
            if (registro) {
                WriteModeOff();
                destinoSuccess.putExtra("idViaje", idViaje);
                destinoSuccess.putExtra("LIST", 0);
                destinoSuccess.putExtra("code", datosVista.getAsString("Code"));
                startActivity(destinoSuccess);
            } else {
                alert(mensaje);
            }
        }
    }

    public void alert(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(TiroUnicoActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {

                    }
                }).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        new TiroTarea(this,  intent).execute();
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
            new AlertDialog.Builder(TiroUnicoActivity.this)
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                                    progressDialogSync = ProgressDialog.show(TiroUnicoActivity.this, "Sincronizando datos", "Por favor espere...", true);
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
            Intent listActivity = new Intent(this, ListaViajesActivity.class);
            startActivity(listActivity);

        } else if (id == R.id.nav_desc) {

            Intent descarga = new Intent(this, DescargaActivity.class);
            startActivity(descarga);

        }  else if (id == R.id.nav_logout) {
            if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                new AlertDialog.Builder(TiroUnicoActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(TiroUnicoActivity.this, "Sincronizando datos", "Por favor espere...", true);
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

    private Boolean foliosSinCeros(String folio){
        if(folio.replace("0","").trim().equals("")){
            return true;
        }
        return false;
    }

}
