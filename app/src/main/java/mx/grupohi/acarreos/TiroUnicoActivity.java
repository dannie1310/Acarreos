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
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TiroUnicoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Objetos
    private Usuario usuario;
    private Material material;
    private Origen origen;
    private Camion camion;
    private Ruta ruta;

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
    private TextView textViewMotivo;
    private Spinner  spinnerMotivo;
    private  Spinner rutasSpinner;
    private  EditText observaciones;
    private  HashMap<String, String> spinnerMotivosMap;
    private  HashMap<String, String> spinnerRutasMap;
    private TextView mensajeTextView;

    private ProgressDialog progressDialogSync;

    private Snackbar snackbar;

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
    private Integer idMotivo;
    private String UID;
    private Integer idcamion;
    private Integer idruta;
    private Integer error_eliminar = 0;// error de borrado de Tag (si es 0 no se ha realizado ninguna lectura, si es 1 no elimino correctamente)


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
        ruta = new Ruta(getApplicationContext());

        UID = getIntent().getStringExtra("UID");
        idcamion = Integer.valueOf(getIntent().getStringExtra("camion"));
        camion = camion.find(idcamion);
        destinoSuccess = new Intent(this, SuccessDestinoActivity.class);
        TelephonyManager phneMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = phneMgr.getDeviceId();

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

        tagAlertTextView.setVisibility(View.INVISIBLE);
        nfcImage.setVisibility(View.INVISIBLE);
        fabCancel.setVisibility(View.INVISIBLE);
        text_origen = (TextView) findViewById(R.id.textView5);
        materialesSpinner = (Spinner) findViewById(R.id.spinnerMateriales);
        origenesSpinner = (Spinner) findViewById(R.id.spinnerOrigenes);
        mensajeTextView = (TextView) findViewById(R.id.textViewMensaje);
        rutasSpinner = (Spinner) findViewById(R.id.spinnerRutass);
        textDeductiva = (EditText) findViewById(R.id.textDeductiva);
        textViewMotivo = (TextView) findViewById(R.id.textViewMotivo);
        spinnerMotivo = (Spinner) findViewById(R.id.spinnerMotivo);
        observaciones = (EditText) findViewById(R.id.textViewObservaciones);
        mensajeTextView.setVisibility(View.INVISIBLE);

        textDeductiva.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             textViewMotivo.setVisibility(View.VISIBLE);
                                             spinnerMotivo.setVisibility(View.VISIBLE);
                                         }
                                     }

        );


        final ArrayList<String> descripcionesOrigenes = origen.getArrayListDescripcionesOrigenTiro(usuario.idtiro);
        final ArrayList<String> idsOrigenes = origen.getArrayListId(usuario.idtiro);

        final String[] spinnerOrigenesArray = new String[idsOrigenes.size()];
        final HashMap<String, String> spinnerOrigenesMap = new HashMap<>();

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
                final ArrayList <String> idsRutas = ruta.getArrayListId(idOrigen, usuario.idtiro);

                final String[] spinnerRutasArray = new String[idsRutas.size()];
                final HashMap<String, String> spinnerRutasMap = new HashMap<>();

                for (int i = 0; i < idsRutas.size(); i++) {
                    spinnerRutasMap.put(descripcionesRutas.get(i), idsRutas.get(i));
                    spinnerRutasArray[i] = descripcionesRutas.get(i);
                }

                final ArrayAdapter<String> arrayAdapterRutas = new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, spinnerRutasArray);
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

        Motivo motivo = new Motivo(getApplicationContext());
        final ArrayList<String> descripcionesMotivos = motivo.getArrayListDescripciones();
        final ArrayList <String> idsMotivos = motivo.getArrayListId();

        final String[] spinnerMotivosA = new String[idsMotivos.size()];
        spinnerMotivosMap = new HashMap<>();

        for (int i = 0; i < idsMotivos.size(); i++) {
            spinnerMotivosMap.put(descripcionesMotivos.get(i), idsMotivos.get(i));
            spinnerMotivosA[i] = descripcionesMotivos.get(i);
        }
        final ArrayAdapter<String> arrayAdapterMotivos = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item, spinnerMotivosA);
        arrayAdapterMotivos.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerMotivo.setAdapter(arrayAdapterMotivos);

        spinnerMotivo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String Mo = String.valueOf(parent.getItemAtPosition(position));
                idMotivo = Integer.valueOf(spinnerMotivosMap.get(Mo));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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


        escribirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (idMaterial == 0) {
//                    Toast.makeText(getApplicationContext(), "Por favor seleccione un Material de la lista", Toast.LENGTH_LONG).show();
//                    materialesSpinner.requestFocus();
//                } else if (idOrigen == 0) {
//                    Toast.makeText(getApplicationContext(), "Por favor seleccione un Origen de la lista", Toast.LENGTH_LONG).show();
//                    origenesSpinner.requestFocus();
//                } else if (camion.capacidad != 0 && camion.capacidad != null && !textDeductiva.getText().toString().equals("") && Integer.valueOf(textDeductiva.getText().toString()) != 0 && Integer.valueOf(textDeductiva.getText().toString()) >= camion.capacidad) {
//                    Toast.makeText(getApplicationContext(), R.string.error_deductiva, Toast.LENGTH_LONG).show();
//                } else if ((textDeductiva.getText().toString().equals("") == false) && idMotivo == 0) {
//                    Toast.makeText(getApplicationContext(), "Por favor seleccione un motivo", Toast.LENGTH_SHORT).show();
//                } else {
                    if (textDeductiva.getText().toString().equals("") || textDeductiva.getText().toString().equals("0")) {
                        spinnerMotivo.setSelection(0);
                        textViewMotivo.setVisibility(View.GONE);
                        spinnerMotivo.setVisibility(View.GONE);
                    } else {
                        checkNfcEnabled();
                        WriteModeOn();
                    //}
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

                        if (tvp != null) {
                            tvp.setText(usuario.descripcionBaseDatos);
                        }
                        if (tvu != null) {
                            tvu.setText(usuario.nombre);
                        }
                        if (tpe != null) {
                            if (usuario.origen_name == "0") {
                                tpe.setText(usuario.tiro_name);
                            } else if (usuario.tiro_name == "0") {
                                tpe.setText(usuario.origen_name);
                            }
                        }
                        if (tvv != null) {
                            tvv.setText(getString(R.string.app_name) + "     " + "Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                        }
                    }
                }
            });
    }


    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);

        escribirButton.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.INVISIBLE);

        fabCancel.setVisibility(View.VISIBLE);
        nfcImage.setVisibility(View.VISIBLE);
        mensajeTextView.setVisibility(View.VISIBLE);
    }

    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);

        escribirButton.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.VISIBLE);

        fabCancel.setVisibility(View.GONE);
        nfcImage.setVisibility(View.GONE);
        mensajeTextView.setVisibility(View.GONE);
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

    @Override
    protected void onNewIntent(Intent intent) {
        int contador=0;
        JSONObject json = new JSONObject();
        String tagInfo ="";
        Boolean limpiarorigen = false;
        boolean continuar = false;
        String uidtag ="";
        if (writeMode) {
            if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                int tipo=0;
                String[] techs = myTag.getTechList();
                for (String t : techs) {
                    if (MifareClassic.class.getName().equals(t)) {
                        nfcTag = new NFCTag(myTag, this);
                        uidtag = nfcTag.byteArrayToHexString(myTag.getId());
                        tipo=1;
                    }
                    else if (MifareUltralight.class.getName().equals(t)) {
                        nfcUltra = new NFCUltralight(myTag, this);
                        uidtag = nfcUltra.byteArrayToHexString(myTag.getId());
                        tipo=2;
                    }
                }
                if(error_eliminar == 0) {
                    if (uidtag.equals(UID)) {
                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();
                        if (tipo == 1) {
                            tagInfo = nfcTag.readSector(myTag, 0, 1);
                            if (tagInfo != null) {
                                limpiarorigen = nfcTag.cleanSector(myTag, 1);
                                if (!limpiarorigen){
                                    error_eliminar = 1;
                                }else{
                                    error_eliminar = 2;
                                }
                                continuar = true;
                            } else {
                                snackbar = Snackbar.make(findViewById(R.id.content_set_destino),getString(R.string.error_tag_comunicacion) , Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(Color.RED);
                                snackbar.show();
                            }
                        }
                        if (tipo == 2) {
                            try{
                                tagInfo = nfcUltra.readPage(myTag, 4) + nfcUltra.readPage(myTag, 5);
                                Boolean  limpiar = nfcUltra.cleanTag(myTag);
                                if (!limpiar){
                                    error_eliminar = 1;
                                }else{
                                    error_eliminar = 2;
                                }
                                continuar = true;
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                    } else {
                        snackbar = Snackbar.make(findViewById(R.id.content_set_destino), "Por favor utiliza el TAG correcto", Snackbar.LENGTH_LONG);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(Color.RED);
                        snackbar.show();
                    }
                } else if(error_eliminar == 1){
                    if(tipo == 1) {
                        limpiarorigen = nfcTag.cleanSector(myTag, 1);

                        if (limpiarorigen){
                            error_eliminar = 2;
                        }
                    } else if( tipo == 2){
                        Boolean  limpiar = nfcUltra.cleanTag(myTag);
                        if (limpiar){
                            error_eliminar = 2;
                        }
                    }


                }
            }
        }

        // Toast.makeText(getApplicationContext(),"numero de viaje: " + contador, Toast.LENGTH_SHORT).show();
        if(continuar) {
            Integer idCamion = Util.getIdCamion(tagInfo);
            Integer idProyecto = Util.getIdProyecto(tagInfo);
            Viaje viaje = null;
            Camion c = new Camion(getApplicationContext());

            String aux = "";
            String fechaLlegada = Util.getFecha();
            String horaLlegada = Util.getTime();
            ContentValues cv = new ContentValues();
            cv.put("FechaCarga", fechaLlegada);
            cv.put("HoraCarga", horaLlegada);
            cv.put("IdProyecto", idProyecto);
            cv.put("IdCamion", idCamion);
            cv.put("IdOrigen", idOrigen);
            cv.put("IdTiro", usuario.idtiro);
            cv.put("FechaLlegada", fechaLlegada);
            cv.put("HoraLlegada", horaLlegada);
            cv.put("FechaSalida", fechaLlegada);
            cv.put("HoraSalida", horaLlegada);

            cv.put("IdMaterial", idMaterial);
            cv.put("Observaciones", observaciones.getText().toString());
            cv.put("Creo", usuario.getId());
            cv.put("Estatus", "1");
           // cv.put("Ruta", idRuta);

            aux = Util.dateFolios();

            cv.put("Code", Util.folio(aux) + String.valueOf(idCamion));
            cv.put("uidTAG", UID);
            cv.put("IMEI", IMEI);
            cv.put("CodeImagen", Util.getCodeFecha(idCamion, aux));

            if (textDeductiva.getText().toString().equals("")) {
                cv.put("deductiva", 0);
                cv.put("idMotivo", 0);
            } else {
                cv.put("deductiva", textDeductiva.getText().toString());
                cv.put("idMotivo", idMotivo);
            }
            RandomString r = new RandomString(10);
            cv.put("FolioRandom", r.nextString().toUpperCase());
            cv.put("primerToque", usuario.getId());
            c = c.find(idCamion);
            cv.put("cubicacion", String.valueOf(c.capacidad));

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
            coordenada.create(cv,getApplicationContext());
            //  aux="";
            //}


            destinoSuccess.putExtra("idViaje", viaje.idViaje);
            destinoSuccess.putExtra("LIST", 0);
            destinoSuccess.putExtra("code", aux);
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
                                if(!Viaje.isSync(getApplicationContext())) {
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
            if(!Viaje.isSync(getApplicationContext())){
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
}
