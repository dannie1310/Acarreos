package mx.grupohi.acarreos;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;


/**
 * Created by Usuario on 29/09/2016.
 */

public class NFCTag {

    private Tag NFCTag;
    Context context;
    private byte[] ky= hexStringToByteArray("773A7DB80405FF078069773A7DB80405");
    private byte[] kyUID= hexStringToByteArray("773A7DB80405FF05A069773A7DB80405");
    private byte[] pw= hexStringToByteArray("773A7DB80405");
    private byte[] def= hexStringToByteArray("FFFFFFFFFFFFFF078069FFFFFFFFFFFF");

    public NFCTag(Tag NFCTag, Context context) {
        this.context=context;
        this.NFCTag = NFCTag;
    }

    void write(String text, Tag tag) throws IOException {
        MifareClassic mfc = MifareClassic.get(tag);

        mfc.connect();
        int x = 0;
        int y = 0;
        int iw;
        int z = 1;
        int block;
        int auxBlock = 2;
        boolean auth = false;
        byte[] value = text.getBytes();
        System.out.println(value.length);
        if (value.length <= 752) {
            while (x != value.length) {
                if (y < 16) {
                    auth = mfc.authenticateSectorWithKeyA(y, pw);
                    if (auth) {
                        byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];
                        for (block = 0; block < auxBlock; block++) {
                            for (iw = 0; iw < MifareClassic.BLOCK_SIZE; iw++) {
                                if (x < value.length) {
                                    toWrite[iw] = value[x];
                                    x++;

                                } else {
                                    toWrite[iw] = 0;
                                }
                            }
                            mfc.writeBlock(z + block, toWrite);
                            toWrite = new byte[MifareClassic.BLOCK_SIZE];
                        }
                        if (z == 1) {
                            z = z + block + 1;
                            auxBlock = auxBlock + 1;
                        } else {
                            z = z + block + 1;
                        }
                    }
                    y = y + 1;
                }
            }
            Toast.makeText(context, context.getString(R.string.tag_configurado), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.error_tag_capacidad_almacenamiento), Toast.LENGTH_LONG).show();
        }
        mfc.close();
    }

    boolean writeID(Tag tag, int sector, String mensaje) throws  IOException {
        MifareClassic mfc = MifareClassic.get(tag);
        int bloque = mfc.sectorToBlock(sector);
        int x = 0;
        int auxBloque = 0;

        mfc.connect();
        boolean auth = false;
        byte[] value = mensaje.getBytes();
        auth = mfc.authenticateSectorWithKeyA(sector, pw);
        if (auth) {
            while (x != value.length) {
                byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];
                for (int iw = 0; iw < MifareClassic.BLOCK_SIZE; iw++) {
                    if (x < value.length) {
                        toWrite[iw] = value[x];
                        x++;
                    } else {
                        toWrite[iw] = 0;
                    }
                }
                mfc.writeBlock(bloque + auxBloque, toWrite);
                auxBloque += 1;
                toWrite = new byte[MifareClassic.BLOCK_SIZE];
            }
        }
        mfc.close();
        return true;
    }

    String concatenar(String idCamion, String idProyecto){
        String resultado="";
        String aux =idCamion;
        String aux1=idProyecto;
        for(int i=idCamion.length(); i<4;i++){
            aux= 0 + aux;
        }
        for(int i=idProyecto.length(); i<4;i++){
            aux1= 0 + aux1;
        }
        resultado= aux+aux1;
        return resultado;
    }

    String read(Tag tag) throws IOException {// Leer toda la informacion de la tag
        int y = 0;
        int z = 1;
        byte[] toRead = null;
        int block;
        int auxBlock = 2;
        String aux = "";
        MifareClassic mf = MifareClassic.get(tag);
        mf.connect();
        boolean auth = false;
        for (y = 0; y < 16; y++) {
            auth = mf.authenticateSectorWithKeyA(y, pw);
            if (auth) {
                for (block = 0; block < auxBlock; block++) {
                    toRead = mf.readBlock(block + z);
                    if (toRead != null) {
                        byte[] limpio = new byte[toRead.length];
                        for (int i = 0; i < toRead.length; i++) {
                            if (toRead[i] != 0) {
                                limpio[i] += toRead[i];
                            } else {
                                limpio[i] += ' ';
                            }
                        }
                        String s = new String(limpio);
                        aux += s;
                    }
                }
                if (z == 1) {
                    z += block + 1;
                    auxBlock = auxBlock + 1;
                } else {
                    z += block + 1;
                }
            }
        }
        mf.close();
        return aux;
    }

    public String readSector (Tag tag_1, int sector, int bloque) throws IOException { //Leer solo un sector y bloque especifico
        byte[] toRead = null;
        String aux = "";
        MifareClassic mf = MifareClassic.get(NFCTag);
        mf.connect();
        boolean auth = false;
        auth = mf.authenticateSectorWithKeyA(sector, pw);
        if (auth) {
            toRead = mf.readBlock(bloque);
            byte[] limpio = new byte[toRead.length];
            if (toRead != null) {
                for (int i = 0; i < toRead.length; i++) {
                    if (toRead[i] != 0) {
                        limpio[i] += toRead[i];
                    } else {
                        limpio[i] += ' ';
                    }
                }
                String s = new String(limpio);
                aux += s;
            }
        }
        mf.close();
        return aux;
    }

    void clean(Tag tag) throws  IOException {
        MifareClassic mfc = MifareClassic.get(tag);
        mfc.connect();
        int x = 0;
        int y = 0;
        int iw;
        int z = 1;
        int block;
        int auxBlock = 2;
        boolean auth = false;
        for (y = 0; y < 16; y++) {
            auth = mfc.authenticateSectorWithKeyA(y, pw);
            if (auth) {
                byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];

                for (block = 0; block < auxBlock; block++) {

                    for (iw = 0; iw < MifareClassic.BLOCK_SIZE; iw++) {
                        toWrite[iw] = 0;
                    }
                    mfc.writeBlock(z + block, toWrite);
                    toWrite = new byte[MifareClassic.BLOCK_SIZE];
                }
                if (z == 1) {
                    z = z + block + 1;
                    auxBlock = auxBlock + 1;
                } else {
                    z = z + block + 1;
                }
            }
        }
        Toast.makeText(context, context.getString(R.string.tag_configurado), Toast.LENGTH_LONG).show();
        mfc.close();
    }

    Boolean cleanSector(Tag tag, int sector) throws IOException {
        MifareClassic mfc = MifareClassic.get(tag);
        int bloque = mfc.sectorToBlock(sector);
        mfc.connect();
        int iw;
        int z = 1;
        int block;
        boolean auth = false;
        auth = mfc.authenticateSectorWithKeyA(sector, pw);
        if (auth) {
            byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];

            for (block = 0; block < 3; block++) {

                for (iw = 0; iw < MifareClassic.BLOCK_SIZE; iw++) {
                    toWrite[iw] = 0;
                }
                mfc.writeBlock(bloque + block, toWrite);
                toWrite = new byte[MifareClassic.BLOCK_SIZE];
            }
        }
        // Toast.makeText(context, context.getString(R.string.tag_configurado), Toast.LENGTH_LONG).show();
        mfc.close();
        return true;
    }

    String idTag(Tag tag) throws IOException {
        byte[] toRead = null;
        byte[] send = new byte[4];
        String aux = "";
        MifareClassic mf = MifareClassic.get(tag);
        mf.connect();
        boolean auth = false;
        auth = mf.authenticateSectorWithKeyA(0, pw);
        if (auth == true) {
            toRead = mf.readBlock(0);
            for (int i = 0; i < 4; i++) {
                send[i] = toRead[i];
            }
            aux = byteArrayToHexString(send);
        } else {
            aux = "";
            auth = mf.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT);
            if (auth) {
                toRead = mf.readBlock(0);
                for (int i = 0; i < 4; i++) {
                    send[i] = toRead[i];
                }
                aux = byteArrayToHexString(send);
            }
        }
        mf.close();
        System.out.println("idTag " + aux);
        return aux;
    }

    public static String byteArrayToHexString(byte[] byteArray){
        return String.format("%0" + (byteArray.length * 2) + "X", new BigInteger(1,byteArray));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    boolean changeKey(Tag tag) throws IOException {
        MifareClassic mf = MifareClassic.get(tag);
        int y = 0;
        mf.connect();
        boolean auth = false;
        boolean auth_cambio = false;
        for (y = 0; y < 16; y++) {
            auth = mf.authenticateSectorWithKeyA(y, MifareClassic.KEY_DEFAULT);
            int bloque = mf.sectorToBlock(y);
            if (auth == true) {
                if (y == 0) {
                    mf.writeBlock(bloque + 3, kyUID);
                } else {
                    mf.writeBlock(bloque + 3, ky);
                }
            } else {
                auth_cambio = mf.authenticateSectorWithKeyA(y, pw);
                if (auth_cambio == true) {
                    if (y == 0) {
                        mf.writeBlock(bloque + 3, kyUID);
                    }
                }
            }

        }
        mf.close();
        return true;
    }

    boolean writeSector(Tag tag, int sector, int bloque, String mensaje) throws IOException {
        MifareClassic mfc = MifareClassic.get(tag);
        int bq = mfc.sectorToBlock(sector);
        bq = bq + 3;
        if (bloque == bq) {
            return false;
        }
        int x = 0;
        mfc.connect();
        boolean auth = false;
        byte[] value = mensaje.getBytes();
        if (auth) {
            byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];
            for (int iw = 0; iw < MifareClassic.BLOCK_SIZE; iw++) {
                if (x < value.length) {
                    toWrite[iw] = value[x];
                    x++;
                } else {
                    toWrite[iw] = 0;
                }
            }
            mfc.writeBlock(bloque, toWrite);
        } else {
            auth = mfc.authenticateSectorWithKeyA(sector, pw);
            if (auth) {
                byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];
                for (int iw = 0; iw < MifareClassic.BLOCK_SIZE; iw++) {
                    if (x < value.length) {
                        toWrite[iw] = value[x];
                        x++;
                    } else {
                        toWrite[iw] = 0;
                    }
                }
                mfc.writeBlock(bloque, toWrite);
            }
        }
        mfc.close();
        return true;
    }

    void formatear(Tag tag, int sector) throws IOException {
        MifareClassic mf = MifareClassic.get(tag);
        int bloque = mf.sectorToBlock(sector);
        mf.connect();
        boolean auth = false;
        auth = mf.authenticateSectorWithKeyA(sector, pw);
        if (auth) {
            mf.writeBlock(bloque + 3, def);
        }
        mf.close();
    }


    byte[] readSector (MifareClassic mf, Boolean auth, Tag tag, int sector, int bloque) throws IOException { //Leer solo un sector y bloque especifico
        byte[] toRead = null;
        String aux = "";
        if (mf.authenticateSectorWithKeyA(sector, pw)) {
            toRead = mf.readBlock(bloque);
        }
        // mf.close();
        return toRead;
    }

    JSONObject destino (Tag mytag) throws IOException {
        Boolean auth = false;
        MifareClassic mf = MifareClassic.get(mytag);
        byte[] viaje = new byte[0];
        byte[] taginfo = new byte[0];
        JSONObject JSON = new JSONObject();
        mf.connect();
        viaje = readSector(mf, auth, mytag, 2, 8);
        taginfo = readSector(mf, auth, mytag, 0, 1);
        // JSON.put("viaje", readSector(mf, auth, mytag, 2, 8));
        // JSON.put("tagInfo", readSector(mf, auth, mytag, 0, 1));
        mf.close();
        try {
            JSON.put("viaje", cambio(viaje));
            JSON.put("tagInfo", cambio(taginfo));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return JSON;
    }

    public String cambio(byte[] toRead){
        String aux = "";
        byte[] limpio=new byte[toRead.length];
        if (toRead != null) {
            for (int i = 0; i < toRead.length; i++) {
                if (toRead[i] != 0) {
                    limpio[i] += toRead[i];
                } else {
                    limpio[i] += ' ';
                }
            }
            String s= new String(limpio);
            aux += s;
        }
        return aux;
    }

}
