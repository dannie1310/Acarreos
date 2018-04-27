package mx.grupohi.acarreos.Destino;

import android.content.ContentValues;
import android.content.Context;
import com.crashlytics.android.Crashlytics;
import mx.grupohi.acarreos.Coordenada;
import mx.grupohi.acarreos.TagModel;
import mx.grupohi.acarreos.TiposTag.TagNFC;
import mx.grupohi.acarreos.Usuario;
import mx.grupohi.acarreos.Util;
import mx.grupohi.acarreos.Viaje;

public class DestinoTiro {
    Context context;
    TagNFC tag_nfc;
    public Integer idViaje;
    public DestinoTiro(Context context, TagNFC tag_nfc) {
        this.context = context;
        this.tag_nfc = tag_nfc;
    }

    public String validarDatosTag() {
        Usuario usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        if (!TagModel.findTAG(context, tag_nfc.getUID())) {
            return "El TAG que intentas configurar no está autorizado para éste proyecto.";
        }
        TagModel datosTagCamion = new TagModel(context);
        datosTagCamion = datosTagCamion.find(tag_nfc.getUID(), tag_nfc.getIdcamion(), tag_nfc.getIdproyecto());
        if (datosTagCamion.estatus != 1) {
            return "El camión " + datosTagCamion.economico + " se encuentra inactivo. Por favor contacta al encargado.";
        }
        if (tag_nfc.getIdproyecto() != usuario.getProyecto()) {
            return "El TAG no pertenece al proyecto del usuario.";
        }
        if (tipoPerfil()) { // Perfil Tiro o Salida
            idViaje =  viajeIncompleto(tag_nfc.getIdcamion());
            if(idViaje!=null) {
                return "viaje inconcluso";
            }else {
                if (!validar(tag_nfc.getIdmaterial()) && !validar(tag_nfc.getIdorigen()) && !tag_nfc.getFecha().equals("") && !tag_nfc.getUsuario().equals("") && !tag_nfc.getVolumen().equals("")) {
                    return "destino";
                }
                //buscar en BD para validar si es un viaje no  finalizado....
                if (validar(tag_nfc.getIdmaterial()) && validar(tag_nfc.getIdorigen()) && tag_nfc.getFecha().equals("") && tag_nfc.getUsuario().equals("") && tag_nfc.getVolumen().equals("")) {
                    return "El TAG que intentas utilizar no cuenta con un origen definido.";
                }
                return "El TAG no cuenta con un origen definido.";
            }
        } else {// Perfil de Tiro Libre a Bordo
            if ((!tag_nfc.getIdmaterial().equals("") && !tag_nfc.getIdorigen().equals(""))&& !tag_nfc.getFecha().equals("") && !tag_nfc.getUsuario().equals("") && !tag_nfc.getVolumen().equals("")) {
                return "El TAG cuenta con un viaje activo, Favor de pasar a un filtro de salida para finalizar el viaje.";
            }
            return "libreAbordo";
        }
    }

    public Boolean validar(String datos){
       if(datos.equals("") || datos.equals("null")){
           return true;
       }else{
           return false;
       }
    }

    public Boolean tipoPerfil(){
        Usuario usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        if(usuario.tipo_permiso == 2 || usuario.tipo_permiso == 5){ // perfil de tiro o salida
            return true;
        }
        if(usuario.tipo_permiso == 3){ //perfil de tiro libre a bordo
            return false;
        }
        return null;
    }

    /**
     * Metodo para guardarlos datos en la base de datos,
     * el Content Values que va a llegar se debe complementar con
     * los datos basicos del tag
     * @param datos
     */
    public Boolean guardarDatosDB(ContentValues datos){
        try {
            Viaje viaje = new Viaje(context);
            Boolean resultado = viaje.create(datos);
            idViaje = viaje.idViaje;
            return resultado;
        }catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
            return false;
        }
    }

    public static Boolean cambioEstatus(Context context, Integer idViaje){
        Viaje viaje = new Viaje(context);
        viaje = viaje.find(idViaje);
        if(viaje.Estatus == 3){
            return viaje.updateEstado(idViaje, 1);
        }
        if(viaje.Estatus == 4) {
            return viaje.updateEstado(idViaje, 2);
        }
        return false;
    }

    public Integer viajeIncompleto(Integer idcamion){
        String hora = Util.getHora();
        Integer idviaje = null;
        idviaje = Viaje.findViajeInconcluso(idcamion, Util.getFecha(), Util.getHoraInicial(hora), hora);
        return idviaje;
    }

    public void coordenadas(String IMEI, String code, Double latitud, Double longitud){
        ContentValues contentValues = new ContentValues();

        contentValues.put("IMEI", IMEI);
        contentValues.put("idevento", 2);
        contentValues.put("latitud", latitud);
        contentValues.put("longitud", longitud);
        contentValues.put("fecha_hora", Util.timeStamp());
        contentValues.put("code", code);

        Coordenada coordenada = new Coordenada(context);
        coordenada.create(contentValues, context);
    }
}
