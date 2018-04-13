package mx.grupohi.acarreos.Destino;

import android.content.ContentValues;
import android.content.Context;

import mx.grupohi.acarreos.InicioViaje;
import mx.grupohi.acarreos.TagModel;
import mx.grupohi.acarreos.TiposTag.TagNFC;
import mx.grupohi.acarreos.Usuario;
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
        if (!TagModel.findTAG (context, tag_nfc.getUID())) {
            return "El TAG que intentas configurar no está autorizado para éste proyecto.";
        }
        TagModel datosTagCamion = new TagModel(context);
        datosTagCamion = datosTagCamion.find(tag_nfc.getUID(), tag_nfc.getIdcamion(), tag_nfc.getIdproyecto());
        if(datosTagCamion.estatus != 1){
            return "El camión " + datosTagCamion.economico + " se encuentra inactivo. Por favor contacta al encargado.";
        }
        if (tag_nfc.getIdproyecto() != usuario.getProyecto()) {
            return "El TAG no pertenece al proyecto del usuario.";
        }
        if (tipoPerfil()){ // Perfil Tiro o Salida
            if(tag_nfc.getIdmaterial() == null && tag_nfc.getIdorigen() == null && tag_nfc.getFecha() == "" && tag_nfc.getUsuario() == null && tag_nfc.getVolumen() == null){
                return "El TAG que intentas utilizar no cuenta con un origen definido.";
            }
            return "destino";
        }
        if(!tipoPerfil()){ // Perfil de Tiro Libre a Bordo
            if(tag_nfc.getIdmaterial() != null && tag_nfc.getIdorigen() != null && tag_nfc.getFecha() != "" && tag_nfc.getUsuario() != null && tag_nfc.getVolumen() != null){
                return "El TAG cuenta con un viaje activo, Favor de pasar a un filtro de salida para finalizar el viaje.";
            }
            return "libreAbordo";
        }
        return "Error al validar datos del TAG.";
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
            return false;
        }
    }

}
