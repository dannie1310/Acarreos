package mx.grupohi.acarreos.TiposTag;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

import java.io.IOException;
import java.math.BigInteger;

import mx.grupohi.acarreos.Util;

/**
 * Created by DBENITEZ on 26/03/2018.
 */

public class TagClassic {

    Tag tag;
    Context context;
    private byte[] ky= hexStringToByteArray("773A7DB80405FF078069773A7DB80405");
    private byte[] kyUID= hexStringToByteArray("773A7DB80405FF05A069773A7DB80405");
    private byte[] pw= hexStringToByteArray("773A7DB80405");
    private byte[] def= hexStringToByteArray("FFFFFFFFFFFFFF078069FFFFFFFFFFFF");
    String UID;
    Integer idcamion;
    Integer idproyecto;
    Integer idmaterial;
    Integer idorigen;
    String fecha;
    String usuario;
    String tipo_viaje;
    String volumen;
    String idmotivo = "0";
    String tipo_perfil;
    String volumen_entrada;
    String motivo_entrada = "0";

    public TagClassic(Tag tag, Context context) {
        this.context = context;
        this.tag = tag;
        cargaInicial();
    }

    private void cargaInicial(){
        UID = byteArrayToHexString(tag.getId());
        String camion_proyecto = leerSector(0, 1).replace(" ", "");
        if (camion_proyecto.length() == 8) {
            idcamion = Util.getIdCamion(camion_proyecto, 4);
            idproyecto = Util.getIdProyecto(camion_proyecto, 4);
        } else {
            idcamion = Util.getIdCamion(camion_proyecto, 5);
            idproyecto = Util.getIdProyecto(camion_proyecto, 5);
        }
        String material_origen = leerSector(1, 4);
        idmaterial = Util.getIdMaterial(material_origen);
        idorigen = Util.getIdOrigen(material_origen);
        fecha = leerSector(1,5);
        usuario = leerSector(1,6);
        tipo_viaje = leerSector(2,9);
        volumen = leerSector(3,12);
        tipo_perfil = leerSector(3,14);
        volumen_entrada = leerSector(4,16);
    }

    private String leerSector (int sector, int bloque) {
        byte[] toRead=null;
        String aux="";
        MifareClassic mf = MifareClassic.get(tag);
        try {
            mf.connect();
            boolean auth = false;
            auth = mf.authenticateSectorWithKeyA(sector, pw);
            if (auth) {
                toRead = mf.readBlock(bloque);
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
            }
            mf.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return  aux;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] byteArray){
        return String.format("%0" + (byteArray.length * 2) + "X", new BigInteger(1,byteArray));
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public Integer getIdcamion() {
        return idcamion;
    }

    public void setIdcamion(Integer idcamion) {
        this.idcamion = idcamion;
    }

    public Integer getIdproyecto() {
        return idproyecto;
    }

    public void setIdproyecto(Integer idproyecto) {
        this.idproyecto = idproyecto;
    }

    public Integer getIdmaterial() {
        return idmaterial;
    }

    public void setIdmaterial(Integer idmaterial) {
        this.idmaterial = idmaterial;
    }

    public Integer getIdorigen() {
        return idorigen;
    }

    public void setIdorigen(Integer idorigen) {
        this.idorigen = idorigen;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getTipo_viaje() {
        return tipo_viaje;
    }

    public void setTipo_viaje(String tipo_viaje) {
        this.tipo_viaje = tipo_viaje;
    }

    public String getVolumen() {
        return volumen;
    }

    public void setVolumen(String volumen) {
        this.volumen = volumen;
    }

    public String getTipo_perfil() {
        return tipo_perfil;
    }

    public void setTipo_perfil(String tipo_perfil) {
        this.tipo_perfil = tipo_perfil;
    }

    public String getVolumen_entrada() {
        return volumen_entrada;
    }

    public void setVolumen_entrada(String volumen_entrada) {
        this.volumen_entrada = volumen_entrada;
    }

}
