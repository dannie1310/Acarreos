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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SetDestinoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    Usuario usuario;
    Tiro tiro;
    Ruta ruta;
    Camion c;

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
    private Spinner motivos;
    private Button escribirDestinoButton;
    private LinearLayout mainLayout;
    private ImageView nfcImage;
    private FloatingActionButton fabCancel;
    private TextView mensajeTextView;
    private TextView textmotivo;
    private EditText observacionesTextView;
    private  EditText deductiva;
    private Snackbar snackbar;
    private ProgressDialog progressDialogSync;

    private Integer idTiro;
    private Integer idRuta;
    private Integer idMotivo;
    private HashMap<String, String> spinnerTirosMap;
    private HashMap<String, String> spinnerRutasMap;
    private  HashMap<String, String> spinnerMotivosMap;
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
        camionId = getIntent().getStringExtra("camion");

        c = new Camion(getApplicationContext());
        c = c.find(Integer.valueOf(camionId));



        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        ruta = new Ruta(this);
        tiro = new Tiro(this);

        gps = new GPSTracker(SetDestinoActivity.this);
        TelephonyManager phneMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
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
        textmotivo = (TextView) findViewById(R.id.textViewMotivo);
        motivos = (Spinner) findViewById(R.id.spinnerMotivo);
        textmotivo.setVisibility(View.GONE);
        motivos.setVisibility(View.GONE);
        mensajeTextView.setVisibility(View.INVISIBLE);
        nfcImage.setVisibility(View.INVISIBLE);
        fabCancel.setVisibility(View.INVISIBLE);

        deductiva.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                                 textmotivo.setVisibility(View.VISIBLE);
                                                 motivos.setVisibility(View.VISIBLE);
                                         }
                                     }

        );


        final ArrayList<String> descripcionesTiros = tiro.getArrayListDescripciones(getIntent().getIntExtra("idOrigen", 1));
        final ArrayList <String> idsTiros = tiro.getArrayListId(getIntent().getIntExtra("idOrigen", 1));

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

                final ArrayList<String> descripcionesRutas = ruta.getArrayListDescripciones(getIntent().getIntExtra("idOrigen", 1), idTiro);
                final ArrayList <String> idsRutas = ruta.getArrayListId(getIntent().getIntExtra("idOrigen", 1), idTiro);

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
                        TextView tvv = (TextView) child.findViewById(R.id.textViewVersion);

                        if (tvp != null) {
                            tvp.setText(usuario.descripcionBaseDatos);
                        }
                        if (tvu != null) {
                            tvu.setText(usuario.nombre);
                        }
                        if (tvv != null) {
                            tvv.setText(getString(R.string.app_name)+"     "+"Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                        }
                    }
                }
            });
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
        motivos.setAdapter(arrayAdapterMotivos);

        motivos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String Mo = String.valueOf(parent.getItemAtPosition(position));
                idMotivo = Integer.valueOf(spinnerMotivosMap.get(Mo));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                if (idTiro == 0) {
                    Toast.makeText(getApplicationContext(), "Por favor seleccione el Tiro de la lista", Toast.LENGTH_SHORT).show();
                    tirosSpinner.requestFocus();
                }
                else if(idRuta == 0) {
                    Toast.makeText(getApplicationContext(), "Por favor seleccione la Ruta de la lista", Toast.LENGTH_SHORT).show();
                    rutasSpinner.requestFocus();
                }

                else if(c.capacidad != 0 && c.capacidad!= null && !deductiva.getText().toString().equals("") && Integer.valueOf(deductiva.getText().toString()) != 0 && Integer.valueOf(deductiva.getText().toString()) >= c.capacidad) {
                     Toast.makeText(getApplicationContext(), R.string.error_deductiva, Toast.LENGTH_LONG).show();
                }
                else if (( deductiva.getText().toString().equals("")==false ) && idMotivo == 0){
                    Toast.makeText(getApplicationContext(), "Por favor seleccione un motivo", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(deductiva.getText().toString().equals("") || deductiva.getText().toString().equals("0")){
                        motivos.setSelection(0);
                        textmotivo.setVisibility(View.GONE);
                        motivos.setVisibility(View.GONE);
                    }
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
                                if(!Viaje.isSync(getApplicationContext())) {
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
            if(!Viaje.isSync(getApplicationContext())){
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

    @Override
    protected void onNewIntent(Intent intent) {
        int contador=0;
        JSONObject json = new JSONObject();
        String tagInfo ="";
        String viajes = "";
        String tagOrigen = "";
        String fechaString = "";
        String idUsuario = null;
        Boolean limpiarorigen = false;
        boolean continuar = false;
        String UID ="";
        if (writeMode) {
            if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                int tipo=0;
                String[] techs = myTag.getTechList();
                for (String t : techs) {
                    if (MifareClassic.class.getName().equals(t)) {
                        nfcTag = new NFCTag(myTag, this);
                        UID = nfcTag.byteArrayToHexString(myTag.getId());
                        tipo=1;
                    }
                    else if (MifareUltralight.class.getName().equals(t)) {
                        nfcUltra = new NFCUltralight(myTag, this);
                        UID = nfcUltra.byteArrayToHexString(myTag.getId());
                        tipo=2;
                    }
                }
                if(error_eliminar == 0) {
                    if (UID.equals(getIntent().getStringExtra("UID"))) {

                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();
                        if (tipo == 1) {

                            tagInfo = nfcTag.readSector(myTag, 0, 1);
                            tagOrigen = nfcTag.readSector(myTag, 1, 4);
                            fechaString = nfcTag.readSector(myTag, 1, 5);
                            idUsuario = nfcTag.readSector(myTag, 1, 6);

                            if (tagInfo != null && tagOrigen != null && fechaString != null && idUsuario != null) {
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
                                tagOrigen = nfcUltra.readPage(myTag, 7) + nfcUltra.readPage(myTag, 8);
                                fechaString = nfcUltra.readPage(myTag, 9) + nfcUltra.readPage(myTag, 10) + nfcUltra.readPage(myTag, 11) + nfcUltra.readPage(myTag, 12).substring(0, 2);
                                idUsuario = nfcUltra.readUsuario(myTag, 13) + nfcUltra.readUsuario(myTag, 14);
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
                Integer idOrigen = Util.getIdOrigen(tagOrigen);
                Integer idMaterial = Util.getIdMaterial(tagOrigen);
                Viaje viaje = null;
                Camion c = new Camion(getApplicationContext());

                String aux = "";

                // for(int x=0; x<600;x++) {
                ContentValues cv = new ContentValues();
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
                if (fechaString.replace(" ", "").isEmpty()) {
                    String fecha = Util.getFechaDisminucion(fechaLlegada + " " + horaLlegada);
                    cv.put("FechaSalida", Util.getFecha(fecha));
                    cv.put("HoraSalida", Util.getTime(fecha));
                } else {
                    if (horaOrigen == null || fechaOrigen == null) {
                        String fecha = Util.getFechaDisminucion(fechaLlegada + " " + horaLlegada);
                        cv.put("FechaSalida", Util.getFecha(fecha));
                        cv.put("HoraSalida", Util.getTime(fecha));
                    } else {
                        cv.put("FechaSalida", fechaOrigen);
                        cv.put("HoraSalida", horaOrigen);
                    }

                }

                cv.put("IdMaterial", idMaterial);
                cv.put("Observaciones", observacionesTextView.getText().toString());
                cv.put("Creo", usuario.getId());
                cv.put("Estatus", "1");
                cv.put("Ruta", idRuta);

                aux = Util.dateFolios();

                cv.put("Code", Util.folio(aux) + String.valueOf(idCamion));
                cv.put("uidTAG", UID);
                cv.put("IMEI", IMEI);
                cv.put("CodeImagen", Util.getCodeFecha(idCamion, aux));

                if (deductiva.getText().toString().equals("")) {
                    cv.put("deductiva", 0);
                    cv.put("idMotivo", 0);
                } else {
                    cv.put("deductiva", deductiva.getText().toString());
                    cv.put("idMotivo", idMotivo);
                }
                RandomString r = new RandomString(10);
                cv.put("FolioRandom", r.nextString().toUpperCase());
                cv.put("primerToque", idUsuario.replace(" ", ""));
                c = c.find(idCamion);
                cv.put("cubicacion", String.valueOf(c.capacidad));
                cv.put("tipoEsquema", usuario.getTipoEsquema());
                cv.put("numImpresion", 0);

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

}
