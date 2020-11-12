package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;
import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by DBENITEZ on 27/07/2017.
 */

public class ImprimirTicket  extends AsyncTask<Void, Void, Boolean> {

    private GPSTracker gps;
    public static BixolonPrinter bixolonPrinterApi;
    public static int milisegundos = 7200;
    private static final long PRINTING_TIME = 2100;
    private static final long PRINTING_SLEEP_TIME = 300;
    static final int MESSAGE_START_WORK = Integer.MAX_VALUE - 2;
    static final int MESSAGE_END_WORK = Integer.MAX_VALUE - 3;
    private final int LINE_CHARS = 64;
    private boolean imp= false;
    private static Boolean connectedPrinter = false;
   Context context;
    ProgressDialog progressDialog;
    JSONObject dato;
    Integer idViaje;
    Integer idInicio=null;
    Integer num;
    Bitmap bitmap;
    Usuario uss;
    Integer sumaDeductiva=0;
    Boolean dat;

    ImprimirTicket(Context context, ProgressDialog progressDialog, BixolonPrinter bixolonPrinterApi, JSONObject datos, Bitmap b) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.bixolonPrinterApi = bixolonPrinterApi;
        this.dato = datos;
        this.bitmap = b;
        gps = new GPSTracker(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Usuario uss = new Usuario(context);
        uss= uss.getUsuario();
        Double latitud = gps.getLatitude();
        Double longitud = gps.getLongitude();

        try {

            bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
            //Thread.sleep(PRINTING_SLEEP_TIME);

            bixolonPrinterApi.lineFeed(1, true);
            Thread.sleep(400);
            try {
                bixolonPrinterApi.printBitmap(bitmap, BixolonPrinter.ALIGNMENT_CENTER, 220, 50, true);
                bixolonPrinterApi.lineFeed(1, true);
            }catch (Exception e){
                Crashlytics.logException(e);
                Toast.makeText(context, "Error Impresión de logo", Toast.LENGTH_LONG).show();
            }

            if (!dato.getString("21").equals("null")) {
                printheadproyecto(dato.getString("21"));
            }

            bixolonPrinterApi.lineFeed(1, true);
            printTextTwoColumns("Proyecto: ", dato.getString("1") + " \n");
            printTextTwoColumns("Camión: ", dato.getString("2") + " \n");
            printTextTwoColumns("Cubicación Camión: ", dato.getString("3") + " m3\n");
            printTextTwoColumns("Volumen Real(Recibido): ", dato.getString("10") + " m3\n");
            if (dato.getString("20").equals("NULL")) {
                dat = Util.getFechaImprocedente(dato.getString("6"), dato.getString("8"));
                if(dat == true){
                    bixolonPrinterApi.printText("V I A J E   I M P R O C E D E N T E\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_C, 0, false);
                }
            }
            /*if(!dato.getString("31").equals("0") && !dato.getString("31").equals("NULL")) {
                printTextTwoColumns("              -----  Detalles deductiva  ----", "\n");
                sumaDeductiva = sumaDeductiva + dato.getInt("31");
                printTextTwoColumns("D. Origen: ", dato.getString("31") + " m3\n");
                printTextTwoColumns("Motivo Origen: ", dato.getString("33") + "\n");
            }
            if(!dato.getString("32").equals("0") && !dato.getString("32").equals("NULL")) {
                sumaDeductiva = sumaDeductiva + dato.getInt("32");
                printTextTwoColumns("D. Entrada: ", dato.getString("32") + " m3\n");
                printTextTwoColumns("Motivo Entrada: ", dato.getString("34") + "\n");
            }
            if(!dato.getString("10").equals("0") && !dato.getString("10").equals("NULL")){
                sumaDeductiva = sumaDeductiva + dato.getInt("10");
            }
            printTextTwoColumns("D. Salida: ", dato.getString("10") + " m3\n");
            printTextTwoColumns("Motivo Salida: ", dato.getString("11") + "\n");
            printTextTwoColumns("D. Total: ", sumaDeductiva + " m3\n");

            if(dato.getInt("3")== 0){
                printTextTwoColumns("Cubicacion Real: ", "Datos del camión no actualizados.\n");
            }else if (dato.getInt("3")!= 0 && sumaDeductiva<dato.getInt("3")) {
                printTextTwoColumns("Cubicacion Real: ", dato.getInt("3")-sumaDeductiva + " m3\n");
            }else{
                printTextTwoColumns("Cubicacion Real: ", " Deductivas mayores al cubicaje del camión.\n");
            }
            */
            printTextTwoColumns("Material: ", dato.getString("4") + "\n");
            printTextTwoColumns("Origen: ", dato.getString("5") + "\n");
            printTextTwoColumns("Fecha de Salida: ", dato.getString("6") + "\n");

            if (!dato.getString("23").isEmpty()) {
                printTextTwoColumns("Folio de Vale de Mina: ", dato.getString("23") + "\n");
            }else {
                printTextTwoColumns("Folio de Vale de Mina: ", "------\n");
            }
            if(!dato.getString("24").isEmpty()) {
                printTextTwoColumns("Folio de Seguimiento: ", dato.getString("24") + "\n");
            }else {
                printTextTwoColumns("Folio de Seguimiento: ", "------\n");
            }
            if(!dato.getString("35").equals("NULL") && !dato.getString("36").equals("NULL")) {
                printTextTwoColumns("Coordenadas GD Origen: ", "(" + dato.getString("35") + "," + dato.getString("36") + ")\n");
            }
            if (dato.getString("20").equals("NULL")) {
                idViaje = dato.getInt("0");
                dat = Util.getFechaImprocedente(dato.getString("6"), dato.getString("8"));
                if(dat == true){
                    bixolonPrinterApi.printText("V I A J E   I M P R O C E D E N T E\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_C, 0, false);
                }
                printTextTwoColumns("Destino: ", dato.getString("7") + "\n");
                printTextTwoColumns("Fecha Llegada: ", dato.getString("8") + "\n");
                printTextTwoColumns("Ruta: ", dato.getString("9") + "\n");
                // if(textViewObservaciones.getText().length()!=0) {
                //printTextTwoColumns("Deductiva: ", dato.getString("10") + "\n");
                //printTextTwoColumns("Motivo Deductiva: ", dato.getString("11") + "\n");
                printTextTwoColumns("Observaciones: ", dato.getString("12") + "\n");
                if(dat == true){
                    bixolonPrinterApi.printText("V I A J E   I M P R O C E D E N T E\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_C, 0, false);
                }
                if(!dato.getString("37").equals("NULL") && !dato.getString("38").equals("NULL")) {
                    printTextTwoColumns("Coordenadas GD Tiro: ", "(" + dato.getString("37") + "," + dato.getString("38") + ")\n");
                }
                if (dato.getInt("13") == 3) {
                    printTextTwoColumns("Checador: " + dato.getString("14"), "   "+dato.getString("15") + "\n");
                    printTextTwoColumns("Versión: ", String.valueOf(dato.getString("16")) + "\n");
                    bixolonPrinterApi.printText("TIRO LIBRE ABORDO\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_C, 0, false);
                } else {
                    printTextTwoColumns("Checador Inicio: ", dato.getString("17") + "\n");
                    printTextTwoColumns("Checador Cierre: " + dato.getString("14"), "   "+dato.getString("15") + "\n");
                    printTextTwoColumns("Versión: ", String.valueOf(dato.getString("16")) + "\n");
                }
                if(!dato.getString("30").equals("")){
                    printTextTwoColumns("Tipo Viaje: ", dato.getString("30") + "\n");
                }

                Boolean dat = Util.getFechaImprocedente(dato.getString("6"), dato.getString("8"));
                if(dat == true){
                    bixolonPrinterApi.printText("V I A J E   I M P R O C E D E N T E\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 1, false);
                    bixolonPrinterApi.printText("PASAR A MESA DE ACLARACIÓN\n\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 0, false);
                }
                num = Viaje.numImpresion(dato.getInt("0"), context);
                if(num == 0){
                    bixolonPrinterApi.printText("C H O F E R", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
                }
                else if (num == 1){
                    bixolonPrinterApi.printText("C H E C A D O R", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
                }
               else if(num < 5) {
                    Integer numero = num - 1;
                    bixolonPrinterApi.printText("R E I M P R E S I O N " + numero, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
               }

                printfoot(num, "Checador: " + dato.getString("14"), dato.getString("22"), dato.getString("19"));

                bixolonPrinterApi.lineFeed(3, true);
            } else {
                idInicio = dato.getInt("20");
                num = InicioViaje.numImpresion(dato.getInt("20"), context);

                if(uss.tipo_permiso == 1) {
                    printTextTwoColumns("Checador: " + dato.getString("14"), "   "+dato.getString("15") + "\n");
                    printTextTwoColumns("Versión: ", String.valueOf(dato.getString("16")) + "\n");
                    if(dato.getString("30")!= "NULL"){
                        printTextTwoColumns("Tipo Viaje: ", dato.getString("30") + "\n");
                    }
                    if(dato.getInt("26") == 1) {
                        num = InicioViaje.numImpresion(Integer.valueOf(dato.getString("20")), context);
                        if (num == 0) {
                            bixolonPrinterApi.printText("M I N A", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
                        } else if (num == 1) {
                            bixolonPrinterApi.printText("C H E C A D O R", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
                        } else if (num < 5) {
                            Integer numero = num - 1;
                            bixolonPrinterApi.printText("R E I M P R E S I O N " + numero, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
                        }
                    }

                }else {
                    printTextTwoColumns("Checador: " + dato.getString("14"), "   "+dato.getString("15") + "\n");
                    printTextTwoColumns("Versión: ", String.valueOf(dato.getString("16")) + "\n");
                    if(dato.getString("30")!= "NULL"){
                        printTextTwoColumns("Tipo Viaje: ", dato.getString("30") + "\n");
                    }
                }
                printfootorigen(uss.tipo_permiso, Integer.valueOf(dato.getString("26")), num, dato.getString("19"), dato.getString("22"));
            }
            bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Toast.makeText(context, R.string.error_impresion, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        tiempoEspera();
        if(aBoolean) {
            try {
                if(idViaje == null) {
                    boolean c= InicioViaje.updateImpresion(idInicio,num, context);
                }
                if(idInicio == null){
                    boolean c= Viaje.updateImpresion(idViaje, num, context);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void printfootorigen(Integer tipo_esquema, Integer tipo_suministro, Integer impresion, String datos, String codex) {
        int alignment = BixolonPrinter.ALIGNMENT_LEFT;
        int attribute = 1;
        attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_A;
        int size = 0;

        bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);

        if(tipo_esquema == 1 && tipo_suministro  == 1) {
            bixolonPrinterApi.lineFeed(1, false);
            bixolonPrinterApi.print1dBarcode(codex.toUpperCase(), BixolonPrinter.BAR_CODE_CODE39, BixolonPrinter.ALIGNMENT_CENTER, 2, 180, BixolonPrinter.HRI_CHARACTERS_BELOW_BAR_CODE, true);
            if(impresion == 0){
                bixolonPrinterApi.printText("M I N A", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
            }
            else if (impresion == 1){
                bixolonPrinterApi.printText("C H E C A D O R", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
            }
            else {
                Integer numero = impresion - 1;
                bixolonPrinterApi.printText("R E I M P R E S I O N " + numero, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
            }
            bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
            bixolonPrinterApi.lineFeed(2, false);
            bixolonPrinterApi.printQrCode(datos, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.QR_CODE_MODEL2, 5, false);
        }
        String cadena = "\n\nEste documento es un comprobante unicamente \ninformativo para el Sistema de Administración de \nObra, no representa un compromiso de pago.";
        bixolonPrinterApi.printText(cadena, BixolonPrinter.ALIGNMENT_CENTER, attribute, size, false);
        bixolonPrinterApi.lineFeed(4, false);
        bixolonPrinterApi.cutPaper(true);
        bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);

    }

    public static void printfoot(Integer impresion, String text, String codex, String datos) {
        int alignment = BixolonPrinter.ALIGNMENT_LEFT;
        int attribute = 1;
        attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_A;
        int size = 0;

        bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        bixolonPrinterApi.lineFeed(1, false);
        bixolonPrinterApi.print1dBarcode(codex.toUpperCase(), BixolonPrinter.BAR_CODE_CODE39, BixolonPrinter.ALIGNMENT_CENTER, 2, 180, BixolonPrinter.HRI_CHARACTERS_BELOW_BAR_CODE, true);

        if(impresion == 0){
            bixolonPrinterApi.printText("C H O F E R", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
        }
        else if (impresion == 1){
            bixolonPrinterApi.printText("C H E C A D O R", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
        }
        else {
            Integer numero = impresion - 1;
            bixolonPrinterApi.printText("R E I M P R E S I O N " + numero, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.TEXT_ATTRIBUTE_FONT_A, 2, false);
        }

        bixolonPrinterApi.lineFeed(2, false);
        bixolonPrinterApi.printQrCode(datos, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.QR_CODE_MODEL2, 5, false);

        String cadena = "\nEste documento es un comprobante de recepción \nde materiales del Sistema de Administración de \nObra, no representa un compromiso de pago hasta \nsu validación contra las remisiones del \nproveedor y la revisión de factura.";
        bixolonPrinterApi.printText(cadena, BixolonPrinter.ALIGNMENT_CENTER, attribute, size, false);

        bixolonPrinterApi.lineFeed(1, false);

        bixolonPrinterApi.cutPaper(true);
      //  bixolonPrinterApi.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);

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

    public static void printheadproyecto(String text) {
        int alignment = BixolonPrinter.ALIGNMENT_CENTER;

        int attribute = 0;
        attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_C;

        int size = 0;
        bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        bixolonPrinterApi.printText(text, alignment, attribute, size, false);
        // bixolonPrinterApi.lineFeed(1, false);

        bixolonPrinterApi.cutPaper(true);


    }


    public  void tiempoEspera(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                progressDialog.dismiss();
            }
        }, 3000);
    }


}
