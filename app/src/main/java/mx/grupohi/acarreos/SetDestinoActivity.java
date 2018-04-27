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
    private ProgressDialog progressDialogSync;
    private TextInputLayout mina,
            seg;
    private TextView textmina,
            textseg;

    private Integer idTiro;
    private Integer idRuta;
    String mensaje = "";
    ContentValues datosVista;
    Integer id;

    private HashMap<String, String> spinnerTirosMap;
    private HashMap<String, String> spinnerRutasMap;

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
        id = getIntent().getIntExtra("IdViaje", 0);
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
        Crashlytics.setUserEmail("IMEI:"+IMEI);
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
                    datosVista.put("Estatus", "4");
                } else {
                    datosVista.put("Estatus", "3");
                }
                Integer volumen = volumenMenor(Integer.valueOf(datosVista.getAsString("deductiva_origen")), Integer.valueOf(datosVista.getAsString("deductiva_entrada")), Integer.valueOf(deductiva.getText().toString()));
                datosVista.put("cubicacion", String.valueOf(volumen));
                DestinoTiro destinoTiro = new DestinoTiro(context, tagNFC);
                idViaje = destinoTiro.viajeIncompleto(tagNFC.getIdcamion());
                if(idViaje == null) {
                    while (!destinoTiro.guardarDatosDB(datosVista)) {
                        mensaje = "Error al guardar en Base de Datos";
                    }
                    idViaje = destinoTiro.idViaje;
                    destinoTiro.coordenadas(datosVista.getAsString("IMEI"), datosVista.getAsString("Code"), latitud, longitud);
                }

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
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean registro) {
            super.onPostExecute(registro);
            WriteModeOff();
            if (registro) {
                boolean resp = DestinoTiro.cambioEstatus(context, idViaje);
                if(resp == true) {
                    destinoSuccess.putExtra("idViaje", idViaje);
                    destinoSuccess.putExtra("LIST", 0);
                    startActivity(destinoSuccess);
                }else{
                    alert("Error al cambiar el estatus");
                }
            } else {
                alert(mensaje);
            }
        }
    }

    public void alert(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SetDestinoActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {

                    }
                }).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        new DestinoTarea(this,  intent).execute();
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
