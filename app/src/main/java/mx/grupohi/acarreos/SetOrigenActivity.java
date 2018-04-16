package mx.grupohi.acarreos;

import android.Manifest;
import android.app.Activity;
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
import android.os.Handler;
import android.preference.DialogPreference;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    String[] spinnerMotivosA = null;

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
    private Snackbar snackbar;
    private TextView textmotivo;
    private Integer idMotivo = 0;
    private Integer id_motivo = 0;
    private String mensaje = "";
    private String txtDeductiva = "";
    private HashMap<String, String> spinnerMotivosMap;
    private Spinner motivos;
    private ContentValues datosVista;
    private Integer IdInicio;

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

        escribirOrigenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validarCampos()){
                    /// imprime valiable mensaje en toast o alert
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
                else {
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

    class SalidaMinaTarea extends AsyncTask<Void, Void, Boolean> {
        TagNFC tag_nfc = new TagNFC();
        Context context;
        Intent intent;
        String mensaje_error = "";
        Double latitud = gps.getLatitude();
        Double longitud = gps.getLongitude();

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
                            try {
                                camion_proyecto = nfcTag.readSector(null, 0, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
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
                            String material_origen = null;
                            try {
                                material_origen = nfcTag.readSector(null,1, 4);
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            tag_nfc.setIdmaterial(String.valueOf(Util.getIdMaterial(material_origen)));
                            tag_nfc.setIdorigen(String.valueOf(Util.getIdOrigen(material_origen)));
                            try {
                                tag_nfc.setFecha(nfcTag.readSector(null,1,5));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setUsuario(nfcTag.readSector(null,1,6));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setTipo_viaje(nfcTag.readSector(null,2,9));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setVolumen(nfcTag.readSector(null,3,12));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setTipo_perfil(nfcTag.readSector(null,3,14));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setVolumen_entrada(nfcTag.readSector(null,4,16));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }

                        } else if (MifareUltralight.class.getName().equals(t)) {
                            nfcUltra = new NFCUltralight(myTag, context);
                            tag_nfc.setUID(nfcUltra.byteArrayToHexString(myTag.getId()));
                            tag_nfc.setTipo(2);
                            String camion_proyecto = null;
                            try {
                                camion_proyecto = nfcUltra.readDeductiva(null, 4)+nfcUltra.readDeductiva(null, 5)+nfcUltra.readDeductiva(null, 6);
                            } catch (IOException e) {
                                e.printStackTrace();
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
                            try {
                                tag_nfc.setIdmaterial(nfcUltra.readDeductiva(null,7));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setIdorigen(nfcUltra.readDeductiva(null,8));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setFecha(nfcUltra.readDeductiva(null,9)+nfcUltra.readDeductiva(null,10)+nfcUltra.readDeductiva(null,11)+ nfcUltra.readDeductiva(null,12));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setUsuario(nfcUltra.readDeductiva(null,13));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setTipo_viaje(nfcUltra.readDeductiva(null,15));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                tag_nfc.setVolumen(nfcUltra.readDeductiva(null,16));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setTipo_perfil(nfcUltra.readDeductiva(null,18));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                tag_nfc.setVolumen_entrada(nfcUltra.readDeductiva(null,19));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                        }
                    }
                }
            }

            //// inicia seccion de validaciones y escritura de dtos en DB

            SalidaMina salida_mina = new SalidaMina(context, tag_nfc);
            mensaje = salida_mina.validarDatosTag();
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
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcTag.writeSector(null, 1, 5, Util.getFechaTag(datosVista.getAsString("fecha_origen")));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcTag.writeSector(null, 1, 6, datosVista.getAsString("idusuario"));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcTag.writeSector(null, 2, 9, datosVista.getAsString("tipo_suministro"));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcTag.writeSector(null, 3, 12, datosVista.getAsString("deductiva"));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcTag.writeSector(null, 3, 13, tag_nfc.getIdmotivo());
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcTag.writeSector(null, 3, 14, datosVista.getAsString("tipo_suministro"));
                            } catch (IOException e) {
                                e.printStackTrace();
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
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcUltra.writePagina(null, 9, Util.getFechaTag(datosVista.getAsString("fecha_origen")));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcUltra.writePagina(null, 13, datosVista.getAsString("idusuario"));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcUltra.writePagina(null, 15, datosVista.getAsString("tipo_suministro"));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcUltra.writePagina(null, 16, datosVista.getAsString("deductiva"));
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcUltra.writePagina(null, 17, tag_nfc.getIdmotivo());
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcUltra.writePagina(null, 18, datosVista.getAsString("tipo_suministro"));
                            } catch (IOException e) {
                                e.printStackTrace();
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
            if (registro){
                WriteModeOff();
                Intent success = new Intent(getApplicationContext(), SuccessDestinoActivity.class);
                success.putExtra("idInicio", IdInicio);
                startActivity(success);
            }else{
                if(mensaje == "volumen_entrada") {
                   // mensajeDeductiva(intent, tag_nfc); // usar el mensaje deductiva despues....
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
                }else {
                    WriteModeOff();
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {

        new SalidaMinaTarea(this,  intent).execute();





















      /*  String UID="";
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
                    if (String.valueOf(idMaterial).length() < 5 || String.valueOf(idOrigen).length() < 5) {
                        String data = Util.concatenar(String.valueOf(idMaterial), String.valueOf(idOrigen));
                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();
                        boolean datos = false;
                        boolean dia = false;
                        boolean uss = false;
                        boolean tipo_suministro = false;
                        Boolean deductiva_check = false;
                        String camion = null;
                        String fecha = null;
                        String idusuario = null;
                        String user = String.valueOf(u.getId());
                        Integer idcamion = 0;
                        Integer idproyecto = 0;
                        String camion_proyecto;
                        Integer tipoperfil=0;
                        Integer banderaPermisos=0;
                        String dataTime = Util.getFechaHora();
                        Integer id_motivo = 0;
                        Integer tipo_s = 0;
                        if (tipo == 1) {
                            camion_proyecto = nfcTag.readSector(myTag, 0, 1).replace(" ", "");
                            if (camion_proyecto.length() == 8) {
                                idcamion = Util.getIdCamion(camion_proyecto, 4);
                                idproyecto = Util.getIdProyecto(camion_proyecto, 4);
                            } else {
                                idcamion = Util.getIdCamion(camion_proyecto, 5);
                                idproyecto = Util.getIdProyecto(camion_proyecto, 5);
                            }

                            String aux =nfcTag.readSector(myTag, 3, 14).replace(" ","");

                            if(aux != "") {
                                tipoperfil = Integer.valueOf(nfcTag.readSector(myTag, 3, 14).replace(" ", ""));
                            }

                            if(!txtDeductiva.equals("")){
                                nfcTag.writeSector(myTag, 4, 16, txtDeductiva);
                                nfcTag.writeSector(myTag, 4, 17, id_motivo.toString());
                                camion = nfcTag.readSector(myTag, 1, 4);
                                fecha = nfcTag.readSector(myTag, 1, 5);
                                idusuario = nfcTag.readSector(myTag, 1, 6);
                                idMaterial = Util.getIdMaterial(camion);
                                idOrigen = Util.getIdOrigen(camion);
                                dataTime =fecha.replace(" ", "");
                                tipo_s = 1;
                                banderaPermisos = 0;
                                if(!nfcTag.readSector(myTag, 4, 16).equals("") && !nfcTag.readSector(myTag, 4, 17).equals("")){
                                    deductiva_check = true;
                                }
                            }else {
                                if (usuario.tipo_permiso == 1 && tipoperfil == 1 || usuario.tipo_permiso == 4 && tipoperfil == 0 || usuario.tipo_permiso == 1 && tipoperfil == 0) {
                                    camion = nfcTag.readSector(myTag, 1, 4);
                                    fecha = nfcTag.readSector(myTag, 1, 5);
                                    camion = camion.replace(" ", "");
                                    fecha = fecha.replace(" ", "");
                                    if((camion == null && fecha == null) || (camion == "" && fecha== "")) {
                                        if (usuario.tipo_permiso == 1) {
                                            tipo_s = 1;
                                        } else {
                                            tipo_s = 0;
                                        }

                                        datos = nfcTag.writeSector(myTag, 1, 4, data);
                                        dia = nfcTag.writeSector(myTag, 1, 5, dataTime);
                                        uss = nfcTag.writeSector(myTag, 1, 6, user);
                                        tipo_suministro = nfcTag.writeSector(myTag, 2, 9, String.valueOf(tipo_s));
                                        camion = nfcTag.readSector(myTag, 1, 4);
                                        fecha = nfcTag.readSector(myTag, 1, 5);
                                        idusuario = nfcTag.readSector(myTag, 1, 6);
                                        if (deductiva.getText().toString().equals("")) {
                                            nfcTag.writeSector(myTag, 3, 12, "0");
                                            nfcTag.writeSector(myTag, 3, 13, "0");
                                        } else {
                                            nfcTag.writeSector(myTag, 3, 12, String.valueOf(deductiva.getText()));
                                            nfcTag.writeSector(myTag, 3, 13, String.valueOf(idMotivo));
                                        }
                                        if (usuario.tipo_permiso == 1) {
                                            nfcTag.writeSector(myTag, 3, 14, "1");
                                        } else {
                                            nfcTag.writeSector(myTag, 3, 14, "0");
                                        }
                                    }else{
                                        banderaPermisos = 2;
                                    }

                                } else {
                                    banderaPermisos = 1;
                                }
                            }
                        }
                        if (tipo == 2) {
                            camion_proyecto = (nfcUltra.readConfirmar(myTag, 4) + nfcUltra.readConfirmar(myTag, 5) + nfcUltra.readConfirmar(myTag, 6)).replace(" ", "").replace("null","");
                            if (camion_proyecto.length() == 8) {
                                idcamion = Util.getIdCamion(camion_proyecto, 4);
                                idproyecto = Util.getIdProyecto(camion_proyecto, 4);
                            } else {
                                idcamion = Util.getIdCamion(camion_proyecto, 8);
                                idproyecto = Util.getIdProyecto(camion_proyecto, 8);
                            }
                            tipoperfil =0;
                            String aux =nfcUltra.readDeductiva(myTag, 18);
                            if(aux != "") {
                                tipoperfil = Integer.valueOf(nfcUltra.readDeductiva(myTag, 18));
                            }

                            if(!txtDeductiva.equals("")){
                                nfcUltra.writePagina(myTag,  19, txtDeductiva);
                                nfcUltra.writePagina(myTag,  20, id_motivo.toString());
                                tipo_s = 1;
                                banderaPermisos = 0;
                                camion = nfcUltra.readConfirmar(myTag, 7) + nfcUltra.readConfirmar(myTag, 8);
                                fecha = nfcUltra.readConfirmar(myTag, 9) + nfcUltra.readConfirmar(myTag, 10) + nfcUltra.readConfirmar(myTag, 11) + nfcUltra.readConfirmar(myTag, 12).substring(0, 2);
                                idusuario = nfcUltra.readConfirmar(myTag, 13) + nfcUltra.readConfirmar(myTag, 14);
                                idMaterial = Util.getIdMaterial(camion);
                                idOrigen = Util.getIdOrigen(camion);
                                dataTime =fecha.replace(" ", "");
                                if(!nfcUltra.readConfirmar(myTag, 19).equals("") && !nfcUltra.readConfirmar(myTag, 20).equals("")){
                                    deductiva_check = true;
                                }
                            }else {

                                if (usuario.tipo_permiso == 1 && tipoperfil == 1 || usuario.tipo_permiso == 4 && tipoperfil == 0 || usuario.tipo_permiso == 1 && tipoperfil == 0) {
                                    camion = nfcUltra.readConfirmar(myTag, 7) + nfcUltra.readConfirmar(myTag, 8);
                                    fecha = nfcUltra.readConfirmar(myTag, 9) + nfcUltra.readConfirmar(myTag, 10) + nfcUltra.readConfirmar(myTag, 11) + nfcUltra.readConfirmar(myTag, 12);

                                    if(camion == null && fecha == null || camion.replace(" ","").equals("") && fecha.replace(" ","").equals("")) {
                                        if (usuario.tipo_permiso == 1) { // validar si el viaje es por suministro
                                            tipo_s = 1;
                                        } else { // viaje suministro + flete a sindicato
                                            tipo_s = 0;
                                        }
                                        datos = nfcUltra.writePagina(myTag, 7, data);
                                        dia = nfcUltra.writePagina(myTag, 9, dataTime);
                                        uss = nfcUltra.writePagina(myTag, 13, user);
                                        tipo_suministro = nfcUltra.writePagina(myTag, 15, String.valueOf(tipo_s));
                                        camion = nfcUltra.readConfirmar(myTag, 7) + nfcUltra.readConfirmar(myTag, 8);
                                        fecha = nfcUltra.readConfirmar(myTag, 9) + nfcUltra.readConfirmar(myTag, 10) + nfcUltra.readConfirmar(myTag, 11) + nfcUltra.readConfirmar(myTag, 12).substring(0, 2);
                                        idusuario = nfcUltra.readConfirmar(myTag, 13) + nfcUltra.readConfirmar(myTag, 14);
                                        if (deductiva.getText().toString().equals("")) {
                                            nfcUltra.writePagina(myTag, 16, "0");
                                            nfcUltra.writePagina(myTag, 17, "0");
                                        } else {
                                            nfcUltra.writePagina(myTag, 16, String.valueOf(deductiva.getText()));
                                            nfcUltra.writePagina(myTag, 17, String.valueOf(idMotivo));
                                        }
                                        if (usuario.tipo_permiso == 1) {
                                            nfcUltra.writePagina(myTag, 18, "1");
                                        } else {
                                            nfcUltra.writePagina(myTag, 18, "0");
                                        }
                                    }else{
                                        banderaPermisos = 2;
                                    }

                                } else {
                                    banderaPermisos = 1;
                                }
                            }

                        }

                        TagModel datosTagCamion = new TagModel(getApplicationContext());
                        datosTagCamion = datosTagCamion.find(UID, idcamion, idproyecto);
                        if(banderaPermisos == 0) {
                            if (datosTagCamion.estatus == 1) {
                                if (idproyecto == usuario.getProyecto()) {
                                    if ((data.equals(camion.replace(" ", "")) && dataTime.equals(fecha.replace(" ", "")) && user.equals(idusuario.replace(" ", ""))) || deductiva_check) {

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
                                        if (seguimiento.getText().toString().equals("")) {
                                            cv.put("folio_seguimiento", 0);
                                        } else {
                                            cv.put("folio_seguimiento", seguimiento.getText().toString());
                                        }
                                        if (vale_mina.getText().toString().equals("")) {
                                            cv.put("folio_mina", 0);
                                        } else {
                                            cv.put("folio_mina", vale_mina.getText().toString());
                                        }

                                        cv.put("tipo_suministro", tipo_s);
                                        if (tipo_s == 1) {
                                            cv.put("Code", Util.folio(Util.dateFolios()) + String.valueOf(idcamion));
                                        }
                                        if (deductiva.getText().toString().equals("") && !deductiva_check) {
                                            cv.put("deductiva", 0);
                                            cv.put("idMotivo", 0);
                                        } else if(deductiva_check) {
                                            cv.put("deductiva", txtDeductiva);
                                            cv.put("idMotivo", id_motivo);
                                        }else{
                                            cv.put("deductiva", deductiva.getText().toString());
                                            cv.put("idMotivo", idMotivo);
                                        }

                                        if(deductiva_check){
                                            cv.put("deductiva_entrada", 1);
                                        }else {
                                            cv.put("deductiva_entrada", 0);
                                        }

                                        cv.put("numImpresion", 0);
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
                                Toast.makeText(getApplicationContext(), "El camión " + datosTagCamion.economico + " se encuentra inactivo. Por favor contacta al encargado.", Toast.LENGTH_LONG).show();
                            }
                        } else if(banderaPermisos == 2){
                            Toast.makeText(SetOrigenActivity.this, "El TAG cuenta con un viaje activo, Favor de pasar a un filtro de salida para finalizar el viaje.", Toast.LENGTH_LONG).show();

                        }else {
                            mensajeDeductiva();
                            Toast.makeText(SetOrigenActivity.this, "El TAG cuenta con datos de Origen Mina, Favor de pasar a un filtro de salida", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "El identificador de Origen o Material sobrepasa el limite permitido, Por favor contacta al encargado.", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(SetOrigenActivity.this, getString(R.string.error_tag_inexistente), Toast.LENGTH_LONG).show();
                }

            }
        }*/
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

    class MensajeTarea extends AsyncTask<Void, Void, Boolean> {
        TagNFC tag_nfc = new TagNFC();
        Context context;
        Intent intent;
        String mensaje_error = "";
        Integer IdInicio;
        public MensajeTarea(Context context, Intent intent, TagNFC tag_nfc) {
            this.context = context;
            this.intent = intent;
            this.tag_nfc =tag_nfc;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nfcAdapter.disableForegroundDispatch(SetOrigenActivity.this);
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
                            if (tag_nfc.getUID().equals(nfcTag.byteArrayToHexString(myTag.getId()))) {
                                mensaje_error = "continuar";
                            } else {
                                mensaje_error = "¡Error! Utilice el mismo tag.";
                                return false;
                            }
                        }
                        if (MifareUltralight.class.getName().equals(t)) {
                            nfcUltra = new NFCUltralight(myTag, context);
                            if (tag_nfc.getUID().equals(nfcUltra.byteArrayToHexString(myTag.getId()))) {
                                mensaje_error = "continuar";
                            } else {
                                mensaje_error = "¡Error! Utilice el mismo tag.";
                                return false;
                            }
                        }
                    }
                }
            }
            if (mensaje_error == "continuar") {
                datosVista.put("idcamion", tag_nfc.getIdcamion());
                datosVista.put("fecha_origen", Util.getFormatDate(tag_nfc.getFecha()));
                datosVista.put("uidTAG", tag_nfc.getUID());
                datosVista.put("estatus", 1);
                datosVista.put("tipoEsquema", tag_nfc.getTipo_viaje());
                datosVista.put("idperfil", usuario.tipo_permiso);
                datosVista.put("deductiva_entrada", 1);
                datosVista.put("deductiva", txtDeductiva);
                datosVista.put("idMotivo", tag_nfc.getIdmotivo());
                datosVista.put("numImpresion", 0);
                datosVista.put("tipo_suministro", tag_nfc.getTipo_viaje());
                SalidaMina salida_mina = new SalidaMina(context, tag_nfc);
                if (!salida_mina.guardarDatosDB(datosVista)) {
                    mensaje_error = "Error al guardar en Base de Datos";
                    return false;
                } else {
                    IdInicio = salida_mina.idInicio;
                    myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    if (tag_nfc.getTipo() == 1) {
                        if (tag_nfc.getUID().equals(nfcTag.byteArrayToHexString(myTag.getId()))) {
                            try {
                                nfcTag.writeSector(myTag, 4, 16, txtDeductiva);
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje_error = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcTag.writeSector(myTag, 4, 17, tag_nfc.getIdmotivo());
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje_error = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                        } else {
                            mensaje_error = "¡Error! Utilice el mismo tag.";
                            return false;
                        }
                    }
                    if (tag_nfc.getTipo() == 2) {
                        if (tag_nfc.getUID().equals(nfcUltra.byteArrayToHexString(myTag.getId()))) {
                            try {
                                nfcUltra.writePagina(null, 19, txtDeductiva.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje_error = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                            try {
                                nfcUltra.writePagina(null, 20, tag_nfc.getIdmotivo());
                            } catch (IOException e) {
                                e.printStackTrace();
                                mensaje_error = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                        } else {
                            mensaje_error = "¡Error! Utilice el mismo tag.";
                            return false;
                        }
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
            if (registro){
                Intent success = new Intent(getApplicationContext(), SuccessDestinoActivity.class);
                success.putExtra("idInicio", IdInicio);
                startActivity(success);
            }else {
                Toast.makeText(context, mensaje_error, Toast.LENGTH_SHORT).show();
                mensajeDeductiva(intent,tag_nfc);
            }
        }
    }

    public void mensajeDeductiva(final Intent intent, final TagNFC tag_nfc){
        final Boolean[] ok = {false};
        final android.app.AlertDialog.Builder alerta = new android.app.AlertDialog.Builder(SetOrigenActivity.this);
        View vista = getLayoutInflater().inflate(R.layout.popup,  null);
        final EditText deduc = (EditText) vista.findViewById(R.id.etPopAgregar);
        alerta.setTitle("¿Desea Agregar un Nuevo Volumen?");

        alerta.setView(vista);

        alerta.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(deduc.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Por favor escribir el volumen", Toast.LENGTH_SHORT).show();
                }else{
                    txtDeductiva = deduc.getText().toString();
                    //WriteModeOn();
                    new MensajeTarea(getApplicationContext(),  intent,tag_nfc).execute();
                }
            }
        });
        alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Favor de Pasar a la Salida para Finalizar el Viaje.", Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });
       alerta.show();
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
