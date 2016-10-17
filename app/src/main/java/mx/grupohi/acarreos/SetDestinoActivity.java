package mx.grupohi.acarreos;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.AutoCompleteTextView;
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
    private TextView tagAlertTextView;
    private EditText observacionesTextView;

    private Integer idTiro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_destino);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        usuario = new Usuario(this);
        ruta = new Ruta(this);
        tiro = new Tiro(this);

        gps = new GPSTracker(SetDestinoActivity.this);
        TelephonyManager phneMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = phneMgr.getDeviceId();

        tirosSpinner = (Spinner) findViewById(R.id.spinnerTiros);
        rutasSpinner = (Spinner) findViewById(R.id.spinnerRutas);
        escribirDestinoButton = (Button) findViewById(R.id.buttonEscribirDestino);
        mainLayout = (LinearLayout) findViewById(R.id.MainLayout);
        nfcImage = (ImageView) findViewById(R.id.imageViewNFC);
        tagAlertTextView = (TextView) findViewById(R.id.textViewTagAlert);
        observacionesTextView = (EditText) findViewById(R.id.textObservaciones);
        fabCancel = (FloatingActionButton) findViewById(R.id.fabCancel);

        tagAlertTextView.setVisibility(View.INVISIBLE);
        nfcImage.setVisibility(View.INVISIBLE);
        fabCancel.setVisibility(View.INVISIBLE);

        rutasSpinner.setEnabled(false);


        final ArrayList<String> descripcionesTiros = tiro.getArrayListDescripciones(getIntent().getIntExtra("idOrigen", 1));
        final ArrayList <String> idsTiros = tiro.getArrayListId(getIntent().getIntExtra("idOrigen", 1));

        final String[] spinnerTirosArray = new String[idsTiros.size()];
        final HashMap<String, String> spinnerTirosMap = new HashMap<>();

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
                String descripcion = tirosSpinner.getSelectedItem().toString();
                idTiro = Integer.valueOf(spinnerTirosMap.get(descripcion));
                if(idTiro != 0) {
                    rutasSpinner.setEnabled(true);

                    final ArrayList<String> descripcionesRutas = ruta.getArrayListDescripciones(getIntent().getIntExtra("idOrigen", 0), idTiro);
                    final ArrayList <String> idsRutas = ruta.getArrayListId(getIntent().getIntExtra("idOrigen", 0), idTiro);

                    final String[] spinnerRutasArray = new String[idsRutas.size()];
                    final HashMap<String, String> spinnerRutasMap = new HashMap<>();

                    for (int i = 0; i < idsRutas.size(); i++) {
                        spinnerRutasMap.put(descripcionesRutas.get(i), idsRutas.get(i));
                        spinnerRutasArray[i] = descripcionesRutas.get(i);
                    }

                    final ArrayAdapter<String> arrayAdapterRutas = new ArrayAdapter<>(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item, spinnerRutasArray);
                    arrayAdapterRutas.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    rutasSpinner.setAdapter(arrayAdapterRutas);

                } else {
                    rutasSpinner.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
        tagAlertTextView.setVisibility(View.VISIBLE);
    }

    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);

        escribirDestinoButton.setVisibility(View.VISIBLE);
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
        getMenuInflater().inflate(R.menu.set_destino, menu);
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
        } else if (id == R.id.nav_list) {
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
}
