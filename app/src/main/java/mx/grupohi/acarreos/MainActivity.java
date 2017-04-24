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
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private NFCTag nfc;
    private NFCUltralight nfcUltra;
    private NfcAdapter nfc_adapter;
    private PendingIntent pendingIntent;
    private PendingIntent resp;
    private IntentFilter writeTagFilters[];
    private Snackbar snackbar;
    private String UID;
    private Integer idOrigen;
    private Intent setOrigenActivity;
    private Intent setDestinoActivity;
    private Intent listaViajes;
    private Integer CamionID;


    //Referencias UI
    private LinearLayout infoLayout;
    private LinearLayout origenLayout;
    private Button actionButton;
    private ImageView nfcImage;
    private TextView infoTag;
    private ProgressDialog progressDialogSync;

    private TextView tCamion;
    private TextView tCapacidad;
    private TextView tCapacidad2;
    private TextView tMarca;
    private TextView tModelo;
    private TextView tAlto;
    private TextView tAncho;
    private TextView tLargo;

    private TextView tOrigen;
    private TextView tMaterial;
    private TextView tFecha;
    private TextView tHora;
    String validacion;

    Intent descarga;
    Camion camion;

    private Boolean writeMode;

    Usuario usuario;
    Viaje viaje;
    Coordenada coordenada;
    Configuraciones c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setOrigenActivity = new Intent(this, SetOrigenActivity.class);
        setDestinoActivity = new Intent(this, SetDestinoActivity.class);
        listaViajes =new Intent(this, ListaViajesActivity.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.title_activity_main));
        usuario = new Usuario(this);
        camion = new Camion(getApplicationContext());
        usuario = usuario.getUsuario();
        viaje = new Viaje(this);
        coordenada = new Coordenada(this);
        writeMode = true;
        infoLayout = (LinearLayout) findViewById(R.id.LayoutInfo);
        origenLayout = (LinearLayout) findViewById(R.id.LayoutOrigen);
        actionButton = (Button) findViewById(R.id.ButtonAction);
        infoTag = (TextView) findViewById(R.id.textInfoTag);
        nfcImage = (ImageView) findViewById(R.id.nfc_background);

        tCamion = (TextView) findViewById(R.id.txtCamion);
        tCapacidad = (TextView) findViewById(R.id.txtCapacidad);
        tCapacidad2 = (TextView) findViewById(R.id.textViewCapacidad);
        tMarca = (TextView) findViewById(R.id.textViewMarca);
        tModelo = (TextView) findViewById(R.id.textViewModelo);
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

        validacion = getIntent().getStringExtra("validacion");
        c = new  Configuraciones(getApplicationContext());
        c = c.find();

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        writeTagFilters = new IntentFilter[]{tagDetected};

        nfc_adapter = NfcAdapter.getDefaultAdapter(this);
        if (nfc_adapter == null) {
            Toast.makeText(this, getString(R.string.error_no_nfc), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String imagen = usuario.getImagen();
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
                        TextView tpe = (TextView) child.findViewById(R.id.textViewPerfil);
                        TextView tvv = (TextView) child.findViewById(R.id.textViewVersion);

                        if (tvp != null) {
                            tvp.setText(usuario.descripcionBaseDatos);
                        }
                        if (tvu != null) {
                            tvu.setText(usuario.nombre);
                        }
                        if (tpe != null){
                            if(usuario.origen_name == "0"){
                                tpe.setText(usuario.tiro_name);
                            }else if(usuario.tiro_name == "0"){
                                tpe.setText(usuario.origen_name);
                            }
                        }
                        if (tvv != null) {
                            tvv.setText(getString(R.string.app_name)+"     "+"Versión " + String.valueOf(BuildConfig.VERSION_NAME));
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
        nfc_adapter = NfcAdapter.getDefaultAdapter(this);
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
            finish();
        } else {
            finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int tipo=0;
        Integer tagCamion =0;
        Integer tagProyecto =0;
        Integer tagOrigen =0;
        Integer tagMaterial = 0;
        String fechaString = "";
        if (nfc_adapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            clearCamionInfo();
            clearOrigenInfo();
            if (snackbar != null && snackbar.isShown()) snackbar.dismiss();
            final Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            TagModel tagModel = new TagModel(getApplicationContext());
            Origen origen = new Origen(getApplicationContext());
            Material material = new Material(getApplicationContext());
            String[] techs = myTag.getTechList();

            for (String t : techs) {
                if (MifareClassic.class.getName().equals(t)) {
                    nfc = new NFCTag(myTag, this);
                    try {
                        UID =  nfc.byteArrayToHexString(myTag.getId());
                        String tagString = nfc.readSector(myTag, 0, 1);
                        String origenString = nfc.readSector(myTag, 1, 4);
                        fechaString = nfc.readSector(myTag, 1, 5);
                        tagCamion = Util.getIdCamion(tagString);
                        tagProyecto = Util.getIdProyecto(tagString);
                        tagModel = tagModel.find(UID, tagCamion, tagProyecto);
                        tagOrigen = Util.getIdOrigen(origenString);
                        tagMaterial = Util.getIdMaterial(origenString);
                        origen = origen.find(tagOrigen);
                        material = material.find(tagMaterial);

                    }catch (Exception e){
                        tagModel=null;
                    }
                } else if (MifareUltralight.class.getName().equals(t)) {
                    nfcUltra = new NFCUltralight(myTag, this);
                    UID = nfcUltra.byteArrayToHexString(myTag.getId());
                    try {
                        tagCamion = Integer.valueOf(nfcUltra.readPage(myTag, 4));
                        tagProyecto = Integer.valueOf(nfcUltra.readPage(myTag, 5));
                        String origen1 = nfcUltra.readPage(myTag, 9);
                        String material1 = nfcUltra.readPage(myTag, 8);
                        tagModel = tagModel.find(UID, tagCamion, tagProyecto);

                        if (origen1 != null && material1 != null) {
                            tagOrigen = Integer.valueOf(origen1);
                            tagMaterial = Integer.valueOf(material1);
                            origen = origen.find(tagOrigen);
                            material = material.find(tagMaterial);
                            fechaString = nfcUltra.readPage(myTag, 10) + nfcUltra.readPage(myTag, 11) + nfcUltra.readPage(myTag, 12) + nfcUltra.readPage(myTag, 13).substring(0, 2);
                        } else {
                            origen = null;
                            material = null;
                        }
                    }catch (Exception e){
                        tagModel=null;
                    }

                }
            }


            if (tagModel != null) {

                try {
                        if (origen != null && material != null) {
                            camion = camion.find(tagModel.idCamion);
                            CamionID = camion.idCamion;
                            setCamionInfo(camion);
                            setTitle("INFORMACIÓN DEL TAG");
                            if(validacion == null && c.validacion == 1){
                                Intent validar = new Intent(getApplicationContext(), ValidacionActivity.class);
                                validar.putExtra("economico", camion.economico);
                                validar.putExtra("placas", camion.placas);
                                validar.putExtra("placasCaja", camion.placasCaja);
                                validar.putExtra("capacidad", String.valueOf(camion.capacidad));

                                startActivityForResult(validar,0);
                            }

                            setOrigenInfo(origen, material, fechaString);
                            idOrigen = origen.idOrigen;
                            actionButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    setDestinoActivity.putExtra("UID", UID);
                                    setDestinoActivity.putExtra("idOrigen", idOrigen);
                                    setDestinoActivity.putExtra("camion", String.valueOf(CamionID));
                                    startActivity(setDestinoActivity);
                                }
                            });
                        }else{
                            snackbar = Snackbar.make(findViewById(R.id.content_main),R.string.error_sin_origen, Snackbar.LENGTH_LONG);
                            View snackBarView = snackbar.getView();
                            snackBarView.setBackgroundColor(Color.RED);
                            snackbar.show();
                        }

                }catch (Exception e){
                    snackbar = Snackbar.make(findViewById(R.id.content_main),R.string.error_tag, Snackbar.LENGTH_LONG);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(Color.RED);
                    snackbar.show();
                }
            } else {
                snackbar = Snackbar.make(findViewById(R.id.content_main), "El TAG que intentas utilizar no es valido para el control de viajes", Snackbar.LENGTH_LONG);
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(Color.RED);
                snackbar.show();
            }
        } else if (nfc_adapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(getApplicationContext(), "El TAG que intentas utilizar no es compatible", Toast.LENGTH_LONG).show();
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

        tCamion.setText(camion.economico + " [" + camion.placas + "]");
        tCapacidad.setText("CAPACIDAD: " + camion.capacidad + " m3");
        tCapacidad2.setText(String.valueOf(camion.capacidad));
        tMarca.setText(camion.marca);
        tModelo.setText(camion.modelo);
        tAlto.setText(String.valueOf(camion.alto));
        tAncho.setText(String.valueOf(camion.ancho));
        tLargo.setText(String.valueOf(camion.largo));

        infoTag.setVisibility(View.GONE);
        nfcImage.setVisibility(View.GONE);
        infoLayout.setVisibility(View.VISIBLE);
        actionButton.setVisibility(View.VISIBLE);

    }

    private void clearCamionInfo() {

        infoTag.setVisibility(View.VISIBLE);
        nfcImage.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.GONE);
        actionButton.setVisibility(View.GONE);

        tCamion.setText("");
        tCapacidad.setText("");
        tCapacidad2.setText("");
        tMarca.setText("");
        tModelo.setText("");
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

        actionButton.setText("ESCRIBIR DESTINO");
    }

    private void clearOrigenInfo() {
        origenLayout.setVisibility(View.GONE);
        tOrigen.setText("");
        tMaterial.setText("");
        tFecha.setText("");
        tHora.setText("");
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
                mainActivity = new Intent(getApplicationContext(), SetOrigenActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivity);
            }else if(tipo == 1){
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }

        } else if (id == R.id.nav_sync) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(MainActivity.this, "Sincronizando datos", "Por favor espere...", true);
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
            startActivity(listaViajes);
        }else if (id == R.id.nav_desc) {
            descarga = new Intent(this, DescargaActivity.class);
            startActivity(descarga);
        } else if (id == R.id.nav_logout) {
            if(!Viaje.isSync(getApplicationContext())){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(MainActivity.this, "Sincronizando datos", "Por favor espere...", true);
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
