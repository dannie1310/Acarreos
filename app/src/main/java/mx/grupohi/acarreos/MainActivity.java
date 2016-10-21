package mx.grupohi.acarreos;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private NFCTag nfc;
    private NfcAdapter nfc_adapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private Snackbar snackbar;
    private String UID;
    private Integer idOrigen;
    private Intent setOrigenActivity;
    private Intent setDestinoActivity;
    private Intent listaViajes;


    //Referencias UI
    private LinearLayout infoLayout;
    private LinearLayout origenLayout;
    private Button actionButton;
    private ImageView nfcImage;
    private TextView infoTag;
    private ProgressDialog progressDialogSync;

    private TextView tEconomico;
    private TextView tPlacas;
    private TextView tMarca;
    private TextView tModelo;
    private TextView tAlto;
    private TextView tAncho;
    private TextView tLargo;
    private TextView tCapacidad;

    private TextView tOrigen;
    private TextView tMaterial;
    private TextView tFecha;
    private TextView tHora;

    private Boolean writeMode;

    Usuario usuario;
    Viaje viaje;
    Coordenada coordenada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setOrigenActivity = new Intent(this, SetOrigenActivity.class);
        setDestinoActivity = new Intent(this, SetDestinoActivity.class);
        listaViajes =new Intent(this, ListaViajesActivity.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.title_activity_main));
        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        viaje = new Viaje(this);
        coordenada = new Coordenada(this);
        writeMode = true;
        infoLayout = (LinearLayout) findViewById(R.id.LayoutInfo);
        origenLayout = (LinearLayout) findViewById(R.id.LayoutOrigen);
        actionButton = (Button) findViewById(R.id.ButtonAction);
        infoTag = (TextView) findViewById(R.id.textInfoTag);
        nfcImage = (ImageView) findViewById(R.id.nfc_background);

        tEconomico = (TextView) findViewById(R.id.textViewEconomico);
        tPlacas = (TextView) findViewById(R.id.textViewPlacas);
        tMarca = (TextView) findViewById(R.id.textViewMarca);
        tModelo = (TextView) findViewById(R.id.textViewModelo);
        tCapacidad = (TextView) findViewById(R.id.textViewCapacidad);
        tAlto = (TextView) findViewById(R.id.textViewAlto);
        tAncho = (TextView) findViewById(R.id.textViewAncho);
        tLargo = (TextView) findViewById(R.id.textViewLargo);

        tOrigen = (TextView) findViewById(R.id.textViewOrigen);
        tMaterial = (TextView) findViewById(R.id.textViewMaterial);
        tFecha = (TextView) findViewById(R.id.textViewFecha);
        tHora = (TextView) findViewById(R.id.textViewHora);

        infoLayout.setVisibility(View.GONE);
        origenLayout.setVisibility(View.GONE);
        actionButton.setVisibility(View.GONE);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        writeTagFilters = new IntentFilter[]{tagDetected};

        nfc_adapter = NfcAdapter.getDefaultAdapter(this);
        if (nfc_adapter == null) {
            Toast.makeText(this, getString(R.string.error_no_nfc), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkNfcEnabled();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        WriteModeOn();
        checkNfcEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        WriteModeOff();
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

    @Override
    protected void onNewIntent(Intent intent) {
        if (nfc_adapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            clearCamionInfo();
            clearOrigenInfo();
            if (snackbar != null && snackbar.isShown()) snackbar.dismiss();
            final Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            nfc = new NFCTag(myTag, this);
            UID = nfc.idTag(myTag);

            TagModel tagModel = new TagModel(getApplicationContext());
            Origen origen = new Origen(getApplicationContext());
            Material material = new Material(getApplicationContext());

            String tagString = nfc.readSector(myTag, 0, 1);

            Integer tagCamion = Util.getIdCamion(tagString);
            Integer tagProyecto = Util.getIdProyecto(tagString);
            tagModel = tagModel.find(UID, tagCamion, tagProyecto);

            String origenString = nfc.readSector(myTag, 1, 4);

            Integer tagOrigen = Util.getIdOrigen(origenString);
            Integer tagMaterial = Util.getIdMaterial(origenString);
            origen = origen.find(tagOrigen);
            material = material.find(tagMaterial);


            String fechaString = nfc.readSector(myTag, 1, 5);

            if (tagModel != null) {
                Camion camion = new Camion(getApplicationContext());
                camion = camion.find(tagModel.idCamion);
                setCamionInfo(camion);
                setTitle("INFORMACIÓN DEL TAG");
                if (origen != null && material != null) {
                    setOrigenInfo(origen, material, fechaString);
                    idOrigen = origen.idOrigen;
                    actionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setDestinoActivity.putExtra("UID", UID);
                            setDestinoActivity.putExtra("idOrigen", idOrigen);
                            startActivity(setDestinoActivity);
                        }
                    });
                } else {
                    actionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setOrigenActivity.putExtra("UID", UID);
                            startActivity(setOrigenActivity);
                        }
                    });
                }
            } else {
                snackbar = Snackbar.make(findViewById(R.id.content_main), "El TAG que intentas utilizar no es valido para el control de viajes", Snackbar.LENGTH_SHORT);
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(Color.RED);
                snackbar.show();
            }
        } else if (nfc_adapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(getApplicationContext(), "El TAG que intentas utilizar no es compatible", Toast.LENGTH_SHORT).show();
        }
    }

    private void WriteModeOn() {
        nfc_adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff() {
        nfc_adapter.disableForegroundDispatch(this);
        infoLayout.setEnabled(true);
    }

    private void checkNfcEnabled() {
        Boolean nfcEnabled = nfc_adapter.isEnabled();
        if (!nfcEnabled) {
            new android.app.AlertDialog.Builder(MainActivity.this)
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

    private void setCamionInfo(Camion camion) {
        infoTag.setVisibility(View.GONE);
        nfcImage.setVisibility(View.GONE);
        infoLayout.setVisibility(View.VISIBLE);
        actionButton.setVisibility(View.VISIBLE);

        tEconomico.setText(camion.economico);
        tPlacas.setText(camion.placas);
        tMarca.setText(camion.marca);
        tModelo.setText(camion.modelo);
        tCapacidad.setText(String.valueOf(camion.capacidad));
        tAlto.setText(String.valueOf(camion.alto));
        tAncho.setText(String.valueOf(camion.ancho));
        tLargo.setText(String.valueOf(camion.largo));
    }

    private void clearCamionInfo() {

        infoTag.setVisibility(View.VISIBLE);
        nfcImage.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.GONE);
        actionButton.setVisibility(View.GONE);

        tEconomico.setText("");
        tPlacas.setText("");
        tMarca.setText("");
        tModelo.setText("");
        tCapacidad.setText("");
        tAlto.setText("");
        tAncho.setText("");
        tLargo.setText("");
    }

    private void setOrigenInfo(Origen origen, Material material, String fechaHora) {

        origenLayout.setVisibility(View.VISIBLE);
        tOrigen.setText(origen.descripcion);
        tMaterial.setText(material.descripcion);

        tFecha.setText(Util.getFecha(fechaHora));
        tHora.setText(Util.getTime(fechaHora));

        actionButton.setText("CLICK AQUI PARA ESCRIBIR EL DESTINO");
    }

    private void clearOrigenInfo() {
        origenLayout.setVisibility(View.GONE);
        tOrigen.setText("");
        tMaterial.setText("");
        tFecha.setText("");
        tHora.setText("");
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
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_sync) {
            if (Util.isNetworkStatusAvialable(this)) {
                if(!Viaje.isSync(getApplicationContext())) {
                    progressDialogSync = ProgressDialog.show(this, "Sincronizando datos", "Por favor espere...", true);
                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);
                } else {
                    Toast.makeText(getApplicationContext(), "No es necesaria la sincronización en este momento", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.error_internet, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_list) {
            startActivity(listaViajes);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
