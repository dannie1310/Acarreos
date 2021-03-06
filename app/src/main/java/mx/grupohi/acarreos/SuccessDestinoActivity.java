package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Layout;
import android.util.Base64;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.Set;
import java.util.Timer;

import javax.crypto.Cipher;

public class SuccessDestinoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView textViewCamion,
            textViewCubicacion,
            textViewMaterial,
            textViewOrigen,
            textViewFechaHoraSalida,
            textViewDestino,
            textViewFechaHoraLlegada,
            textViewRuta,
            textViewObservaciones,
            textViewDeductiva,
            motivo,
            textDestino,
            textFechaDestino,
            textRuta,
            textDeductiva,
            textObservacion,
            textMina,
            textSeg,
            textTipoViaje,
            textViewTipoViaje;

    private View view2;
    private LinearLayout folioMina,
            folioSeg;

    private Toolbar toolbar;
    private Integer impresion;

    private ProgressDialog progressDialogSync;
    private Usuario usuario;
    private Viaje viaje;
    private InicioViaje in;
    private Checador checador;
    private Integer idViaje;
    private Integer inicio;
    private String empresa;
    private Integer logo;
    Boolean tipo_usuario = false; // true - origen ; false - tiro
    private Button btnImprimir,
            btnSalir,
            btnImagenes;

    static Bitmap bitmap;

    public static BixolonPrinter bixolonPrinterApi;
    public static int milisegundos = 7200;
    private static final long PRINTING_TIME = 2100;
    private static final long PRINTING_SLEEP_TIME = 300;
    static final int MESSAGE_START_WORK = Integer.MAX_VALUE - 2;
    static final int MESSAGE_END_WORK = Integer.MAX_VALUE - 3;
    private final int LINE_CHARS = 64;

    private static Boolean connectedPrinter = false;
    private boolean imprimir;

    private String mConnectedDeviceName = null;
    private static String APP_DIRECTORY = "Picture/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";
    private String mPath;
    private final int PHOTO_CODE = 200;
    private ImageView mSetImage;
    boolean estado = false;

    //GPS
    private GPSTracker gps;
    private String IMEI;
    CelularImpresora cl;
    String datos;
    ProgressDialog progressDialog;
    JSONObject datosticket;

    Integer num = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_destino);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BitmapDrawable drawable;
        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        viaje = new Viaje(this);

        empresa=usuario.getEmpresa();
        logo=usuario.getLogo();
        checador = new Checador(getApplicationContext());
        bitmap = crearImagen(logo);
        bixolonPrinterApi = new BixolonPrinter(this, mHandler, null);
        view2 = (View) findViewById(R.id.view2);
        textViewCamion = (TextView) findViewById(R.id.textViewCamion);
        textViewCubicacion = (TextView) findViewById(R.id.textViewCubicacion);
        textViewMaterial = (TextView) findViewById(R.id.textViewMaterial);
        textViewOrigen = (TextView) findViewById(R.id.textViewOrigen);
        textViewFechaHoraSalida = (TextView) findViewById(R.id.textViewFechaHora);
        textViewDestino = (TextView) findViewById(R.id.textViewDestino);
        textViewFechaHoraLlegada = (TextView) findViewById(R.id.textViewFechaHoraLlegada);
        textViewRuta = (TextView) findViewById(R.id.textViewRuta);
        textViewObservaciones = (TextView) findViewById(R.id.textViewObservaciones);
        textViewDeductiva = (TextView) findViewById(R.id.textViewDeductiva);
        textDestino = (TextView) findViewById(R.id.textDestino);
        textFechaDestino = (TextView) findViewById(R.id.textFechaDestino);
        textRuta = (TextView) findViewById(R.id.textRuta);
        textDeductiva = (TextView) findViewById(R.id.textDeductiva);
        textObservacion = (TextView) findViewById(R.id.textObservaciones);
        textMina = (TextView) findViewById(R.id.textViewFolioMina);
        textSeg = (TextView) findViewById(R.id.textViewFolioSeg);


        textTipoViaje = (TextView) findViewById(R.id.textTipoViaje);
        textViewTipoViaje = (TextView) findViewById(R.id.textViewTipoViaje);

        btnImprimir = (Button) findViewById(R.id.buttonImprimir);
        btnImagenes = (Button) findViewById(R.id.buttonImagenes);
        btnSalir = (Button) findViewById(R.id.buttonSalir);

        folioMina = (LinearLayout) findViewById(R.id.folioMina);
        folioSeg = (LinearLayout) findViewById(R.id.folioSeg);

        fillInfo();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        gps = new GPSTracker(SuccessDestinoActivity.this);
        TelephonyManager phneMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = phneMgr.getDeviceId();
        cl = new CelularImpresora(getApplicationContext());
        cl = cl.find(IMEI);

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        btnImagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagenesViaje im = new ImagenesViaje(getApplicationContext());
                Integer numImagenes = im.getCount(idViaje);
                //im.getAll();
                //System.out.println("open:  "+numImagenes+ " "+idViaje.toString());
                if(numImagenes == 0){
                    Intent intent = new Intent(getApplicationContext(), CamaraActivity.class);
                    intent.putExtra("idviaje_neto", idViaje.toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }else{
                    Intent intent = new Intent(getApplicationContext(), ImagenesActivity.class);
                    intent.putExtra("idviaje_neto", idViaje.toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }
        });

        btnImprimir.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               num = Viaje.numImpresion(viaje.idViaje, getApplicationContext());
                                              if(num == null) {
                                                  num = InicioViaje.numImpresion(in.id, getApplicationContext());
                                              }
                                               if (num!=null && num > 4)
                                               {
                                                   Toast.makeText(getApplicationContext(), R.string.error_ticket, Toast.LENGTH_SHORT).show();
                                               } else {
                                                   btnImprimir.setEnabled(false);
                                                   //  onPause();
                                                   imprimir = true;
                                                   if (!connectedPrinter) {
                                                       bixolonPrinterApi.findBluetoothPrinters();
                                                   }
                                                   tiempoEspera();
                                                   checkEnabled();
                                                   new Handler().postDelayed(new Thread() {
                                                       @Override
                                                       public void run() {
                                                           super.run();
                                                           // btnImprimir.setEnabled(false);
                                                           if (connectedPrinter) {
                                                               try {
                                                                progressDialog = ProgressDialog.show(SuccessDestinoActivity.this, "Imprimiendo", "Por favor espere...", true);
                                                                new ImprimirTicket(getApplicationContext(), progressDialog, bixolonPrinterApi, TicketDatos(), bitmap).execute((Void) null);
                                                               } catch (JSONException e) {
                                                                   e.printStackTrace();
                                                                   Crashlytics.logException(e);
                                                               }
                                                           }
                                                       }
                                                   }, PRINTING_TIME);

                                               }
                                           }
                                       });

        onPause();
      //  bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);

    }
    public JSONObject TicketDatos() throws JSONException {
        String nombreChecador = "SIN PERFIL";
        String urlEncoded = null;
        String urlCode = null;
        String datos_inicio;
        inicio = getIntent().getIntExtra("idInicio", 0);
        if (tipo_usuario == false) {
           // datos = "000|000000|000|000000000000|000|000000000000|000|00000|000000000000|000000000000000|00000|00|000000000000000000|0|00000000|00000000|00|0";
            datos = usuario.idProyecto + "|"
                    + viaje.idCamion + "|"
                    + viaje.idOrigen + "|"
                    + viaje.fechaSalida.substring(2, 10).replace("/", "") + viaje.horaSalida.replace(":", "") + "|"
                    + viaje.idTiro + "|"
                    + viaje.fechaLlegada.substring(2, 10).replace("/", "") + viaje.horaLlegada.replace(":", "") + "|"
                    + viaje.idMaterial + "|"
                    + viaje.creo + "|"
                    + viaje.getCode(idViaje).replace(viaje.idCamion.toString(), "") + "|"
                    + viaje.uidTAG + "|"
                    + viaje.primerToque + "|"
                    + viaje.cubicacion + "|"
                    + IMEI +"|"
                    + viaje.tipoViaje+"|"
                    + viaje.folio_mina + "|"
                    + viaje.folio_seguimiento +"|0|"
                    + usuario.tipo_permiso;


            try {
                urlEncoded = URLEncoder.encode(encrypt(datos), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!viaje.primerToque.isEmpty()) {
                nombreChecador = checador.findNombre(Integer.valueOf(viaje.primerToque));
                if (nombreChecador == null) {
                    nombreChecador = "SIN PERFIL";
                }
            }
        }

        JSONObject json = new JSONObject();
        if (viaje != null) {
            json.put("0", viaje.idViaje);
            json.put("19", urlEncoded);
            json.put("22",  viaje.getCode(idViaje));
            json.put("23", textMina.getText());
            json.put("24", textSeg.getText());

            json.put("31", "0");
            json.put("33", "0");
            json.put("32", "0");
            json.put("34","0");

            if(viaje.latitud_origen != null) {
                json.put("35", viaje.latitud_origen);
            }else{
                json.put("35", "NULL");
            }
            if(viaje.longitud_origen != null) {
                json.put("36", viaje.longitud_origen);
            }else{
                json.put("36","NULL");
            }
            if (viaje.latitud_tiro != null) {
                json.put("37", viaje.latitud_tiro);
            }else{
                json.put("37", "NULL");
            }
            if (viaje.longitud_tiro != null) {
                json.put("38", viaje.longitud_tiro);
            }else{
                json.put("38", "NULL");
            }
        }

        json.put("1", usuario.getDescripcion());
        json.put("2", textViewCamion.getText());
        json.put("3", textViewCubicacion.getText().toString().replace(" m3",""));
        json.put("4", textViewMaterial.getText());
        json.put("5", textViewOrigen.getText());
        json.put("6", textViewFechaHoraSalida.getText());
        json.put("7", textViewDestino.getText());
        json.put("8", textViewFechaHoraLlegada.getText());
        json.put("9", textViewRuta.getText());
        json.put("10", textViewDeductiva.getText());
        json.put("11", 0);
        json.put("12", textViewObservaciones.getText());
        json.put("13", usuario.tipo_permiso);
        json.put("14", usuario.getNombre());
        json.put("15", Util.getTiempo());
        json.put("16", String.valueOf(BuildConfig.VERSION_NAME));
        json.put("17", nombreChecador);
        json.put("21", empresa);
        json.put("30", textViewTipoViaje.getText());
        json.put("23", textMina.getText());
        json.put("24", textSeg.getText());

        if(inicio != 0) {
            InicioViaje inicios = new InicioViaje(getApplicationContext());
            inicios = inicios.find(inicio);
            json.put("20", inicio);
            json.put("25", 0);
            json.put("26", inicios.tipo_suministro);
            json.put("31", "NULL");
            json.put("32", "NULL");
            json.put("33", "NULL");
            json.put("34", "NULL");
            json.put("35", inicios.latitud_origen);
            json.put("36", inicios.longitud_origen);
            json.put("37", "NULL");
            json.put("38", "NULL");
            if(inicios.tipo_suministro == 1 && inicios.deductiva_entrada == 0) {
               // datos_inicio = "000|000000|000|000000000000|000|000000000000|000|00000|000000000000|000000000000000|00000|00|000000000000000000|0|00000000|00000000|00|0";
                datos_inicio = usuario.idProyecto + "|"
                        + inicios.idcamion + "|"
                        + inicios.idorigen + "|"
                        + inicios.fecha_origen.substring(2,inicios.fecha_origen.length()).replace("/", "").replace("-", "").replace(":", "").replace(" ", "") + "|"
                        + "0|"
                        + "0|"
                        + inicios.idmaterial + "|"
                        + "0|"
                        + inicios.getCode(inicio).replace(inicios.idcamion.toString(), "") + "|"
                        + inicios.uidTAG + "|"
                        + inicios.idusuario + "|"
                        + inicios.deductiva + "|"
                        + IMEI + "|"
                        + inicios.tipo_suministro + '|'
                        + inicios.folio_mina + '|'
                        + inicios.folio_seg + "|"
                        + inicios.deductiva + "|"
                        + usuario.tipo_permiso;
                try {
                    urlCode = URLEncoder.encode(encrypt(datos_inicio), "utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                json.put("19", urlCode);
                json.put("22", inicios.getCode(inicio));
            }else{
                json.put("19", "");
                json.put("22", "");
            }
        }else{
            json.put("20", "NULL");
        }
        return json;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkEnabled();
    }

    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onPause() {
       if (bixolonPrinterApi != null) {
            bixolonPrinterApi.disconnect();
       }
        super.onPause();
    }


    private void checkEnabled() {
        Boolean enabled = Bluetooth.statusBluetooth();
        if (!enabled) {
            new android.app.AlertDialog.Builder(SuccessDestinoActivity.this)
                    .setTitle(getString(R.string.text_warning_blue_is_off))
                    .setMessage(getString(R.string.text_turn_on_bluetooth))
                    .setCancelable(true)
                    .setPositiveButton(
                            getString(R.string.text_update_settingsB),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                                }
                            })
                    .create()
                    .show();
        }
    }



    public void fillInfo() {
        inicio = getIntent().getIntExtra("idInicio", 0);
        idViaje = getIntent().getIntExtra("idViaje", 0);
        if(idViaje != 0) {
            viaje = viaje.find(idViaje);
            if(viaje.camion == null){
                textViewCamion.setText("NO SE ENCONTRO");
                textViewCubicacion.setText("NO SE ENCONTRO");
                alert("Favor de descargar catálogos datos no encontrados.");
            }else {
                textViewCamion.setText(viaje.camion.economico);
                textViewCubicacion.setText(viaje.camion.capacidad + " m3");
            }
            if(viaje.material != null) {
                textViewMaterial.setText(viaje.material.descripcion);
            }else{
                textViewMaterial.setText("NO SE ENCONTRO");
            }
            textViewFechaHoraSalida.setText(viaje.fechaSalida + " " + viaje.horaSalida);
            if(viaje.tiro != null) {
                textViewDestino.setText(viaje.tiro.descripcion);
            }else{
                textViewDestino.setText("NO SE ENCONTRO");
            }
            textViewFechaHoraLlegada.setText(viaje.fechaLlegada + " " + viaje.horaLlegada);
            if(viaje.origen != null) {
                textViewOrigen.setText(viaje.origen.descripcion);
            }else{
                textViewOrigen.setText("NO SE ENCONTRO");
            }
            if (viaje.idRuta != 0) {
                textViewRuta.setText(viaje.ruta.toString());
            }else{
                textViewRuta.setText("NO SE ENCONTRO RUTA");
            }

            textViewObservaciones.setText(viaje.observaciones);
            textViewDeductiva.setText(viaje.cubicacion);
            if(viaje.folio_mina != "" && viaje.folio_seguimiento!=""){
                folioMina.setVisibility(View.VISIBLE);
                folioSeg.setVisibility(View.VISIBLE);
                textMina.setText(viaje.folio_mina);
                textSeg.setText(viaje.folio_seguimiento);
            }

            if(viaje.tipoViaje == 1){
                textTipoViaje.setVisibility(View.VISIBLE);
                textViewTipoViaje.setText("Origen(Mina).");
            }else{
                textTipoViaje.setVisibility(View.GONE);
            }

        }
        else if(inicio != 0){
            in = new InicioViaje(getApplicationContext());
            in = in.find(inicio);
            textViewDeductiva.setText(in.deductiva);
            if(in.camion == null){
                textViewCamion.setText("NO SE ENCONTRO");
                textViewCubicacion.setText("NO SE ENCONTRO");
                alert("Favor de descargar catálogos datos no encontrados.");
            }else {
                textViewCamion.setText(in.camion.economico);
                textViewCubicacion.setText(in.camion.capacidad + " m3");
            }
            if(in.material != null) {
                textViewMaterial.setText(in.material.descripcion);
            }else{
                textViewMaterial.setText("NO SE ENCONTRO");
            }
            if(in.origen != null) {
                textViewOrigen.setText(in.origen.descripcion);
            }else{
                textViewOrigen.setText("NO SE ENCONTRO");
            }
            textViewFechaHoraSalida.setText(in.fecha_origen);
            if(in.folio_seg != null || in.folio_mina != null) {
                folioMina.setVisibility(View.VISIBLE);
                folioSeg.setVisibility(View.VISIBLE);
                textMina.setText(in.folio_mina);
                textSeg.setText(in.folio_seg);
            }
            textViewDestino.setVisibility(View.GONE);
            textViewFechaHoraLlegada.setVisibility(View.GONE);
            textViewRuta.setVisibility(View.GONE);
            textViewObservaciones.setVisibility(View.GONE);
            btnImagenes.setVisibility(View.GONE);
            tipo_usuario = true;
            textDestino.setVisibility(View.GONE);
            textFechaDestino.setVisibility(View.GONE);
            textRuta.setVisibility(View.GONE);
            textObservacion.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
            btnImprimir.setText("IMPRIMIR");
            if(in.deductiva_entrada == 1 && in.tipo_suministro==1){
                textTipoViaje.setVisibility(View.VISIBLE);
                textViewTipoViaje.setText("Origen (Mina) - Volumen Entrada.");
            } else if (in.tipo_suministro == 1){
                textViewTipoViaje.setText("Origen (Mina).");
            }else{
                textTipoViaje.setVisibility(View.GONE);
            }
        }


    }
    public void alert(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SuccessDestinoActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {

                    }
                }).show();
    }
    @Override
    public void onBackPressed() {
        Integer list = getIntent().getIntExtra("list", 0);
        if(list == 1) {
            super.onBackPressed();
        } else {
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
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.nav_pair_on) {
            imprimir = false;
            bixolonPrinterApi.findBluetoothPrinters();
        } else if (id == R.id.nav_pair_off) {
            bixolonPrinterApi.disconnect();
        }
        return super.onOptionsItemSelected(item);
    }
*/
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
        }  else if (id == R.id.nav_sync) {
            new AlertDialog.Builder(SuccessDestinoActivity.this)
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                                    progressDialogSync = ProgressDialog.show(SuccessDestinoActivity.this, "Sincronizando datos", "Por favor espere...", true);
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
            Intent navList = new Intent(this, ListaViajesActivity.class);
            startActivity(navList);
        } else if (id == R.id.nav_logout) {
            if(!Viaje.isSync(getApplicationContext()) || !InicioViaje.isSync(getApplicationContext())){
                new AlertDialog.Builder(SuccessDestinoActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(SuccessDestinoActivity.this, "Sincronizando datos", "Por favor espere...", true);
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

    private String encrypt(String str) throws Exception {
        String file = this.getExternalFilesDir("certificados/SAO_certificado2048.crt").toString();
        InputStream is = new FileInputStream(file);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)certificateFactory.generateCertificate(is);
        PublicKey pk = certificate.getPublicKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pk);
        return Base64.encodeToString(cipher.doFinal(str.getBytes()), Base64.DEFAULT);
    }

    public Bitmap crearImagen(Integer logo) {
        Bitmap bitmap = null;
        if (logo == 1) {
            BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ghi_logo);
            bitmap = drawable.getBitmap();
            
        }
        if (logo == 2) {
            int x = usuario.getProyecto();
            String log = usuario.getImagen();
            if (log != null) {
                bitmap = usuario.decodeBase64(log);
            }
        }
        return bitmap;
    }

    Handler mHandler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            Log.i("Handler: - ", msg.what + " " + msg.arg1 + " " + msg.arg2);

            switch (msg.what) {

                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_STATE_CHANGE");
                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:
                            Log.i("Handler", "BixolonPrinter.STATE_CONNECTED");
                            toolbar.setSubtitle("Impresora Contectada " + mConnectedDeviceName);
                            // btnImprimir.setEnabled(false);
                            connectedPrinter = true;
                           /*if(imprimir) {
                                btnImprimir.performClick();
                           }*/

                            break;

                        case BixolonPrinter.STATE_CONNECTING:
                            Log.i("Handler", "BixolonPrinter.STATE_CONNECTING");
                            toolbar.setSubtitle(R.string.title_connecting);
                            SuccessDestinoActivity.connectedPrinter = false;

                            break;

                        case BixolonPrinter.STATE_NONE:
                            toolbar.setSubtitle(R.string.title_not_connected);
                            Log.i("Handler", "BixolonPrinter.STATE_NONE");
                            connectedPrinter = false;

                            break;
                        case BixolonPrinter.STATUS_BATTERY_LOW:
                            toolbar.setSubtitle(R.string.title_battery_low);
                            connectedPrinter = false;

                            break;
                    }
                    break;

                case BixolonPrinter.MESSAGE_WRITE:
                    switch (msg.arg1) {
                        case BixolonPrinter.PROCESS_SET_SINGLE_BYTE_FONT:
                            Log.i("Handler", "BixolonPrinter.PROCESS_SET_SINGLE_BYTE_FONT");
                            break;

                        case BixolonPrinter.PROCESS_SET_DOUBLE_BYTE_FONT:
                            Log.i("Handler", "BixolonPrinter.PROCESS_SET_DOUBLE_BYTE_FONT");
                            break;

                        case BixolonPrinter.PROCESS_DEFINE_NV_IMAGE:
                            Log.i("Handler", "BixolonPrinter.PROCESS_DEFINE_NV_IMAGE");
                            break;

                        case BixolonPrinter.PROCESS_REMOVE_NV_IMAGE:
                            Log.i("Handler", "BixolonPrinter.PROCESS_REMOVE_NV_IMAGE");
                            break;

                        case BixolonPrinter.PROCESS_UPDATE_FIRMWARE:
                            Log.i("Handler", "BixolonPrinter.PROCESS_UPDATE_FIRMWARE");
                            break;
                    }
                    break;

                case BixolonPrinter.MESSAGE_READ:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_READ");
                    break;

                case BixolonPrinter.MESSAGE_DEVICE_NAME:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_DEVICE_NAME - " + msg.getData().getString(BixolonPrinter.KEY_STRING_DEVICE_NAME));
                    mConnectedDeviceName = msg.getData().getString(BixolonPrinter.KEY_STRING_DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Impresora Conectada como: " + mConnectedDeviceName, Toast.LENGTH_LONG).show();
                    break;

                case BixolonPrinter.MESSAGE_TOAST:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_TOAST - " + msg.getData().getString("toast"));
                    break;

                // The list of paired printers
                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET");
                    if (msg.obj == null) {
                        new android.app.AlertDialog.Builder(SuccessDestinoActivity.this)
                                .setTitle("¡Error!")
                                .setMessage("No Hay Impresoras Emparejadas.")
                                .setCancelable(true)
                                .setPositiveButton("Ajustes de Bluetooth",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                                            }
                                        })
                                .create()
                                .show();
                        // Toast.makeText(getApplicationContext(), "No Hay Impresoras Emparejadas", Toast.LENGTH_LONG).show();
                    } else {
                        Set<BluetoothDevice> pairedDevices = (Set<BluetoothDevice>) msg.obj;
                        DialogManager.showBluetoothDialog(SuccessDestinoActivity.this, bixolonPrinterApi, (Set<BluetoothDevice>) msg.obj, cl.MAC);
                    }
                    break;

                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_PRINT_COMPLETE");
                    //Toast.makeText(getApplicationContext(),"Impresión Completa!!!.", Toast.LENGTH_SHORT).show();


                    break;

                case BixolonPrinter.MESSAGE_COMPLETE_PROCESS_BITMAP:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_COMPLETE_PROCESS_BITMAP");
                    break;

                case MESSAGE_START_WORK:
                    Log.i("Handler", "MESSAGE_START_WORK");
                    Toast.makeText(getApplicationContext(),"Iniciando Impresión ", Toast.LENGTH_LONG).show();
                    break;

                case MESSAGE_END_WORK:
                    Log.i("Handler", "MESSAGE_END_WORK");
                    Toast.makeText(getApplicationContext(),"Finalizado ", Toast.LENGTH_LONG).show();

                    break;

                case BixolonPrinter.MESSAGE_NETWORK_DEVICE_SET:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_NETWORK_DEVICE_SET");
                    if (msg.obj == null) {
                        Toast.makeText(getApplicationContext(), "No connectable device", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };

    public  void tiempoEspera(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                btnImprimir.setEnabled(true);
            }
        }, 6000);
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
                    startActivity(mainActivity);
                }else if(tipo == 1){
                    mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainActivity);
                }

            }
        }, 8000);
    }

}
