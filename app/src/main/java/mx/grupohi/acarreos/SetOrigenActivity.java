package mx.grupohi.acarreos;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.ImageWriter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class SetOrigenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //Objetos
    private Usuario usuario;
    private Material material;
    private Origen origen;

    //Variables
    private Integer idMaterial;
    private Integer idOrigen;

    //Referencias UI
    Spinner materialesSpinner;
    private Spinner origenesSpinner;
    private Button escribirOrigenButton;
    private LinearLayout mainLayout;
    private ImageView nfcImage;
    private FloatingActionButton fabCancel;
    private TextView tagAlertTextView;

    private Snackbar snackbar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_origen);
        escribirOrigenButton = (Button) findViewById(R.id.buttonEscribirOrigen);

        usuario = new Usuario(this);
        material = new Material(this);
        origen = new Origen(this);

        gps = new GPSTracker(SetOrigenActivity.this);

        TelephonyManager phneMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = phneMgr.getDeviceId();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        nfcImage = (ImageView) findViewById(R.id.imageViewNFC);
        fabCancel = (FloatingActionButton) findViewById(R.id.fabCancel);
        mainLayout = (LinearLayout) findViewById(R.id.MainLayout);
        tagAlertTextView =(TextView) findViewById(R.id.textViewTagAlert);

        tagAlertTextView.setVisibility(View.INVISIBLE);
        nfcImage.setVisibility(View.INVISIBLE);
        fabCancel.setVisibility(View.INVISIBLE);

        materialesSpinner = (Spinner) findViewById(R.id.spinnerMateriales);
        origenesSpinner = (Spinner) findViewById(R.id.spinnerOrigenes);

        final ArrayList<String> descripcionesMateriales = material.getArrayListDescripciones();
        final ArrayList <String> idsMateriales = material.getArrayListId();

        final String[] spinnerMaterialesArray = new String[idsMateriales.size()];
        final HashMap<String, String> spinnerMaterialesMap = new HashMap<>();

        for (int i = 0; i < idsMateriales.size(); i++) {
            spinnerMaterialesMap.put(descripcionesMateriales.get(i), idsMateriales.get(i));
            spinnerMaterialesArray[i] = descripcionesMateriales.get(i);
        }

        final ArrayAdapter<String> arrayAdapterMateriales = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item, spinnerMaterialesArray);
        arrayAdapterMateriales.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        materialesSpinner.setAdapter(arrayAdapterMateriales);

        final ArrayList<String> descripcionesOrigenes = origen.getArrayListDescripciones();
        final ArrayList <String> idsOrigenes = origen.getArrayListId();

        final String[] spinnerOrigenesArray = new String[idsOrigenes.size()];
        final HashMap<String, String> spinnerOrigenesMap = new HashMap<>();

        for (int i = 0; i < idsOrigenes.size(); i++) {
            spinnerOrigenesMap.put(descripcionesOrigenes.get(i), idsOrigenes.get(i));
            spinnerOrigenesArray[i] = descripcionesOrigenes.get(i);
        }

        final ArrayAdapter<String> arrayAdapterOrigenes = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item, spinnerOrigenesArray);
        arrayAdapterOrigenes.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        origenesSpinner.setAdapter(arrayAdapterOrigenes);

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

        origenesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String descripcion = origenesSpinner.getSelectedItem().toString();
                idOrigen = Integer.valueOf(spinnerOrigenesMap.get(descripcion));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        escribirOrigenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idMaterial == 0) {
                    Toast.makeText(getApplicationContext(), "Por favor seleccione un Material de la lista", Toast.LENGTH_SHORT).show();
                    materialesSpinner.requestFocus();
                } else if(idOrigen == 0) {
                    Toast.makeText(getApplicationContext(), "Por favor seleccione un Origen de la lista", Toast.LENGTH_SHORT).show();
                    origenesSpinner.requestFocus();
                } else {
                    checkNfcEnabled();
                    WriteModeOn();
                }
            }
        });

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteModeOff();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(writeMode) {
            if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                nfcTag = new NFCTag(myTag, this);

                String UID = nfcTag.idTag(myTag);

                if(UID.equals(getIntent().getStringExtra("UID"))) {
                    String data = Util.concatenar(String.valueOf(idMaterial), String.valueOf(idOrigen));
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                    String dataTime = Util.getFechaHora();

                    if (nfcTag.writeID(myTag, 1, 4, data) && nfcTag.writeID(myTag, 1, 5, dataTime)) {
                        ContentValues cv = new ContentValues();
                        cv.put("IMEI", IMEI);
                        cv.put("idevento", 2);
                        cv.put("latitud", latitude);
                        cv.put("longitud", longitude);
                        cv.put("fecha_hora", Util.timeStamp());
                        cv.put("code", "");

                        Coordenada coordenada = new Coordenada(getApplicationContext());
                        coordenada.create(cv);

                        Intent success = new Intent(SetOrigenActivity.this, SuccessOrigenActivity.class);
                        startActivity(success);
                    } else {
                        Toast.makeText(SetOrigenActivity.this, getString(R.string.error_tag_comunicacion), Toast.LENGTH_LONG).show();
                    }
                } else {
                    snackbar = Snackbar.make(findViewById(R.id.content_set_origen), "Por favor utiliza el TGA previamente correcto", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(Color.RED);
                    snackbar.show();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNfcEnabled();
        WriteModeOff();
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);

        escribirOrigenButton.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.INVISIBLE);

        fabCancel.setVisibility(View.VISIBLE);
        nfcImage.setVisibility(View.VISIBLE);
        tagAlertTextView.setVisibility(View.VISIBLE);
    }

    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);

        escribirOrigenButton.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.VISIBLE);

        fabCancel.setVisibility(View.GONE);
        nfcImage.setVisibility(View.GONE);
        tagAlertTextView.setVisibility(View.GONE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.set_origen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
        } else if (id == R.id.nav_sync) {
            Toast.makeText(this, getString(R.string.msg_sincronizacion_correcta), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_list) {
            //Intent lista_viajes_activity = new Intent(this, ListaViajesActivity.class);
            //startActivity(lista_viajes_activity);
        } else if (id == R.id.nav_pair_on) {

        } else if (id == R.id.nav_pair_off) {

        } else if (id == R.id.nav_logout) {
            Intent login_activity = new Intent(this, LoginActivity.class);
            usuario.destroy();
            startActivity(login_activity);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkNfcEnabled() {
        Boolean nfcEnabled = nfcAdapter.isEnabled();
        if (!nfcEnabled) {
            new android.app.AlertDialog.Builder(SetOrigenActivity.this)
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
}
