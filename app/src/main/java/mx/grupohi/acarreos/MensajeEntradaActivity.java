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
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import java.io.IOException;

import mx.grupohi.acarreos.Mina.SalidaMina;
import mx.grupohi.acarreos.TiposTag.TagNFC;

public class MensajeEntradaActivity extends AppCompatActivity {
    //Objetos
    private Usuario usuario;
    private TagNFC tagNFC;
    private Camion c;
    private ContentValues datosVista;

    //Referencias UI
    private LinearLayout mainLayout;
    private LinearLayout lecturaTag;
    private ImageView nfcImage;
    private FloatingActionButton fabCancel;
    private ProgressDialog progressDialogSync;

    EditText deduc;
    String mensaje;
    String txtDeductiva;

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

        setContentView(R.layout.activity_mensaje_entrada_volumen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        certificados = new Certificados(getApplicationContext());
        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        tagNFC = (TagNFC) getIntent().getSerializableExtra("datos");
        c = new Camion(getApplicationContext());
        c = c.find(tagNFC.getIdcamion());
        gps = new GPSTracker(MensajeEntradaActivity.this);
        datosVista = new ContentValues();
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

        nfcImage = (ImageView) findViewById(R.id.imageViewNFC);
        fabCancel = (FloatingActionButton) findViewById(R.id.fabCancel);
        mainLayout = (LinearLayout) findViewById(R.id.MainLayout);
        lecturaTag = (LinearLayout) findViewById(R.id.leerTag);
        lecturaTag.setVisibility(View.GONE);
        mensajeDeductiva();
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
                mensajeDeductiva();
                WriteModeOff();
            }
        });

    }
    public void mensajeDeductiva(){
        final Boolean[] ok = {false};
        final android.app.AlertDialog.Builder alerta = new android.app.AlertDialog.Builder(MensajeEntradaActivity.this);
        View vista = getLayoutInflater().inflate(R.layout.popup,  null);
        final EditText deduc = (EditText) vista.findViewById(R.id.etPopAgregar);
        alerta.setTitle("¿Desea Agregar un Nuevo Volumen?");

        alerta.setView(vista);

        alerta.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                txtDeductiva = deduc.getText().toString();
                if(!validarCampos()) {
                    alert(mensaje);
                }else{
                    WriteModeOn();
                }
            }
        });
        alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Favor de Pasar a la Salida para Finalizar el Viaje.", Toast.LENGTH_LONG).show();
                dialog.cancel();
                Intent success = new Intent(getApplicationContext(), SetOrigenActivity.class);
                startActivity(success);
            }
        });
        alerta.show();
    }


    private Boolean validarCampos(){
        /// validar volumen entrada
        if(txtDeductiva.isEmpty()){
            mensaje = "Por favor escribir el volumen";
            mensajeDeductiva();
            return false;
        }
        if(foliosSinCeros(txtDeductiva)){
            mensaje = "El volumen no puede ser cero.";
            return false;
        }
        if(c.capacidad != 0 && c.capacidad!= null && Integer.valueOf(txtDeductiva) > c.capacidad) {
            mensaje = "El volumen es mayor a la capacidad del camión.";
            return false;
        }

        /// asignacion de valores
        datosVista.put("idmaterial", tagNFC.getIdmaterial());
        datosVista.put("idorigen", tagNFC.getIdorigen());
        datosVista.put("folio_mina", "");
        datosVista.put("folio_seguimiento", "");
        datosVista.put("idusuario", String.valueOf(usuario.getId()));
        datosVista.put("IMEI", IMEI);
        return true;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        new MensajeTarea(getApplicationContext(), intent).execute();
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
        Context context;
        Intent intent;
        String mensaje_error = "";
        Integer IdInicio = 0;
        public MensajeTarea(Context context, Intent intent) {
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
            if (writeMode) {
                if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                    myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    String[] techs = myTag.getTechList();
                    for (String t : techs) {
                        if (MifareClassic.class.getName().equals(t)) {
                            nfcTag = new NFCTag(myTag, context);
                            if (tagNFC.getUID().equals(nfcTag.byteArrayToHexString(myTag.getId()))) {
                                mensaje_error = "continuar";
                            } else {
                                mensaje_error = "¡Error! Utilice el mismo tag.";
                                return false;
                            }
                        }
                        if (MifareUltralight.class.getName().equals(t)) {
                            nfcUltra = new NFCUltralight(myTag, context);
                            if (tagNFC.getUID().equals(nfcUltra.byteArrayToHexString(myTag.getId()))) {
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
                datosVista.put("idcamion", tagNFC.getIdcamion());
                datosVista.put("fecha_origen", Util.getFormatDate(tagNFC.getFecha()));
                datosVista.put("uidTAG", tagNFC.getUID());
                datosVista.put("estatus", 1);
                datosVista.put("tipoEsquema", tagNFC.getTipo_viaje());
                datosVista.put("idperfil", usuario.tipo_permiso);
                datosVista.put("deductiva_entrada", 1);
                datosVista.put("deductiva", txtDeductiva);
                datosVista.put("idMotivo", tagNFC.getIdmotivo());
                datosVista.put("numImpresion", 0);
                datosVista.put("tipo_suministro", tagNFC.getTipo_viaje());
                SalidaMina salida_mina = new SalidaMina(context, tagNFC);
                if (!salida_mina.guardarDatosDB(datosVista)) {
                    mensaje_error = "Error al guardar en Base de Datos";
                    return false;
                } else {
                    IdInicio = salida_mina.idInicio;
                    myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    if (tagNFC.getTipo() == 1) {
                        if (tagNFC.getUID().equals(nfcTag.byteArrayToHexString(myTag.getId()))) {
                            try {
                                nfcTag.writeSector(myTag, 4, 16, txtDeductiva);
                                nfcTag.writeSector(myTag, 4, 17, tagNFC.getIdmotivo());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                mensaje_error = "¡Error! No se puede establecer la comunicación con el TAG, por favor mantenga el TAG cerca del dispositivo";
                                return false;
                            }
                        } else {
                            mensaje_error = "¡Error! Utilice el mismo tag.";
                            return false;
                        }
                    }
                    if (tagNFC.getTipo() == 2) {
                        if (tagNFC.getUID().equals(nfcUltra.byteArrayToHexString(myTag.getId()))) {
                            try {
                                nfcUltra.writePagina(null, 19, txtDeductiva);
                                nfcUltra.writePagina(null, 20, tagNFC.getIdmotivo());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
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
                if(!IdInicio.equals(0)){
                    SalidaMina salidaMina = new SalidaMina(context, tagNFC);
                    while(!salidaMina.rollbackDB(IdInicio)){
                       mensaje_error = "intentar nuevamente";
                    }
                    mensaje_error = "Manten el TAG más tiempo! " + mensaje_error;
                }
                alert(mensaje_error);
            }
        }
    }

    public void alert(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MensajeEntradaActivity.this);

        dialog.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        mensajeDeductiva();
                    }
                }).show();
    }

    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        lecturaTag.setVisibility(View.VISIBLE);
    }

    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
        lecturaTag.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        Intent success = new Intent(getApplicationContext(), SetOrigenActivity.class);
        startActivity(success);
    }

    public  void tiempoEsperaSincronizacion(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                Intent mainActivity;
                Integer tipo = usuario.getTipo_permiso();
                if(tipo == 0){
                    mainActivity = new Intent(getApplicationContext(), MensajeEntradaActivity.class);
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
            new android.app.AlertDialog.Builder(MensajeEntradaActivity.this)
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
