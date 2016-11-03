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

import java.util.ArrayList;
import java.util.HashMap;

public class SetDestinoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    Usuario usuario;
    Tiro tiro;
    Ruta ruta;

    //GPS
    private GPSTracker gps;
    private String IMEI;
    private Double latitude;
    private Double longitude;

    //NFC
    private NFCTag nfcTag;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private Boolean writeMode;

    //Referencias UI
    private Spinner tirosSpinner;
    private Spinner rutasSpinner;
    private Button escribirDestinoButton;
    private LinearLayout mainLayout;
    private ImageView nfcImage;
    private FloatingActionButton fabCancel;
    private TextView mensajeTextView;
    private EditText observacionesTextView;
    private Snackbar snackbar;
    private ProgressDialog progressDialogSync;

    private Integer idTiro;
    private Integer idRuta;
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


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

        mensajeTextView.setVisibility(View.INVISIBLE);
        nfcImage.setVisibility(View.INVISIBLE);
        fabCancel.setVisibility(View.INVISIBLE);

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

                if(drawer != null)
                    drawer.post(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < drawer.getChildCount(); i++) {
                                View child = drawer.getChildAt(i);
                                TextView tvp = (TextView) child.findViewById(R.id.textViewProyecto);
                                TextView tvu = (TextView) child.findViewById(R.id.textViewUser);

                                if (tvp != null) {
                                    tvp.setText(usuario.descripcionBaseDatos);
                                }
                                if (tvu != null) {
                                    tvu.setText(usuario.nombre);
                                }
                            }
                        }
                    });

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
                if(idRuta == 0) {
                    Toast.makeText(getApplicationContext(), "Por favor seleccione la Ruta de la lista", Toast.LENGTH_SHORT).show();
                    rutasSpinner.requestFocus();
                } else if (idTiro == 0) {
                    Toast.makeText(getApplicationContext(), "Por favor seleccione el Tiro de la lista", Toast.LENGTH_SHORT).show();
                    tirosSpinner.requestFocus();
                } else {
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
            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
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
                                    Toast.makeText(getApplicationContext(), "No es necesaria la sincronización en este momento", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.error_internet, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(getApplicationContext(), R.string.error_internet, Toast.LENGTH_SHORT).show();
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
        if (writeMode) {
            if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                nfcTag = new NFCTag(myTag, this);

                String UID = nfcTag.idTag(myTag);
                if (UID.equals(getIntent().getStringExtra("UID"))) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    String viajes = nfcTag.readSector(myTag,2,8);
                    viajes=viajes.replace(" ","");
                    int contador=0;
                    contador = Integer.valueOf(viajes) + 1;
                    nfcTag.writeSector(myTag, 2, 8, String.valueOf(contador));
                    Toast.makeText(getApplicationContext(),"numero de viaje: " + contador, Toast.LENGTH_SHORT).show();
                    String tagInfo = nfcTag.readSector(myTag, 0, 1);
                    Integer idCamion = Util.getIdCamion(tagInfo);
                    Integer idProyecto = Util.getIdProyecto(tagInfo);

                    String tagOrigen = nfcTag.readSector(myTag, 1, 4);
                    Integer idOrigen = Util.getIdOrigen(tagOrigen);
                    Integer idMaterial = Util.getIdMaterial(tagOrigen);

                    String fechaString = nfcTag.readSector(myTag, 1, 5);

                    ContentValues cv = new ContentValues();
                    cv.put("FechaCarga", Util.getFecha());
                    cv.put("HoraCarga", Util.getTime());
                    cv.put("IdProyecto", idProyecto);
                    cv.put("IdCamion", idCamion);
                    cv.put("IdOrigen", idOrigen);
                    cv.put("FechaSalida", Util.getFecha(fechaString));
                    cv.put("HoraSalida", Util.getTime(fechaString));
                    cv.put("IdTiro", idTiro);
                    cv.put("FechaLlegada", Util.getFecha());
                    cv.put("HoraLlegada", Util.getTime());
                    cv.put("IdMaterial", idMaterial);
                    cv.put("Observaciones", observacionesTextView.getText().toString());
                    cv.put("Creo", usuario.getId());
                    cv.put("Estatus", "10");
                    cv.put("Ruta", idRuta);
                    cv.put("Code", getCode(contador,idCamion).toUpperCase());
                    cv.put("uidTAG", UID);
                    Viaje viaje = new Viaje(this);
                    viaje.create(cv);

                    cv.clear();
                    cv.put("IMEI", IMEI);
                    cv.put("idevento", 3);
                    cv.put("latitud", latitude);
                    cv.put("longitud", longitude);
                    cv.put("fecha_hora", Util.timeStamp());
                    cv.put("code", getCode(contador,idCamion).toUpperCase());
                    Coordenada coordenada = new Coordenada(this);
                    coordenada.create(cv, SetDestinoActivity.this);

                    nfcTag.cleanSector(myTag,1);

                    Intent destinoSuccess = new Intent(this, SuccessDestinoActivity.class);
                    destinoSuccess.putExtra("idViaje", viaje.idViaje);
                    destinoSuccess.putExtra("LIST", 0);
                    destinoSuccess.putExtra("code", getCode(contador,idCamion));
                    startActivity(destinoSuccess);
                } else {
                    snackbar = Snackbar.make(findViewById(R.id.content_set_origen), "Por favor utiliza el TAG correcto", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(Color.RED);
                    snackbar.show();
                }
            }
        }
    }

    public static String getCode(Integer viaje, Integer idCamion) {
        String mensaje = "";
        String camion = "";
        String viajes="";
        viajes= viaje.toString();
        camion = idCamion.toString();
        int ceros = 0;

        if(camion.length() < 5){
            ceros = 5 - camion.length();
            for ( int i=0; i< ceros; i++){
                mensaje += "0";
            }
            mensaje +=camion;

        }
        else{
            mensaje +=camion;
        }
        if(viajes.length() < 5){
            ceros=0;
            ceros = 5 - viajes.length();
            for(int i=0; i< ceros; i++){
                mensaje+="0";
            }
            mensaje+=viajes;
        }
        else{
            mensaje+=viajes;
        }

        /*String resp = Long.toHexString(Long.parseLong(mensaje));
        mensaje="";
        if (resp.length() < 10){
            ceros=0;
            ceros= 10 - resp.length();
            for (int i=0; i<ceros; i++){
                mensaje += "0";
            }
            mensaje += resp;
        }*/
        return mensaje;
    }
}
