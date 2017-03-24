package mx.grupohi.acarreos;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static mx.grupohi.acarreos.SuccessDestinoActivity.MESSAGE_END_WORK;

/**
 * Creado por JFEsquivel on 25/10/2016.
 */



public class PrintViaje implements Runnable {

    public static Context context;
    
    private Usuario usuario;
    private Viaje viaje;
    private static Bitmap bitmap;
    private String code;

    public static BixolonPrinter bixolonPrinterApi;
    private Boolean connectedPrinter = false;
    private static final long PRINTING_SLEEP_TIME = 300;
    static final int MESSAGE_START_WORK = Integer.MAX_VALUE - 2;
    static final int MESSAGE_END_WORK = Integer.MAX_VALUE - 3;
    private final int LINE_CHARS = 42 + 22;
    
    private List<String> pairedPrinters = new ArrayList<String>();


    PrintViaje(Context context, Viaje viaje, Usuario usuario) {
        this.context = context;
        BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.logo_ghi);
        bitmap = drawable.getBitmap();
        this.viaje = viaje;
        this.usuario = usuario;
        this.bixolonPrinterApi = new BixolonPrinter(context, handler, null);
    }

    @Override
    public void run() {
        try {
            bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
            Bitmap fewlapsBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_success);

            Thread.sleep(PRINTING_SLEEP_TIME);
            printheadproyecto(usuario.getDescripcion());
            bixolonPrinterApi.lineFeed(1,true);
            printTextTwoColumns("Camion: ", viaje.camion.economico + "\n");
            printTextTwoColumns("Cubicación: ", viaje.camion.capacidad + "\n");

            printTextTwoColumns("Material: ", viaje.material.descripcion + "\n");
            printTextTwoColumns("Origen: ", viaje.origen.descripcion + "\n");
            printTextTwoColumns("Fecha de Salida: ", viaje.fechaSalida + "\n");

            printTextTwoColumns("Destino: ", viaje.tiro.descripcion + "\n");
            printTextTwoColumns("Fecha Llegada: ", viaje.fechaLlegada + "\n");
            printTextTwoColumns("Ruta: ", viaje.ruta + "\n");
            printTextTwoColumns("Observaciones: ", viaje.observaciones + "\n");

            bixolonPrinterApi.lineFeed(1,true);
            printfoot("Checador: "+ usuario.getNombre(), Viaje.getCode(viaje.idViaje));
            bixolonPrinterApi.printQrCode(viaje.getCode(viaje.idViaje), BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.QR_CODE_MODEL1, 5, false);

            bixolonPrinterApi.lineFeed(2, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            Log.i("Handler: - ", msg.what + " " + msg.arg1 + " " + msg.arg2);

            switch (msg.what) {

                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_STATE_CHANGE");
                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:
                            // updateScreenStatus(layoutPrinterReady);
                            Log.i("Handler", "BixolonPrinter.STATE_CONNECTED");
                            connectedPrinter = true;
                            // updateScreenStatus(layoutPrinterReady);
                            break;

                        case BixolonPrinter.STATE_CONNECTING:
                            // updateScreenStatus(layoutLoading);
                            Log.i("Handler", "BixolonPrinter.STATE_CONNECTING");
                            connectedPrinter = false;
                            break;

                        case BixolonPrinter.STATE_NONE:
                            // updateScreenStatus(layoutLoading);
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
                    // debugTextView.setText(msg.getData().getString(BixolonPrinter.KEY_STRING_DEVICE_NAME));
                    Log.i("Handler", "BixolonPrinter.MESSAGE_DEVICE_NAME - " + msg.getData().getString(BixolonPrinter.KEY_STRING_DEVICE_NAME));
                    Toast.makeText(context, "Impresora Conectada como: " +msg.getData().getString(BixolonPrinter.KEY_STRING_DEVICE_NAME), Toast.LENGTH_SHORT).show();
                    break;

                case BixolonPrinter.MESSAGE_TOAST:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_TOAST - " + msg.getData().getString("toast"));
                    // Toast.makeText(context,"BixolonPrinter.MESSAGE_TOAST - " + msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    break;

                // The list of paired printers
                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET");
                    if (msg.obj == null) {
                        Toast.makeText(context, "No paired device",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Set<BluetoothDevice> pairedDevices = (Set<BluetoothDevice>) msg.obj;
                        DialogManager.showBluetoothDialog(context, bixolonPrinterApi, (Set<BluetoothDevice>) msg.obj);
                    }
                    break;

                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_PRINT_COMPLETE");
                    Toast.makeText(context,"Impresión Completa.", Toast.LENGTH_SHORT).show();
                    break;

                case BixolonPrinter.MESSAGE_COMPLETE_PROCESS_BITMAP:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_COMPLETE_PROCESS_BITMAP");
                    break;

                case MESSAGE_START_WORK:
                    Log.i("Handler", "MESSAGE_START_WORK");
                    Toast.makeText(context,"Iniciando Impresión ", Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_END_WORK:
                    Log.i("Handler", "MESSAGE_END_WORK");
                    Toast.makeText(context,"Finalizado ", Toast.LENGTH_SHORT).show();
                    break;

                case BixolonPrinter.MESSAGE_NETWORK_DEVICE_SET:
                    Log.i("Handler", "BixolonPrinter.MESSAGE_NETWORK_DEVICE_SET");
                    if (msg.obj == null) {
                        Toast.makeText(context, "No connectable device", Toast.LENGTH_SHORT).show();
                    }
                    // DialogManager.showNetworkDialog(PrintingActivity.this, (Set<String>) msg.obj);
                    break;
            }
        }
    };

    public static void printheadproyecto(String text) {
        bixolonPrinterApi.printBitmap(bitmap, BixolonPrinter.ALIGNMENT_CENTER,260, 50, true);

        int alignment = BixolonPrinter.ALIGNMENT_CENTER;

        int attribute = 0;
        attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_B;

        int size = 1;
        bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        bixolonPrinterApi.printText(text, alignment, attribute, size, false);
        bixolonPrinterApi.lineFeed(1, false);

        bixolonPrinterApi.cutPaper(true);
        bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);
    }

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

    public static void printfoot(String text, String codex) {
        int alignment = BixolonPrinter.ALIGNMENT_LEFT;
        int attribute = 1;
        attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_A;
        int size = 0;

        bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        bixolonPrinterApi.printText(text, alignment, attribute, size, false);
        bixolonPrinterApi.lineFeed(1, false);
        bixolonPrinterApi.print1dBarcode(codex.toUpperCase(), BixolonPrinter.BAR_CODE_CODE93, BixolonPrinter.ALIGNMENT_CENTER, 4, 200, BixolonPrinter.HRI_CHARACTER_NOT_PRINTED, true);
        bixolonPrinterApi.formFeed(true);
        bixolonPrinterApi.printText(codex.toUpperCase(), BixolonPrinter.ALIGNMENT_CENTER, attribute, size, false);

        String cadena = "\n\nEste documento es un comprobante de recepción \nde materiales del Sistema de Administración de \nObra, no representa un compromiso de pago hasta \nsu validación contra las remisiones del \nproveedor y la revisión de factura.";
        bixolonPrinterApi.printText(cadena, BixolonPrinter.ALIGNMENT_LEFT, attribute, size, false);
        bixolonPrinterApi.lineFeed(3, false);
        bixolonPrinterApi.cutPaper(true);
        bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);

    }


}
