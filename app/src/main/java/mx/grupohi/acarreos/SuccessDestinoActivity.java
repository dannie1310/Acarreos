package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

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
            textMotivo,
            textObservacion;

    private View view2;

    private Toolbar toolbar;
    private Integer impresion;

    private ProgressDialog progressDialogSync;
    private Usuario usuario;
    private Viaje viaje;
    private InicioViaje inicioViaje;
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
        motivo = (TextView) findViewById(R.id.textViewMotivoDeductiva);
        textDestino = (TextView) findViewById(R.id.textDestino);
        textFechaDestino = (TextView) findViewById(R.id.textFechaDestino);
        textRuta = (TextView) findViewById(R.id.textRuta);
        textDeductiva = (TextView) findViewById(R.id.textDeductiva);
        textMotivo = (TextView) findViewById(R.id.textMotivo);
        textObservacion = (TextView) findViewById(R.id.textObservaciones);

        btnImprimir = (Button) findViewById(R.id.buttonImprimir);
        btnImagenes = (Button) findViewById(R.id.buttonImagenes);
        btnSalir = (Button) findViewById(R.id.buttonSalir);

        fillInfo();

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
                btnImprimir.setEnabled(false);
                imprimir = true;
                if(!connectedPrinter) {
                    bixolonPrinterApi.findBluetoothPrinters();
                }

                new Handler().postDelayed(new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        btnImprimir.setEnabled(true);
                    }
                }, PRINTING_TIME);

                Thread t = new Thread() {
                    public void run() {
                        try {
                            bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
                            Bitmap fewlapsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_success);

                           // Thread.sleep(PRINTING_SLEEP_TIME);

                            if (logo == 1) {
                                BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ghi_logo);
                                bitmap = drawable.getBitmap();
                                bixolonPrinterApi.printBitmap(bitmap, BixolonPrinter.ALIGNMENT_CENTER, 220, 50, true);
                            }
                            if (logo == 2) {
                                int x = usuario.getProyecto();
                                String log = usuario.getImagen();
                                if(log!=null) {
                                   bitmap = usuario.decodeBase64(log);
                                   bixolonPrinterApi.printBitmap(bitmap, BixolonPrinter.ALIGNMENT_CENTER, 220, 50, true);
                                }
                            }

                            if(!empresa.equals("null")) {
                                printheadproyecto(empresa);
                            }
                            if(tipo_usuario == false) {
                                viaje = viaje.find(idViaje);
                                impresion = viaje.numImpresion;
                                String nombreChecador = "SIN PERFIL";

                                if (!viaje.primerToque.isEmpty()) {
                                    nombreChecador = checador.findNombre(Integer.valueOf(viaje.primerToque));
                                    if (nombreChecador == null) {
                                        nombreChecador = "SIN PERFIL";
                                    }
                                }


                                bixolonPrinterApi.lineFeed(1, true);
                                printTextTwoColumns("Proyecto: ", usuario.getDescripcion() + " \n");
                                printTextTwoColumns("Camión: ", textViewCamion.getText() + " \n");
                                printTextTwoColumns("Cubicación: ", textViewCubicacion.getText() + " \n");

                                printTextTwoColumns("Material: ", textViewMaterial.getText() + "\n");
                                printTextTwoColumns("Origen: ", textViewOrigen.getText() + "\n");
                                printTextTwoColumns("Fecha de Salida: ", textViewFechaHoraSalida.getText() + "\n");

                                printTextTwoColumns("Destino: ", textViewDestino.getText() + "\n");
                                printTextTwoColumns("Fecha Llegada: ", textViewFechaHoraLlegada.getText() + "\n");
                                printTextTwoColumns("Ruta: ", textViewRuta.getText() + "\n");
                                // if(textViewObservaciones.getText().length()!=0) {
                                printTextTwoColumns("Deductiva: ", textViewDeductiva.getText() + "\n");
                                printTextTwoColumns("Motivo Deductiva: ", motivo.getText() + "\n");
                                printTextTwoColumns("Observaciones: ", textViewObservaciones.getText() + "\n");

                                if(usuario.tipo_permiso == 3){
                                    printTextTwoColumns("Checador: "+ usuario.getNombre(), Util.getTiempo()  + "\n");
                                    printTextTwoColumns("Versión: ", String.valueOf(BuildConfig.VERSION_NAME)+"\n");
                                    bixolonPrinterApi.printText("TIRO LIBRE ABORDO\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_C, 0, false);
                                }else {
                                    printTextTwoColumns("Checador Inicio: ", nombreChecador + "\n");
                                    printTextTwoColumns("Checador Cierre: " + usuario.getNombre(), Util.getTiempo() + "\n");
                                    printTextTwoColumns("Versión: ", String.valueOf(BuildConfig.VERSION_NAME)+"\n");
                                }

                                if(impresion != 0){
                                    bixolonPrinterApi.printText("R E I M P R E S I O N "+impresion, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
                                }
                                // }
                                //bixolonPrinterApi.lineFeed(1,true);
                                printfoot(impresion,"Checador: " + usuario.getNombre(), viaje.getCode(idViaje));
                                bixolonPrinterApi.printQrCode(viaje.getCode(idViaje), BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.QR_CODE_MODEL2, 5, false);

                                bixolonPrinterApi.lineFeed(2, true);
                                if(connectedPrinter != false){
                                    Viaje.updateImpresion(viaje.idViaje,impresion, getApplicationContext());
                                }
                            }else{
                                inicioViaje = new InicioViaje(getApplicationContext());
                                inicioViaje = inicioViaje.find(inicio);


                                bixolonPrinterApi.lineFeed(1, true);
                                printTextTwoColumns("Proyecto: ", usuario.getDescripcion() + " \n");
                                bixolonPrinterApi.lineFeed(1, true);
                                printTextTwoColumns("Camión: ", textViewCamion.getText() + " \n");
                                printTextTwoColumns("Cubicación: ", textViewCubicacion.getText() + " \n");
                                bixolonPrinterApi.lineFeed(1, true);
                                printTextTwoColumns("Material: ", textViewMaterial.getText() + "\n");
                                printTextTwoColumns("Origen: ", textViewOrigen.getText() + "\n");
                                printTextTwoColumns("Fecha de Origen: ", textViewFechaHoraSalida.getText() + "\n");

                                printTextTwoColumns("Checador: "+  usuario.getNombre(), Util.getTiempo() + "\n");
                                printfootorigen();

                            }

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), R.string.error_impresion, Toast.LENGTH_LONG).show();
                        }
                    }
                };
                t.start();
            }
        });
        onPause();
        bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkEnabled();
    }

    @Override
    protected void onPause() {
        if (bixolonPrinterApi != null) {
            bixolonPrinterApi.disconnect();
        }
        super.onPause();
    }

    public static void printheadproyecto(String text) {
        int alignment = BixolonPrinter.ALIGNMENT_CENTER;

        int attribute = 0;
        attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_C;

        int size = 0;
        bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        bixolonPrinterApi.printText(text, alignment, attribute, size, false);
       // bixolonPrinterApi.lineFeed(1, false);

        bixolonPrinterApi.cutPaper(true);
        bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);
    }

    /**
     * Print the common two columns ticket style text. Label+Value.
     *
     * @param leftText
     * @param rightText
     */
    private void printTextTwoColumns(String leftText, String rightText) {
        if (leftText.length() + rightText.length() + 1 > LINE_CHARS) {
            int alignment = BixolonPrinter.ALIGNMENT_LEFT;
            int attribute = 0;
            attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_C;
            bixolonPrinterApi.printText(leftText, alignment, attribute, BixolonPrinter.TEXT_SIZE_HORIZONTAL1, false);
            alignment = BixolonPrinter.ALIGNMENT_RIGHT;
            attribute = 0;
            attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_C;
            bixolonPrinterApi.printText(rightText, alignment, attribute, BixolonPrinter.TEXT_SIZE_HORIZONTAL1, false);
        } else {
            int padding = LINE_CHARS - leftText.length() - rightText.length();
            String paddingChar = "";
            for (int i = 0; i < padding; i++) {
                paddingChar = paddingChar.concat(" ");
            }

            int alignment = BixolonPrinter.ALIGNMENT_CENTER;
            int attribute = 0;
            attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_C;
            bixolonPrinterApi.printText(leftText + paddingChar + rightText, alignment, attribute, BixolonPrinter.TEXT_SIZE_HORIZONTAL1, false);
        }
    }

    public static void printfoot(Integer impresion, String text, String codex) {
        int alignment = BixolonPrinter.ALIGNMENT_LEFT;
        int attribute = 1;
        attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_A;
        int size = 0;

        bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        bixolonPrinterApi.lineFeed(1, false);
        bixolonPrinterApi.print1dBarcode(codex.toUpperCase(), BixolonPrinter.BAR_CODE_CODE39, BixolonPrinter.ALIGNMENT_CENTER, 2, 180, BixolonPrinter.HRI_CHARACTERS_BELOW_BAR_CODE, true);

        if(impresion != 0){
            bixolonPrinterApi.printText("R E I M P R E S I O N "+impresion+"\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
        }

        String cadena = "\nEste documento es un comprobante de recepción \nde materiales del Sistema de Administración de \nObra, no representa un compromiso de pago hasta \nsu validación contra las remisiones del \nproveedor y la revisión de factura.";
        bixolonPrinterApi.printText(cadena, BixolonPrinter.ALIGNMENT_CENTER, attribute, size, false);

        bixolonPrinterApi.lineFeed(1, false);

        bixolonPrinterApi.cutPaper(true);
        bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);

    }

    public static void printfootorigen() {
        int alignment = BixolonPrinter.ALIGNMENT_LEFT;
        int attribute = 1;
        attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_A;
        int size = 0;

        bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);

        String cadena = "\n\nEste documento es un comprobante unicamente \ninformativo para el Sistema de Administración de \nObra, no representa un compromiso de pago.";
        bixolonPrinterApi.printText(cadena, BixolonPrinter.ALIGNMENT_CENTER, attribute, size, false);
        bixolonPrinterApi.lineFeed(4, false);
        bixolonPrinterApi.cutPaper(true);
        bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);

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
                            connectedPrinter = true;
                            if(imprimir) {
                                btnImprimir.performClick();

                            }

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
                        Toast.makeText(getApplicationContext(), "No paired device",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Set<BluetoothDevice> pairedDevices = (Set<BluetoothDevice>) msg.obj;
                        DialogManager.showBluetoothDialog(SuccessDestinoActivity.this, bixolonPrinterApi, (Set<BluetoothDevice>) msg.obj);
                    }
                    break;

                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_PRINT_COMPLETE");
                    Toast.makeText(getApplicationContext(),"Impresión Completa.", Toast.LENGTH_LONG).show();
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

    public void fillInfo() {
        inicio = getIntent().getIntExtra("idInicio", 0);
        idViaje = getIntent().getIntExtra("idViaje", 0);
        if(idViaje != 0) {
            Viaje viaje = new Viaje(getApplicationContext());
            viaje = viaje.find(idViaje);
            Motivo motivoss = new Motivo(getApplicationContext());
            if(viaje.deductiva.equals("0")) {
                motivo.setText("NA");
            }else{
                motivoss.find(Integer.valueOf(viaje.idmotivo));
                motivo.setText(motivoss.descripcion);
            }

            textViewCamion.setText(viaje.camion.economico);
            textViewCubicacion.setText(viaje.camion.capacidad + " m3");
            textViewMaterial.setText(viaje.material.descripcion);
            textViewOrigen.setText(viaje.origen.descripcion);
            textViewFechaHoraSalida.setText(viaje.fechaSalida + " " + viaje.horaSalida);
            textViewDestino.setText(viaje.tiro.descripcion);
            textViewFechaHoraLlegada.setText(viaje.fechaLlegada + " " + viaje.horaLlegada);
            textViewRuta.setText(viaje.ruta.toString());
            textViewObservaciones.setText(viaje.observaciones);
            textViewDeductiva.setText(viaje.deductiva);

        }
        else if(inicio != 0){
            InicioViaje in = new InicioViaje(getApplicationContext());
            in = in.find(inicio);

            textViewCamion.setText(in.camion.economico);
            textViewCubicacion.setText(in.camion.capacidad + " m3");
            textViewMaterial.setText(in.material.descripcion);
            textViewOrigen.setText(in.origen.descripcion);
            textViewFechaHoraSalida.setText(in.fecha_origen);
            textViewDestino.setVisibility(View.GONE);
            textViewFechaHoraLlegada.setVisibility(View.GONE);
            textViewRuta.setVisibility(View.GONE);
            textViewObservaciones.setVisibility(View.GONE);
            textViewDeductiva.setVisibility(View.GONE);
            btnImagenes.setVisibility(View.GONE);
            tipo_usuario = true;
            textDestino.setVisibility(View.GONE);
            textFechaDestino.setVisibility(View.GONE);
            textRuta.setVisibility(View.GONE);
            textDeductiva.setVisibility(View.GONE);
            textMotivo.setVisibility(View.GONE);
            textObservacion.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
            btnImprimir.setText("IMPRIMIR COMPROBANTE");

        }


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

    @Override
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
                                    Intent mainActivity;
                                    Integer tipo = usuario.getTipo_permiso();
                                    if(tipo == 0){
                                        mainActivity = new Intent(getApplicationContext(), SetOrigenActivity.class);
                                        startActivity(mainActivity);
                                    }else if(tipo == 1){
                                        mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(mainActivity);
                                    }

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

}
