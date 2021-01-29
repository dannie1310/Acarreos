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
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import mx.grupohi.acarreos.Mina.SalidaMina;
import mx.grupohi.acarreos.TiposTag.TagNFC;

public class SetOrigenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //Objetos
    private Usuario usuario;
    private Material material;
    private Origen origen;
    private Camion c;

    //Variables
    private Integer idMaterial;
    private Integer idOrigen;

    //Referencias UI
    Spinner materialesSpinner;
    private Spinner origenesSpinner;
    private Button escribirOrigenButton;
    private LinearLayout mainLayout;
    private LinearLayout lecturaTag;
    private ImageView nfcImage;
    private FloatingActionButton fabCancel;
    private TextView tagAlertTextView;
    private TextView text_origen;
    private ProgressDialog progressDialogSync;
    private TextView vale_mina;
    private TextView seguimiento;
    private TextView deductiva;
    private TextInputLayout mina;
    private TextInputLayout seg;
    private TextInputLayout ded;
    private String mensaje = "";
    private ContentValues datosVista;
    private Integer IdInicio;

    //GPS
    private GPSTracker gps;
    Double latitud;
    Double longitud;
    AlertDialog alerta = null;
    private String IMEI = "N/A";

    //NFC
    private NFCTag nfcTag;
    private NFCUltralight nfcUltra;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private Boolean writeMode;
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

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

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
        if(phneMgr.getDeviceId() != null){
            IMEI = phneMgr.getDeviceId();
        }

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
        lecturaTag = (LinearLayout) findViewById(R.id.leerTag);
        tagAlertTextView =(TextView) findViewById(R.id.textViewMensaje);

        text_origen = (TextView) findViewById(R.id.textView5);
        materialesSpinner = (Spinner) findViewById(R.id.spinnerMateriales);
        origenesSpinner = (Spinner) findViewById(R.id.spinnerOrigenes);

        mina = (TextInputLayout) findViewById(R.id.textomina);
        seg = (TextInputLayout) findViewById(R.id.seg);
        ded = (TextInputLayout) findViewById(R.id.textodeductiva);
        vale_mina = (TextView)findViewById(R.id.vale_mina);
        seguimiento = (TextView) findViewById(R.id.seguimiento);
        deductiva = (TextView) findViewById(R.id.deductiva);
        //inicializa contentvalue para registrar datos a DB
        datosVista = new ContentValues();
        lecturaTag.setVisibility(View.GONE);

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
        Crashlytics.setUserEmail("IMEI:"+IMEI);
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
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertNoGps();
        }

        escribirOrigenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
                    AlertNoGps();
                }else {
                    latitud = gps.getLatitude();
                    longitud = gps.getLongitude();

                    if(latitud.toString() != "0.0" || longitud.toString() != "0.0") {
                        if (!validarCampos()) {
                            /// imprime valiable mensaje en toast o alert
                            alert(mensaje);
                        } else {
                            checkNfcEnabled();
                            WriteModeOn();
                        }
                    }else{
                        alert("ESPERE, NO SE DETECTA SU UBICACION");
                    }
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
                            TextView tim = (TextView) child.findViewById(R.id.textViewImpresora);

                            Integer impresora = CelularImpresora.getId(getApplicationContext());
                            if (tim != null) {
                                if (impresora == 0) {
                                    tim.setTextColor(Color.RED);
                                    tim.setText("Sin Impresora Asignada");
                                } else {
                                    tim.setText("Impresora " + impresora);
                                }
                            }
                            if (tvp != null) {
                                tvp.setText(usuario.descripcionBaseDatos);
                            }
                            if (tvu != null) {
                                tvu.setText(usuario.nombre);
                            }
                            if (tpe != null) {
                                if (usuario.origen_name == "0") {
                                    tpe.setText("PERFIL: " + usuario.getNombreEsquema() + " - " + usuario.tiro_name);
                                } else if (usuario.tiro_name == "0") {
                                    tpe.setText("PERFIL: " + usuario.getNombreEsquema() + " - " + usuario.origen_name);
                                }
                            }
                            if (tvv != null) {
                                tvv.setText(getString(R.string.app_name) + "     " + "Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                            }
                        }
                    }
                });
    }

    private Boolean validarCampos(){
        /// validar material
        if(idMaterial == 0) {
            mensaje = "Por favor seleccione un Material de la lista";
            materialesSpinner.requestFocus();
            return false;
        }
        /// validar origen
        if(idOrigen == 0){
            mensaje = "Por favor seleccione un Origen de la lista";
            origenesSpinner.requestFocus();
            return false;
        }
        /// validar folio mina
        if(vale_mina.getText().toString().isEmpty()){
            mensaje = "Por favor escribir el folio de mina";
            vale_mina.requestFocus();
            return false;
        }
        if(foliosSinCeros(vale_mina.getText().toString())){
            mensaje = "Por favor ingrese un folio de mina valido (No es permitido usar únicamente ceros).";
            vale_mina.requestFocus();
            return false;
        }
        /// validar folio seguimiento
        if(seguimiento.getText().toString().isEmpty()){
            mensaje = "Por favor escribir el folio de seguimiento de material";
            seguimiento.requestFocus();
            return false;
        }
        if(foliosSinCeros(seguimiento.getText().toString())){
            mensaje = "Por favor ingrese un folio de seguimiento valido (No es permitido usar únicamente ceros).";
            seguimiento.requestFocus();
            return false;
        }
        /// validar volumen
        if(deductiva.getText().toString().isEmpty()){
            mensaje = "Por favor escribir el volumen";
            deductiva.requestFocus();
            return false;
        }
        if(foliosSinCeros(deductiva.getText().toString())){
            mensaje = "El volumen no puede ser cero.";
            deductiva.requestFocus();
            return false;
        }

        /// asignacion de valores
        datosVista.put("idmaterial", idMaterial);
        datosVista.put("idorigen", idOrigen);
        datosVista.put("folio_mina", vale_mina.getText().toString());
        datosVista.put("folio_seguimiento", seguimiento.getText().toString());
        datosVista.put("idusuario", String.valueOf(usuario.getId()));
        datosVista.put("IMEI", IMEI);
        return true;
    }

    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Debe activar el GPS")
                .setCancelable(false)
                .setPositiveButton("ACTIVAR", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        alerta = builder.create();
        alerta.show();
    }

    class SalidaMinaTarea extends AsyncTask<Void, Void, Boolean> {
        TagNFC tag_nfc = new TagNFC();
        Context context;
        Intent intent;

        public SalidaMinaTarea(Context context, Intent intent) {
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
            IdInicio = 0;
            //// se lee el tag y se inicializa la clase con los datos
            if(writeMode){
                if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                    myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    String[] techs = myTag.getTechList();
                    for (String t : techs) {
                        if (MifareClassic.class.getName().equals(t)) {
                            nfcTag = new NFCTag(myTag, context);
                            tag_nfc.setUID(nfcTag.byteArrayToHexString(myTag.getId()));
                            tag_nfc.setTipo(1);
                            String camion_proyecto = null;
                            String material_origen = null;
                            try {
                                camion_proyecto = nfcTag.readSector(null, 0, 1);
                                material_origen = nfcTag.readSector(null,1, 4);
                                tag_nfc.setFecha(nfcTag.readSector(null,1,5));
                                tag_nfc.setUsuario(nfcTag.readSector(null,1,6));
                                tag_nfc.setTipo_viaje(nfcTag.readSector(null,2,9));
                                tag_nfc.setVolumen(nfcTag.readSector(null,3,12));
                                tag_nfc.setTipo_perfil(nfcTag.readSector(null,3,14));
                                tag_nfc.setVolumen_entrada(nfcTag.readSector(null,4,16));

                            } catch (IOException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            if (camion_proyecto.length() == 8) {
                                tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 4));
                                tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 4));
                            } else {
                                tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 5));
                                tag_nfc.setIdproyecto( Util.getIdProyecto(camion_proyecto, 5));
                            }
                            tag_nfc.setIdmaterial(String.valueOf(Util.getIdMaterial(material_origen)));
                            tag_nfc.setIdorigen(String.valueOf(Util.getIdOrigen(material_origen)));
                        } else if (MifareUltralight.class.getName().equals(t)) {
                            nfcUltra = new NFCUltralight(myTag, context);
                            tag_nfc.setUID(nfcUltra.byteArrayToHexString(myTag.getId()));
                            tag_nfc.setTipo(2);
                            String camion_proyecto = null;
                            try {
                                camion_proyecto = nfcUltra.readDeductiva(null, 4)+nfcUltra.readDeductiva(null, 5)+nfcUltra.readDeductiva(null, 6);
                                tag_nfc.setIdmaterial(nfcUltra.readDeductiva(null,7));
                                tag_nfc.setIdorigen(nfcUltra.readDeductiva(null,8));
                                tag_nfc.setFecha(nfcUltra.readDeductiva(null,9)+nfcUltra.readDeductiva(null,10)+nfcUltra.readDeductiva(null,11)+ nfcUltra.readDeductiva(null,12));
                                tag_nfc.setUsuario(nfcUltra.readDeductiva(null,13));
                                tag_nfc.setTipo_viaje(nfcUltra.readDeductiva(null,15));
                                tag_nfc.setVolumen(nfcUltra.readDeductiva(null,16));
                                tag_nfc.setTipo_perfil(nfcUltra.readDeductiva(null,18));
                                tag_nfc.setVolumen_entrada(nfcUltra.readDeductiva(null,19));

                            } catch (IOException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            if (camion_proyecto.length() == 8) {
                                tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 4));
                                tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 4));
                            } else {
                                tag_nfc.setIdcamion(Util.getIdCamion(camion_proyecto, 8));
                                tag_nfc.setIdproyecto(Util.getIdProyecto(camion_proyecto, 8));
                            }
                        }
                        tag_nfc.setLatitud_origen(latitud.toString());
                        tag_nfc.setLongitud_origen(longitud.toString());
                    }
                }
            }

            //// inicia seccion de validaciones y escritura de dtos en DB

            SalidaMina salida_mina = new SalidaMina(context, tag_nfc);
            mensaje = salida_mina.validarDatosTag(Integer.valueOf(deductiva.getText().toString()));
            if(mensaje == "continuar"){
                /// aqui ya se valido que no haya viajes pendientes en el tag, lo que procede es:
                ///  1) crear el content values con los datos extra que debe de llevar el tag
                ///     con el content valaes generado se complementa con lo que esta en la clase POJO para
                ///        generar el paquete de insercion a BBD
                ///  2) insertar los datos al tag
                datosVista.put("idcamion", tag_nfc.getIdcamion());
                datosVista.put("fecha_origen", Util.getFormatDate(Util.getFechaHora()));
                datosVista.put("uidTAG", tag_nfc.getUID());
                datosVista.put("estatus", 1);
                datosVista.put("tipoEsquema", usuario.getTipoEsquema());
                datosVista.put("idperfil", usuario.tipo_permiso);
                datosVista.put("deductiva", deductiva.getText().toString());
                datosVista.put("deductiva_entrada", 0);
                datosVista.put("idMotivo", tag_nfc.getIdmotivo());
                datosVista.put("numImpresion", 0);
                datosVista.put("deductiva_entrada",0);
                if(salida_mina.tipoviaje()){
                    datosVista.put("tipo_suministro", 1);
                    datosVista.put("Code", Util.folio(Util.dateFolios()) + String.valueOf(tag_nfc.getIdcamion()));
                }else{
                    datosVista.put("tipo_suministro", 0);
                }

                if(!tag_nfc.getLatitud_origen().equals("")) {
                    datosVista.put("latitud_origen", tag_nfc.getLatitud_origen());
                }else{
                    datosVista.put("latitud_origen", "NULL");
                }
                if(!tag_nfc.getLongitud_origen().equals("")) {
                    datosVista.put("longitud_origen", tag_nfc.getLongitud_origen());
                }else{
                    datosVista.put("longitud_origen", "NULL");
                }

                if(!salida_mina.guardarDatosDB(datosVista)){
                    mensaje = "Error al guardar en Base de Datos";
                    return false;
                }else {
                    IdInicio = salida_mina.idInicio;
                    salida_mina.coordenadas(datosVista.getAsString("IMEI"),datosVista.getAsString("Code"), latitud, longitud);
                    //guardar datos en el tag
                    myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    String data = Util.concatenar(datosVista.getAsString("idmaterial"), datosVista.getAsString("idorigen"));
                    if (tag_nfc.getTipo() == 1) {
                        if (tag_nfc.getUID().equals(nfcTag.byteArrayToHexString(myTag.getId()))) {
                            try {
                                nfcTag.writeSector(null, 1, 4, data);
                                nfcTag.writeSector(null, 1, 5, Util.getFechaTag(datosVista.getAsString("fecha_origen")));
                                nfcTag.writeSector(null, 1, 6, datosVista.getAsString("idusuario"));
                                nfcTag.writeSector(null, 2, 9, datosVista.getAsString("tipo_suministro"));
                                nfcTag.writeSector(null, 3, 12, datosVista.getAsString("deductiva"));
                                nfcTag.writeSector(null, 3, 13, tag_nfc.getIdmotivo());
                                nfcTag.writeSector(null, 3, 14, datosVista.getAsString("tipo_suministro"));
                                nfcTag.writeSector(null, 5, 20, tag_nfc.getLatitud_origen());
                                nfcTag.writeSector(null, 5, 21, tag_nfc.getLongitud_origen());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            return true;
                        } else {
                            mensaje = "¡Error! Utilice el mismo tag.";
                            return false;
                        }
                    }
                    if (tag_nfc.getTipo() == 2) {
                        if (tag_nfc.getUID().equals(nfcUltra.byteArrayToHexString(myTag.getId()))) {
                            try {
                                nfcUltra.writePagina(null, 7, data);
                                nfcUltra.writePagina(null, 9, Util.getFechaTag(datosVista.getAsString("fecha_origen")));
                                nfcUltra.writePagina(null, 13, datosVista.getAsString("idusuario"));
                                nfcUltra.writePagina(null, 15, datosVista.getAsString("tipo_suministro"));
                                nfcUltra.writePagina(null, 16, datosVista.getAsString("deductiva"));
                                nfcUltra.writePagina(null, 17, tag_nfc.getIdmotivo());
                                nfcUltra.writePagina(null, 18, datosVista.getAsString("tipo_suministro"));
                                nfcUltra.writePagina(null, 21, tag_nfc.getLatitud_origen());
                                nfcUltra.writePagina(null, 24, tag_nfc.getLongitud_origen());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            return true;
                        } else {
                            mensaje = "¡Error! Utilice el mismo tag.";
                            return false;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean registro) {
            super.onPostExecute(registro);
            WriteModeOff();
            if (registro){
                Intent success = new Intent(getApplicationContext(), SuccessDestinoActivity.class);
                success.putExtra("idInicio", IdInicio);
                startActivity(success);
            }else{
                if(mensaje == "volumen_entrada") {
                    Intent r = new Intent(getApplicationContext(), MensajeEntradaActivity.class);
                    r.putExtra("datos", tag_nfc);// enviar el POJO TagNFC
                    startActivity(r);
                }else {
                    if(!IdInicio.equals(0)){
                        SalidaMina salidaMina = new SalidaMina(context, tag_nfc);
                        while(!salidaMina.rollbackDB(IdInicio)){
                            mensaje = "intentar nuevamente";
                        }
                        mensaje = "Manten el TAG más tiempo! " + mensaje;
                    }
                    alert(mensaje);
                }
            }
        }
    }

    public void alert(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SetOrigenActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {

                    }
                }).show();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        new SalidaMinaTarea(this,  intent).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNfcEnabled();
      //  WriteModeOff();
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        mainLayout.setVisibility(View.GONE);
        lecturaTag.setVisibility(View.VISIBLE);
    }

    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
        mainLayout.setVisibility(View.VISIBLE);
        lecturaTag.setVisibility(View.GONE);
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

    private Boolean foliosSinCeros(String folio){
        if(folio.replace("0","").trim().equals("")){
            return true;
        }
        return false;
    }
}
