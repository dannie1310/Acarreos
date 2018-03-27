package mx.grupohi.acarreos.TiposTag;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;

import java.math.BigInteger;

import mx.grupohi.acarreos.Util;

/**
 * Created by DBENITEZ on 26/03/2018.
 */

public class TagUltralight {
    Tag tag;
    Context context;
    String UID;
    Integer idcamion;
    Integer idproyecto;
    String idmaterial;
    String idorigen;
    String fecha;
    String usuario;
    String tipo_viaje;
    String volumen;
    String idmotivo = "0";
    String tipo_perfil;
    String volumen_entrada;
    String motivo_entrada = "0";

    public TagUltralight(Tag tag, Context context) {
        this.context = context;
        this.tag = tag;
        cargaInicial();
    }

    private void cargaInicial(){
        UID = byteArrayToHexString(tag.getId());
        String camion_proyecto = leerSector(4)+leerSector(5)+leerSector(6);
        if (camion_proyecto.length() == 8) {
            idcamion = Util.getIdCamion(camion_proyecto, 4);
            idproyecto = Util.getIdProyecto(camion_proyecto, 4);
        } else {
            idcamion = Util.getIdCamion(camion_proyecto, 8);
            idproyecto = Util.getIdProyecto(camion_proyecto, 8);
        }
        idmaterial = leerSector(7);
        idorigen = leerSector(8);
        fecha = leerSector(9)+leerSector(10)+leerSector(11)+ leerSector(12);
        usuario = leerSector(13);
        tipo_viaje = leerSector(15);
        volumen = leerSector(16);
        tipo_perfil = leerSector(18);
        volumen_entrada = leerSector(19);
    }

    public String leerSector(int page){
        MifareUltralight mf=MifareUltralight.get(tag);
        byte[] toRead = null;
        byte[] auxRead =  new byte[4];
        String aux="";
        try{
            mf.connect();
            toRead = mf.readPages(page);
            for(int i=0; i<4; i++) {
                if (toRead[i] != 0) {
                    auxRead[i] = toRead[i];
                }else{
                    auxRead[i] = ' ';
                }
            }
            String x = byteArrayToHexString(auxRead);
            if(x.equalsIgnoreCase("00000000")){
                aux=null;
            }
            else {
                String s = new String(auxRead);
                aux += s;
                toRead = null;
            }
            mf.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        String respuesta = aux.replace(" ","");
        return respuesta;
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

    public String getIdmaterial() {
        return idmaterial;
    }

    public void setIdmaterial(String idmaterial) {
        this.idmaterial = idmaterial;
    }

    public String getIdorigen() {
        return idorigen;
    }

    public void setIdorigen(String idorigen) {
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
