package mx.grupohi.acarreos.TiposTag;

/**
 * Created by DBENITEZ on 26/03/2018.
 */

public class TagNFC {
    Integer tipo;
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