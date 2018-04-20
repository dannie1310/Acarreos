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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.ActionMenuItemView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import mx.grupohi.acarreos.Destino.DestinoTiro;
import mx.grupohi.acarreos.TiposTag.TagNFC;

public class SetDestinoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    Usuario usuario;
    Tiro tiro;
    Ruta ruta;
    Camion c;
    TagNFC tagNFC;

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
    Intent destinoSuccess;

    //Referencias UI
    private Spinner tirosSpinner;
    private Spinner rutasSpinner;
    private Button escribirDestinoButton;
    private LinearLayout mainLayout;
    private ImageView nfcImage;
    private FloatingActionButton fabCancel;
    private TextView mensajeTextView;
    private EditText observacionesTextView;
    private EditText deductiva;
    private Snackbar snackbar;
    private ProgressDialog progressDialogSync;
    private TextInputLayout mina,
            seg;
    private TextView textmina,
            textseg;

    private Integer idTiro;
    private Integer idRuta;
    private Integer idMotivo;
    Integer idCamion;
    Integer idProyecto;
    String mensaje = "";
    ContentValues datosVista;


    private Integer tipo_suministro;
    private HashMap<String, String> spinnerTirosMap;
    private HashMap<String, String> spinnerRutasMap;
    int error_eliminar = 0;// error de borrado de Tag (si es 0 no se ha realizado ninguna lectura, si es 1 no elimino correctamente)

    String camionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_destino);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        destinoSuccess = new Intent(this, SuccessDestinoActivity.class);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        tagNFC = (TagNFC) getIntent().getSerializableExtra("datos");
        c = new Camion(getApplicationContext());
        c = c.find(tagNFC.getIdcamion());
        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        ruta = new Ruta(this);
        tiro = new Tiro(this);
        datosVista = new ContentValues();

        gps = new GPSTracker(SetDestinoActivity.this);
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

        tirosSpinner = (Spinner) findViewById(R.id.spinnerTiros);
        rutasSpinner = (Spinner) findViewById(R.id.spinnerRutas);
        escribirDestinoButton = (Button) findViewById(R.id.buttonEscribir);
        mainLayout = (LinearLayout) findViewById(R.id.MainLayout);
        nfcImage = (ImageView) findViewById(R.id.imageViewNFC);
        mensajeTextView = (TextView) findViewById(R.id.textViewMensaje);
        observacionesTextView = (EditText) findViewById(R.id.textObservaciones);
        fabCancel = (FloatingActionButton) findViewById(R.id.fabCancel);
        deductiva = (EditText) findViewById(R.id.textDeductiva);
        mina = (TextInputLayout) findViewById(R.id.textomina);
        seg = (TextInputLayout) findViewById(R.id.seg);
        textmina = (TextView) findViewById(R.id.vale_mina);
        textseg = (TextView) findViewById(R.id.seguimiento);
        mensajeTextView.setVisibility(View.INVISIBLE);
        nfcImage.setVisibility(View.INVISIBLE);
        fabCancel.setVisibility(View.INVISIBLE);

        final ArrayList<String> descripcionesTiros = tiro.getArrayListDescripcionesTiro();
        final ArrayList <String> idsTiros = tiro.getArrayListIdTiro();

        final String[] spinnerTirosArray = new String[idsTiros.size()];
        spinnerTirosMap = new HashMap<>();

        for (int i = 0; i < idsTiros.size(); i++) {
            spinnerTirosMap.put(descripcionesTiros.get(i), idsTiros.get(i));
            spinnerTirosArray[i] = descripcionesTiros.get(i);
        }

        final ArrayAdapter<String> arrayAdapterTiros = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item, spinnerTirosArray);
        arrayAdapterTiros.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        tirosSpinner.setAdapter(arrayAdapterTiros);

        tirosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String descripcion = String.valueOf(parent.getItemAtPosition(position));
                idTiro = Integer.valueOf(spinnerTirosMap.get(descripcion));

                final ArrayList<String> descripcionesRutas = ruta.getArrayListDescripciones(Integer.valueOf(tagNFC.getIdorigen()), idTiro);
                final ArrayList <String> idsRutas = ruta.getArrayListId(Integer.valueOf(tagNFC.getIdorigen()), idTiro);

                final String[] spinnerRutasArray = new String[idsRutas.size()];
                spinnerRutasMap = new HashMap<>();

                for (int i = 0; i < idsRutas.size(); i++) {
                    spinnerRutasMap.put(descripcionesRutas.get(i), idsRutas.get(i));
                    spinnerRutasArray[i] = descripcionesRutas.get(i);
                }

                final ArrayAdapter<String> arrayAdapterRutas = new ArrayAdapter<>(SetDestinoActivity.this, R.layout.support_simple_spinner_dropdown_item, spinnerRutasArray);
                arrayAdapterRutas.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                rutasSpinner.setAdapter(arrayAdapterRutas);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        Motivo motivo = new Motivo(getApplicationContext());
        final ArrayList<String> descripcionesMotivos = motivo.getArrayListDescripciones();

        rutasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String descripcion = String.valueOf(parent.getItemAtPosition(position));
                idRuta = Integer.valueOf(spinnerRutasMap.get(descripcion));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        escribirDestinoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(!validarCampos()){
                   alert(mensaje);
               }
                else {
                    checkNfcEnabled();
                    WriteModeOn();
                }
            }
        });

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

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteModeOff();
            }
        });
    }

    private Boolean validarCampos(){
        // validar el Spinner idTiro
        if (idTiro == 0) {
            mensaje = "Por favor seleccione el Tiro de la lista";
            tirosSpinner.requestFocus();
            return false;
        }

        // Validar una ruta seleccionada del Spinner
        if(idRuta == 0 &&  Ruta.getCount(getApplicationContext(),Integer.valueOf(tagNFC.getIdorigen()),idTiro) != 0) {
            mensaje = "Por favor seleccione la Ruta de la lista";
            rutasSpinner.requestFocus();
            return false;
        }

        // Ingresar folio de mina obligatorio
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

        // Ingresar folio seguimiento obligatorio
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
        // escribir volumen
        if(deductiva.getText().toString().equals("")){
            mensaje = "Por favor ingrese el volumen";
            deductiva.requestFocus();
            return false;
        }
        if(foliosSinCeros(deductiva.getText().toString())){
            mensaje = "El volumen no puede ser cero.";
            deductiva.requestFocus();
            return false;
        }
        if(c.capacidad != 0 && c.capacidad!= null && Integer.valueOf(deductiva.getText().toString()) > c.capacidad) {
            mensaje = "El volumen es mayor a la capacidad del camión.";
            deductiva.requestFocus();
            return false;
        }

        /// asignacion de valores
        datosVista.put("IdMaterial", tagNFC.getIdmaterial());
        datosVista.put("IdOrigen", tagNFC.getIdorigen());
        datosVista.put("IdTiro", idTiro);
        datosVista.put("Ruta", idRuta);
        datosVista.put("deductiva", deductiva.getText().toString());
        datosVista.put("idmotivo", tagNFC.getIdmotivo());
        datosVista.put("folio_mina", textmina.getText().toString());
        datosVista.put("folio_seguimiento", textseg.getText().toString());
        datosVista.put("Observaciones", observacionesTextView.getText().toString());
        datosVista.put("Creo", String.valueOf(usuario.getId()));
        datosVista.put("IMEI", IMEI);
        datosVista.put("IdProyecto", tagNFC.getIdproyecto());
        datosVista.put("IdCamion", tagNFC.getIdcamion());
        datosVista.put("deductiva_origen", tagNFC.getVolumen());
        datosVista.put("idmotivo_origen", tagNFC.getIdmotivo());
        if (tagNFC.getVolumen_entrada().trim().equals("") || tagNFC.getVolumen_entrada() == null) { //deductiva checador de entrada
            datosVista.put("deductiva_entrada", 0);
            datosVista.put("idmotivo_entrada", 0);
        } else {
            datosVista.put("deductiva_entrada", tagNFC.getVolumen_entrada());
            datosVista.put("idmotivo_entrada", tagNFC.getIdmotivo());
        }
        RandomString r = new RandomString(10);
        datosVista.put("FolioRandom", r.nextString().toUpperCase());
        datosVista.put("primerToque", tagNFC.getUsuario());
        datosVista.put("tipoEsquema", usuario.getTipoEsquema());
        datosVista.put("numImpresion", 0);
        datosVista.put("idperfil", usuario.tipo_permiso);
        datosVista.put("tipoViaje", tagNFC.getTipo_viaje());

        return true;
    }

    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);

        escribirDestinoButton.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.INVISIBLE);

        fabCancel.setVisibility(View.VISIBLE);
        nfcImage.setVisibility(View.VISIBLE);
        mensajeTextView.setVisibility(View.VISIBLE);
    }

    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);

        escribirDestinoButton.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.VISIBLE);

        fabCancel.setVisibility(View.GONE);
        nfcImage.setVisibility(View.GONE);
        mensajeTextView.setVisibility(View.GONE);
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

        int id = item.getItemId();

        if (id == R.id.nav_home) {

            Intent mainActivity;
            Integer tipo = usuario.getTipo_permiso();
            if(tipo == 0){
                mainActivity = new Intent(getApplicationContext(), SetOrigenActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivity);
            }else if(tipo == 1){
                mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivity);
            }
        } else if (id == R.id.nav_desc) {
            Intent descarga = new Intent(this, DescargaActivity.class);
            startActivity(descarga);
        } else if (id == R.id.nav_sync) {
            new AlertDialog.Builder(SetDestinoActivity.this)
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                                    progressDialogSync = ProgressDialog.show(SetDestinoActivity.this, "Sincronizando datos", "Por favor espere...", true);
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
            Intent navList = new Intent(this, ListaViajesActivity.class);
            startActivity(navList);
        } else if (id == R.id.nav_pair_on) {

        } else if (id == R.id.nav_pair_off) {

        } else if (id == R.id.nav_logout) {
            if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                new AlertDialog.Builder(SetDestinoActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(SetDestinoActivity.this, "Sincronizando datos", "Por favor espere...", true);
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

    private void checkNfcEnabled() {
        Boolean nfcEnabled = nfcAdapter.isEnabled();
        if (!nfcEnabled) {
            new android.app.AlertDialog.Builder(SetDestinoActivity.this)
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

    class DestinoTarea extends AsyncTask<Void, Void, Boolean> {
        Context context;
        Intent intent;
        Integer idViaje;
        Double latitud = gps.getLatitude();
        Double longitud = gps.getLongitude();


        public DestinoTarea(Context context, Intent intent) {
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
                            if(nfcTag.byteArrayToHexString(myTag.getId()).equals(tagNFC.getUID())){
                                mensaje = "continuar";
                            }else {
                                mensaje = "¡Error! Utilice el mismo tag.";
                                return false;
                            }

                        } else if (MifareUltralight.class.getName().equals(t)) {
                            nfcUltra = new NFCUltralight(myTag, context);
                            if(nfcUltra.byteArrayToHexString(myTag.getId()).equals(tagNFC.getUID())){
                                mensaje = "continuar";
                            }else {
                                mensaje = "¡Error! Utilice el mismo tag.";
                                return false;
                            }
                        }
                    }
                }
            }
            //// inicia seccion de validaciones y escritura de datos en DB
            if(mensaje == "continuar") {
                String aux = Util.dateFolios();
                String code = Util.folio(aux) + String.valueOf(tagNFC.getIdcamion());
                String fechaLlegada = Util.getFecha();
                String horaLlegada = Util.getTime();
                String fechaOrigen = Util.getFecha(tagNFC.getFecha());
                String horaOrigen = Util.getTime(tagNFC.getFecha());
                String fecha;

                datosVista.put("Code", code);
                datosVista.put("CodeImagen", Util.getCodeFecha(tagNFC.getIdcamion(), aux));
                datosVista.put("uidTAG", tagNFC.getUID());
                datosVista.put("FechaCarga", Util.getFecha());
                datosVista.put("HoraCarga", Util.getTime());
                datosVista.put("FechaLlegada", fechaLlegada);
                datosVista.put("HoraLlegada", horaLlegada);
                if (tagNFC.getFecha().replace(" ", "").isEmpty()) {
                    fecha = Util.getFechaDisminucion(fechaLlegada + " " + horaLlegada);
                    datosVista.put("FechaSalida", Util.getFecha(fecha));
                    datosVista.put("HoraSalida", Util.getTime(fecha));
                } else {
                    if (horaOrigen == null || fechaOrigen == null) {
                        fecha = Util.getFechaDisminucion(fechaLlegada + " " + horaLlegada);
                        datosVista.put("FechaSalida", Util.getFecha(fecha));
                        datosVista.put("HoraSalida", Util.getTime(fecha));
                    } else {
                        fecha = fechaOrigen + " " + horaOrigen;
                        datosVista.put("FechaSalida", fechaOrigen);
                        datosVista.put("HoraSalida", horaOrigen);
                    }
                }
                if (Util.getFechaImprocedente(fecha, fechaLlegada + " " + horaLlegada)) {
                    datosVista.put("Estatus", "2");
                } else {
                    datosVista.put("Estatus", "1");
                }
                Integer volumen = volumenMenor(Integer.valueOf(datosVista.getAsString("deductiva_origen")), Integer.valueOf(datosVista.getAsString("deductiva_entrada")), Integer.valueOf(deductiva.getText().toString()));
                datosVista.put("cubicacion", String.valueOf(volumen));

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
                DestinoTiro destinoTiro = new DestinoTiro(context, tagNFC);
                while (!destinoTiro.guardarDatosDB(datosVista)) {
                    mensaje = "Error al guardar en Base de Datos";
                }
                idViaje = destinoTiro.idViaje;
                destinoTiro.coordenadas(datosVista.getAsString("IMEI"), datosVista.getAsString("Code"), latitud, longitud);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean registro) {
            super.onPostExecute(registro);
            WriteModeOff();
            if (registro) {
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
        AlertDialog.Builder dialog = new AlertDialog.Builder(SetDestinoActivity.this);

        dialog.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {

                    }
                }).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {


        new DestinoTarea(this,  intent).execute();

        /*int contador=0;
        JSONObject json = new JSONObject();
        String tagInfo ="";
        String viajes = "";
        String tagOrigen = "";
        String fechaString = "";
        String idUsuario = null;
        Boolean limpiarorigen = false;
        Boolean limpiarusuario = false;
        boolean continuar = false;
        String UID ="";

        String deductiva_origen = "";
        String idmotivo_origen = "";
        String deductiva_entrada = "";
        String idmotivo_entrada = "";
        String tipoviaje="";
        int tipo = 0;
        Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);;

        if (writeMode) {
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);


                String[] techs = myTag.getTechList();
                for (String t : techs) {
                    if (MifareClassic.class.getName().equals(t)) {
                        nfcTag = new NFCTag(myTag, this);
                        UID = nfcTag.byteArrayToHexString(myTag.getId());
                        tipo = 1;
                    } else if (MifareUltralight.class.getName().equals(t)) {
                        nfcUltra = new NFCUltralight(myTag, this);
                        UID = nfcUltra.byteArrayToHexString(myTag.getId());
                        tipo = 2;
                    }
                }
                if (error_eliminar == 0) {
                    if (UID.equals(getIntent().getStringExtra("UID"))) {

                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();
                        if (tipo == 1) {

                            try {
                                tagInfo = nfcTag.readSector(myTag, 0, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                tagOrigen = nfcTag.readSector(myTag, 1, 4);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                fechaString = nfcTag.readSector(myTag, 1, 5);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                idUsuario = nfcTag.readSector(myTag, 1, 6);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                deductiva_origen = nfcTag.readSector(myTag, 3, 12);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                idmotivo_origen = nfcTag.readSector(myTag, 3, 13);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                tipoviaje = nfcTag.readSector(myTag, 2, 9);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                deductiva_entrada = nfcTag.readSector(myTag, 4, 16); //validar
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                idmotivo_entrada = nfcTag.readSector(myTag, 4, 17);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            tagInfo = tagInfo.replace(" ", "");
                            idCamion = null;
                            idProyecto = null;
                            if (tagInfo.length() == 8) {
                                idCamion = Util.getIdCamion(tagInfo, 4);
                                idProyecto = Util.getIdProyecto(tagInfo, 4);
                            } else if (tagInfo.length() == 9) {
                                idCamion = Util.getIdCamion(tagInfo, 5);
                                idProyecto = Util.getIdProyecto(tagInfo, 5);
                            } else if (tagInfo.length() == 12) {
                                idCamion = Util.getIdCamion(tagInfo, 8);
                                idProyecto = Util.getIdProyecto(tagInfo, 8);
                            }

                            TagModel datosTagCamion = new TagModel(getApplicationContext());
                            datosTagCamion = datosTagCamion.find(UID, idCamion, idProyecto);

                            if (datosTagCamion.estatus == 1) {
                                if (tagInfo != null && tagOrigen != null && fechaString != null && idUsuario != null) {
                                    continuar = true;
                                } else {
                                    snackbar = Snackbar.make(findViewById(R.id.content_set_destino), getString(R.string.error_tag_comunicacion), Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(Color.RED);
                                    snackbar.show();
                                }
                            } else {
                                continuar = false;
                                Toast.makeText(getApplicationContext(), "El camión " + datosTagCamion.economico + " se encuentra inactivo. Por favor contacta al encargado.", Toast.LENGTH_LONG).show();
                            }
                        }
                        if (tipo == 2) {
                            try {
                                tagInfo = nfcUltra.readPage(myTag, 4) + nfcUltra.readPage(myTag, 5) + nfcUltra.readPage(myTag, 6);
                                tagInfo = tagInfo.replace(" ", "").replace("null", "");
                                tagOrigen = nfcUltra.readPage(myTag, 7) + nfcUltra.readPage(myTag, 8);
                                fechaString = nfcUltra.readPage(myTag, 9) + nfcUltra.readPage(myTag, 10) + nfcUltra.readPage(myTag, 11) + nfcUltra.readPage(myTag, 12).substring(0, 2);
                                idUsuario = nfcUltra.readUsuario(myTag, 13) + nfcUltra.readUsuario(myTag, 14);
                                deductiva_origen = nfcUltra.readDeductiva(myTag, 16);
                                idmotivo_origen = nfcUltra.readDeductiva(myTag, 17);
                                deductiva_entrada = nfcUltra.readDeductiva(myTag, 19);
                                idmotivo_entrada = nfcUltra.readDeductiva(myTag, 20);
                                tipoviaje = nfcUltra.readDeductiva(myTag, 15);

                                tagInfo = tagInfo.replace(" ", "");
                                idCamion = null;
                                idProyecto = null;
                                if (tagInfo.length() == 8) {
                                    idCamion = Util.getIdCamion(tagInfo, 4);
                                    idProyecto = Util.getIdProyecto(tagInfo, 4);
                                } else if (tagInfo.length() == 9) {
                                    idCamion = Util.getIdCamion(tagInfo, 5);
                                    idProyecto = Util.getIdProyecto(tagInfo, 5);
                                } else if (tagInfo.length() == 12) {
                                    idCamion = Util.getIdCamion(tagInfo, 8);
                                    idProyecto = Util.getIdProyecto(tagInfo, 8);
                                }

                                TagModel datosTagCamion = new TagModel(getApplicationContext());
                                datosTagCamion = datosTagCamion.find(UID, idCamion, idProyecto);

                                if (datosTagCamion.estatus == 1) {
                                    if (tagInfo != null && tagOrigen != null && fechaString != null && idUsuario != null) {
                                        continuar = true;
                                    } else {
                                        snackbar = Snackbar.make(findViewById(R.id.content_set_destino), getString(R.string.error_tag_comunicacion), Snackbar.LENGTH_SHORT);
                                        View snackBarView = snackbar.getView();
                                        snackBarView.setBackgroundColor(Color.RED);
                                        snackbar.show();
                                    }
                                } else {
                                    continuar = false;
                                    Toast.makeText(getApplicationContext(), "El camión " + datosTagCamion.economico + " se encuentra inactivo. Por favor contacta al encargado.", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        snackbar = Snackbar.make(findViewById(R.id.content_set_destino), "Por favor utiliza el TAG correcto", Snackbar.LENGTH_LONG);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(Color.RED);
                        snackbar.show();
                    }
                }
            }

            if (continuar) {
                Integer idOrigen = Util.getIdOrigen(tagOrigen);
                Integer idMaterial = Util.getIdMaterial(tagOrigen);
                Viaje viaje = null;
                Camion c = new Camion(getApplicationContext());
                String aux = "";

                ContentValues cv = new ContentValues();
                aux = Util.dateFolios();
                String code = Util.folio(aux) + String.valueOf(idCamion);

                cv.put("FechaCarga", Util.getFecha());
                cv.put("HoraCarga", Util.getTime());
                cv.put("IdProyecto", idProyecto);
                cv.put("IdCamion", idCamion);
                cv.put("IdOrigen", idOrigen);
                cv.put("IdTiro", idTiro);
                String fechaLlegada = Util.getFecha();
                String horaLlegada = Util.getTime();
                cv.put("FechaLlegada", fechaLlegada);
                cv.put("HoraLlegada", horaLlegada);
                String fechaOrigen = Util.getFecha(fechaString);
                String horaOrigen = Util.getTime(fechaString);
                String fechaSal;
                if (fechaString.replace(" ", "").isEmpty()) {
                    String fecha = Util.getFechaDisminucion(fechaLlegada + " " + horaLlegada);
                    fechaSal = fecha;
                    cv.put("FechaSalida", Util.getFecha(fecha));
                    cv.put("HoraSalida", Util.getTime(fecha));
                } else {
                    if (horaOrigen == null || fechaOrigen == null) {
                        String fecha = Util.getFechaDisminucion(fechaLlegada + " " + horaLlegada);
                        fechaSal = fecha;
                        cv.put("FechaSalida", Util.getFecha(fecha));
                        cv.put("HoraSalida", Util.getTime(fecha));
                    } else {
                        fechaSal = fechaOrigen +" "+horaOrigen;
                        cv.put("FechaSalida", fechaOrigen);
                        cv.put("HoraSalida", horaOrigen);
                    }

                }

                cv.put("IdMaterial", idMaterial);
                cv.put("Observaciones", observacionesTextView.getText().toString());
                cv.put("Creo", usuario.getId());
                Boolean dat = Util.getFechaImprocedente(fechaSal, fechaLlegada+" "+horaLlegada);
                if(dat == true){
                    cv.put("Estatus", "2");
                }else{
                    cv.put("Estatus", "1");
                }

                cv.put("Ruta", idRuta);

                cv.put("Code", code);
                cv.put("uidTAG", UID);
                cv.put("IMEI", IMEI);
                cv.put("CodeImagen", Util.getCodeFecha(idCamion, aux));

                if (deductiva_origen.toString().trim().equals("") || deductiva_origen == null) { //deductiva checador de origen
                    cv.put("deductiva_origen", 0);
                    cv.put("idmotivo_origen", 0);
                } else {
                    cv.put("deductiva_origen", deductiva_origen.replace(" ", ""));
                    cv.put("idmotivo_origen", 0);
                }
                Integer datoViaje = 0;
                if (!tipoviaje.toString().trim().equals("")) {
                    datoViaje = Integer.valueOf(tipoviaje.substring(0, 1));
                }
                if (deductiva_entrada.toString().trim().equals("") || deductiva_entrada == null) { //deductiva checador de entrada
                    cv.put("deductiva_entrada", 0);
                    cv.put("idmotivo_entrada", 0);
                } else if (datoViaje == 1) {
                    cv.put("deductiva_entrada", deductiva_entrada.replace(" ", ""));
                    cv.put("idmotivo_entrada", 0);
                }

                if (deductiva.getText().toString().equals("") || deductiva.getText().toString() == null) { // deductiva checador de salida, fin del viaje.
                    cv.put("deductiva", 0);
                    cv.put("idmotivo", 0);
                } else {
                    cv.put("deductiva", deductiva.getText().toString());
                    cv.put("idmotivo", 0);
                }
                RandomString r = new RandomString(10);
                cv.put("FolioRandom", r.nextString().toUpperCase());
                cv.put("primerToque", idUsuario.replace(" ", ""));
               // c = c.find(idCamion);
                cv.put("idmotivo", 0);
                Integer origen = 0;
                Integer entrada = 0;
                Integer salida = 0;
                if(!deductiva_origen.replace(" ", "").equals("")){
                    origen = Integer.valueOf(deductiva_origen.replace(" ", ""));
                }
                if(!deductiva_entrada.replace(" ", "").equals("")){
                    entrada = Integer.valueOf(deductiva_entrada.replace(" ", ""));
                }
                if(!deductiva.getText().toString().equals("")){
                    salida = Integer.valueOf(deductiva.getText().toString());
                }
                Integer volumen = volumenMenor(origen,entrada,salida);
                cv.put("cubicacion", String.valueOf(volumen));
                cv.put("tipoEsquema", usuario.getTipoEsquema());
                cv.put("numImpresion", 0);
                cv.put("idperfil", usuario.tipo_permiso);
                if (textseg.getText().toString().equals("")) {
                    cv.put("folio_seguimiento", 0);
                } else {
                    cv.put("folio_seguimiento", textseg.getText().toString());
                }
                if (textmina.getText().toString().equals("")) {
                    cv.put("folio_mina", 0);
                } else {
                    cv.put("folio_mina", textmina.getText().toString());
                }
                if (!tipoviaje.toString().trim().equals("")) {
                    cv.put("tipoViaje", tipoviaje.substring(0, 1));
                } else {
                    cv.put("tipoViaje", "0");
                }

                viaje = new Viaje(this);
                viaje.create(cv);

                cv.clear();
                cv.put("IMEI", IMEI);
                cv.put("idevento", 3);
                cv.put("latitud", latitude);
                cv.put("longitud", longitude);
                cv.put("fecha_hora", Util.timeStamp());
                cv.put("code", aux);
                Coordenada coordenada = new Coordenada(this);
                coordenada.create(cv, SetDestinoActivity.this);

                destinoSuccess.putExtra("idViaje", viaje.idViaje);
                destinoSuccess.putExtra("LIST", 0);
                destinoSuccess.putExtra("code", aux);

                if (Viaje.findCode(code) != null) {
                    if (tipo == 1) {
                        try {
                            limpiarorigen = nfcTag.cleanSector(myTag, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            nfcTag.cleanSector(myTag, 2);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            nfcTag.cleanSector(myTag, 3);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            nfcTag.cleanSector(myTag, 4);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (!limpiarorigen) {
                            error_eliminar = 1;
                        } else {
                            error_eliminar = 2;
                        }
                    }
                    if (tipo == 2) {
                        Boolean limpiar = null;
                        try {
                            limpiar = nfcUltra.cleanTag(myTag);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!limpiar) {
                            error_eliminar = 1;
                        } else {
                            error_eliminar = 2;
                        }
                    }
              /*  else if (error_eliminar == 1) {
                    if (tipo == 1) {
                        limpiarorigen = nfcTag.cleanSector(myTag, 1);
                        nfcTag.cleanSector(myTag, 2);
                        nfcTag.cleanSector(myTag, 3);
                        nfcTag.cleanSector(myTag, 4);

                        if (limpiarorigen) {
                            error_eliminar = 2;
                        }
                    } else if (tipo == 2) {
                        Boolean limpiar = nfcUltra.cleanTag(myTag);
                        if (limpiar) {
                            error_eliminar = 2;
                        }
                    }
                }*/
            /*    } else {
                    snackbar = Snackbar.make(findViewById(R.id.content_set_destino), getString(R.string.error_guardado), Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(Color.RED);
                    snackbar.show();
                }
            }

        }

        if(error_eliminar == 2){
            startActivity(destinoSuccess);
        }else{
            //getString(R.string.error_tag_comunicacion)
            snackbar = Snackbar.make(findViewById(R.id.content_set_destino), getString(R.string.error_tag_comunicacion), Snackbar.LENGTH_SHORT);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(Color.RED);
            snackbar.show();
        }

        */
    }

    public static String getCode(String idcamion) {
        String mensaje = "";
        int ceros = 0;

        if(idcamion.length() < 5){
            ceros = 5 - idcamion.length();
            for ( int i=0; i< ceros; i++){
                mensaje += "0";
            }
            mensaje +=idcamion;

        }
        else{
            mensaje +=idcamion;
        }

        return mensaje;
    }


    public Integer menor(Integer x, Integer y){
        Integer resp = 0;
        if(x!=0 && y!=0) {
            if (x < y) {
                resp = x;
            } else {
                resp = y;
            }
        }else if(x==0){
            resp = y;
        }else if(y==0){
            resp = x;
        }
        return resp;
    }

    public  Integer volumenMenor(Integer o, Integer e, Integer d){
        Integer resp = 0;
        while(resp == 0){
            resp = menor(o, e);
            resp = menor(resp, d);
        }
        return resp;
    }

    private Boolean foliosSinCeros(String folio){
        if(folio.replace("0","").trim().equals("")){
            return true;
        }
        return false;
    }
}
