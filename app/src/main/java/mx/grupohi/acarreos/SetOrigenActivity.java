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
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private  Camion c;

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
    private TextView text_origen;
    private ProgressDialog progressDialogSync;
    private CheckBox pago;
    private TextView vale_mina;
    private TextView seguimiento;
    private TextView volumen;
    private TextView deductiva;
    private TextInputLayout mina;
    private TextInputLayout seg;
    private TextInputLayout vol;
    private TextInputLayout ded;
    private Snackbar snackbar;
    private TextView textmotivo;
    private Integer idMotivo;
    private  HashMap<String, String> spinnerMotivosMap;
    private Spinner motivos;

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
    private Integer tipo;
    Certificados certificados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        certificados = new Certificados(getApplicationContext());
        setContentView(R.layout.activity_set_origen);
        escribirOrigenButton = (Button) findViewById(R.id.buttonEscribirOrigen);

        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        material = new Material(this);
        origen = new Origen(this);

        gps = new GPSTracker(SetOrigenActivity.this);

        TelephonyManager phneMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
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
        tagAlertTextView =(TextView) findViewById(R.id.textViewMensaje);

        tagAlertTextView.setVisibility(View.INVISIBLE);
        nfcImage.setVisibility(View.INVISIBLE);
        fabCancel.setVisibility(View.INVISIBLE);
        text_origen = (TextView) findViewById(R.id.textView5);
        materialesSpinner = (Spinner) findViewById(R.id.spinnerMateriales);
        origenesSpinner = (Spinner) findViewById(R.id.spinnerOrigenes);

        pago = (CheckBox) findViewById(R.id.pagoCheck);
        mina = (TextInputLayout) findViewById(R.id.textomina);
        vol = (TextInputLayout) findViewById(R.id.vol);
        seg = (TextInputLayout) findViewById(R.id.seg);
        ded = (TextInputLayout) findViewById(R.id.textodeductiva);
        vale_mina = (TextView)findViewById(R.id.vale_mina);
        seguimiento = (TextView) findViewById(R.id.seguimiento);
        volumen = (TextView) findViewById(R.id.volumen);
        deductiva = (TextView) findViewById(R.id.deductiva);
        textmotivo = (TextView) findViewById(R.id.textViewMotivo);
        motivos = (Spinner) findViewById(R.id.spinnerMotivo);
        mina.setVisibility(View.GONE);
        seg.setVisibility(View.GONE);
        vol.setVisibility(View.GONE);

        deductiva.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             textmotivo.setVisibility(View.VISIBLE);
                                             motivos.setVisibility(View.VISIBLE);
                                         }
                                     }

        );


        tipo = 1;
            final ArrayList<String> descripcionesOrigenes = origen.getArrayListDescripciones();
            final ArrayList <String> idsOrigenes = origen.getArrayListId();

            final String[] spinnerOrigenesArray = new String[idsOrigenes.size()];
            final HashMap<String, String> spinnerOrigenesMap = new HashMap<>();

            for (int i = 0; i < idsOrigenes.size(); i++) {
                spinnerOrigenesMap.put(descripcionesOrigenes.get(i), idsOrigenes.get(i));
                spinnerOrigenesArray[i] = descripcionesOrigenes.get(i);
               // idOrigen = usuario.idorigen; // origen o tiro asignado
            }

            final ArrayAdapter<String> arrayAdapterOrigenes = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item, spinnerOrigenesArray);
            arrayAdapterOrigenes.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            origenesSpinner.setAdapter(arrayAdapterOrigenes);

            origenesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String descripcion = origenesSpinner.getSelectedItem().toString();
                    if(descripcion == "0") {
                        descripcion = "NO SE ENCUENTRAN ORIGENES";
                        idOrigen = 0;
                    }else{
                        idOrigen = Integer.valueOf(spinnerOrigenesMap.get(descripcion));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

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
        if(usuario.tipo_permiso == 1) {
            pago.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                    if (isChecked) {
                        mina.setVisibility(View.VISIBLE);
                        seg.setVisibility(View.VISIBLE);
                        vol.setVisibility(View.VISIBLE);
                    } else {
                        vale_mina.setText(null);
                        seguimiento.setText(null);
                        volumen.setText(null);
                        mina.setVisibility(View.GONE);
                        seg.setVisibility(View.GONE);
                        vol.setVisibility(View.GONE);
                    }
                    ded.setVisibility(View.GONE);
                    deductiva.setText(null);
                }
            });
        }
        else if(usuario.tipo_permiso == 4) {
            ded.setVisibility(View.VISIBLE);
            pago.setVisibility(View.GONE);
        }
        else{
            pago.setVisibility(View.GONE);
        }

        escribirOrigenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idMaterial == 0) {
                    Toast.makeText(getApplicationContext(), "Por favor seleccione un Material de la lista", Toast.LENGTH_LONG).show();
                    materialesSpinner.requestFocus();
                } else if(idOrigen == 0) {
                        Toast.makeText(getApplicationContext(), "Por favor seleccione un Origen de la lista", Toast.LENGTH_LONG).show();
                        origenesSpinner.requestFocus();
                }
                else if (( deductiva.getText().toString().equals("")==false ) && idMotivo == 0){
                    Toast.makeText(getApplicationContext(), "Por favor seleccione un motivo", Toast.LENGTH_SHORT).show();
                }
                else if(pago.isChecked() && vale_mina.getText().toString().isEmpty()){
                        Toast.makeText(getApplicationContext(), "Por favor escribir el folio del vale de mina", Toast.LENGTH_SHORT).show();
                }
                else if(pago.isChecked() && seguimiento.getText().toString().isEmpty()){
                        Toast.makeText(getApplicationContext(), "Por favor escribir el folio de seguimiento de material", Toast.LENGTH_SHORT).show();
                }
                else if(pago.isChecked() && volumen.getText().toString().isEmpty()){
                        Toast.makeText(getApplicationContext(), "Por favor escribir el volumen del material", Toast.LENGTH_SHORT).show();
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

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteModeOff();
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String UID="";
        Usuario u = new Usuario(getApplicationContext());
        u = u.getUsuario();
        int tipo=0;
        if(writeMode) {
            if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
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
                if (TagModel.findTAG (getApplicationContext(), UID)) {
                    String data = Util.concatenar(String.valueOf(idMaterial), String.valueOf(idOrigen));
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    boolean datos = false;
                    boolean dia = false;
                    boolean uss = false;
                    boolean tipo_suministro = false;
                    String camion = null;
                    String fecha = null;
                    String idusuario = null;
                    String user = String.valueOf(u.getId());
                    Integer idcamion = 0;
                    Integer idproyecto = 0;
                    String camion_proyecto;
                    String dataTime = Util.getFechaHora();
                    Integer tipo_s = 0;
                    if (tipo == 1) {
                        datos = nfcTag.writeSector(myTag, 1, 4, data);
                        dia = nfcTag.writeSector(myTag, 1, 5, dataTime);
                        uss = nfcTag.writeSector(myTag, 1, 6, user);
                        camion_proyecto = nfcTag.readSector(myTag, 0, 1);
                        idcamion = Util.getIdCamion(camion_proyecto);
                        idproyecto = Util.getIdProyecto(camion_proyecto);
                        camion = nfcTag.readSector(myTag, 1, 4);
                        fecha = nfcTag.readSector(myTag, 1, 5);
                        idusuario = nfcTag.readSector(myTag, 1, 6);
                        if(pago.isChecked()){
                            tipo_s = 1;
                        }else {
                            tipo_s = 0;
                        }
                        tipo_suministro = nfcTag.writeSector(myTag,2,9,String.valueOf(tipo_s));
                    }
                    if (tipo == 2) {
                        datos = nfcUltra.writePagina(myTag, 7, data);
                        dia = nfcUltra.writePagina(myTag, 9, dataTime);
                        uss = nfcUltra.writePagina(myTag, 13, user);
                        camion_proyecto = nfcUltra.readConfirmar(myTag, 4) + nfcUltra.readConfirmar(myTag, 5);
                        idcamion = Util.getIdCamion(camion_proyecto);
                        idproyecto = Util.getIdProyecto(camion_proyecto);
                        camion = nfcUltra.readConfirmar(myTag, 7) + nfcUltra.readConfirmar(myTag, 8);
                        fecha = nfcUltra.readConfirmar(myTag, 9) + nfcUltra.readConfirmar(myTag, 10) + nfcUltra.readConfirmar(myTag, 11) + nfcUltra.readConfirmar(myTag, 12).substring(0, 2);
                        idusuario = nfcUltra.readConfirmar(myTag, 13) + nfcUltra.readConfirmar(myTag, 14);
                        if(pago.isChecked()){
                            tipo_s = 1;
                        }else {
                            tipo_s = 0;
                        }
                        tipo_suministro = nfcUltra.writePagina(myTag, 15, String.valueOf(tipo_s));
                    }
                    Camion datosCamion = new Camion(getApplicationContext());
                    datosCamion = datosCamion.find(idcamion);
                    if (datosCamion.estatus == 1) {
                        if (idproyecto == usuario.getProyecto()) {
                            if (data.equals(camion.replace(" ", "")) && dataTime.equals(fecha.replace(" ", "")) && user.equals(idusuario.replace(" ", ""))) {

                                ContentValues cv = new ContentValues();
                                cv.put("IMEI", IMEI);
                                cv.put("idevento", 2);
                                cv.put("latitud", latitude);
                                cv.put("longitud", longitude);
                                cv.put("fecha_hora", Util.timeStamp());
                                cv.put("code", "");


                                Coordenada coordenada = new Coordenada(getApplicationContext());
                                coordenada.create(cv, getApplicationContext());

                                cv.clear();
                                cv.put("idcamion", idcamion);
                                cv.put("idmaterial", idMaterial);
                                cv.put("idorigen", idOrigen);
                                cv.put("fecha_origen", Util.getFormatDate(dataTime));
                                cv.put("idusuario", user);
                                cv.put("uidTAG", UID);
                                cv.put("IMEI", IMEI);
                                cv.put("estatus", 1);
                                cv.put("tipoEsquema", usuario.getTipoEsquema());
                                cv.put("idperfil", usuario.tipo_permiso);

                                cv.put("folio_mina", vale_mina.getText().toString());
                                cv.put("folio_seguimiento", seguimiento.getText().toString());
                                cv.put("volumen", volumen.getText().toString());
                                cv.put("tipo_suministro", tipo_s);
                                if(tipo_s == 1) {
                                    cv.put("Code", Util.folio(Util.dateFolios()) + String.valueOf(idcamion));
                                }
                                if (deductiva.getText().toString().equals("")) {
                                    cv.put("deductiva", 0);
                                    cv.put("idMotivo", 0);
                                } else {
                                    cv.put("deductiva", deductiva.getText().toString());
                                    cv.put("idMotivo", idMotivo);
                                }
                                InicioViaje in = new InicioViaje(getApplicationContext());
                                Boolean guardar = in.create(cv);

                                if (guardar) {
                                    Intent success = new Intent(getApplicationContext(), SuccessDestinoActivity.class);
                                    success.putExtra("idInicio", in.id);
                                    startActivity(success);
                                }

                            } else {
                                Toast.makeText(SetOrigenActivity.this, getString(R.string.error_tag_comunicacion), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(SetOrigenActivity.this, getString(R.string.error_proyecto), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(),"El camión "+datosCamion.economico+" se encuentra inactivo. Por favor contacta al encargado.", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(SetOrigenActivity.this, getString(R.string.error_tag_inexistente), Toast.LENGTH_LONG).show();
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
            finish();
        } else {
            finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
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
            new AlertDialog.Builder(SetOrigenActivity.this)
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                                    progressDialogSync = ProgressDialog.show(SetOrigenActivity.this, "Sincronizando datos", "Por favor espere...", true);
                                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);
                                    tiempoEsperaSincronizacion();
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
                new AlertDialog.Builder(SetOrigenActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(SetOrigenActivity.this, "Sincronizando datos", "Por favor espere...", true);
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

    public  void tiempoEsperaSincronizacion(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
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
            }
        }, 8000);
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
