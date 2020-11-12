package mx.grupohi.acarreos.TiposTag;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

/**
 * Created by DBENITEZ on 26/03/2018.
 */
@SuppressWarnings("serial")
public class TagNFC implements Serializable{
    Context context;
    Integer tipo;
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
    String latitud_origen;
    String longitud_origen;
    String latitud_tiro;
    String longitud_tiro;

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
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
    public String getIdmotivo() {
        return idmotivo;
    }

    public void setIdmotivo(String idmotivo) {
        this.idmotivo = idmotivo;
    }

    public String getMotivo_entrada() {
        return motivo_entrada;
    }

    public void setMotivo_entrada(String motivo_entrada) {
        this.motivo_entrada = motivo_entrada;
    }
    public String getLatitud_origen() {
        return latitud_origen;
    }

    public String getLongitud_origen() {
        return longitud_origen;
    }

    public String getLatitud_tiro() {
        return latitud_tiro;
    }

    public String getLongitud_tiro() {
        return longitud_tiro;
    }

    public void setLatitud_origen(String latitud_origen) {
        this.latitud_origen = latitud_origen;
    }

    public void setLongitud_origen(String longitud_origen) {
        this.longitud_origen = longitud_origen;
    }

    public void setLatitud_tiro(String latitud_tiro) {
        this.latitud_tiro = latitud_tiro;
    }

    public void setLongitud_tiro(String longitud_tiro) {
        this.longitud_tiro = longitud_tiro;
    }

}
