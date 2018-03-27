package mx.grupohi.acarreos.Mina;

import android.content.ContentValues;
import android.content.Context;

import mx.grupohi.acarreos.R;
import mx.grupohi.acarreos.TagModel;
import mx.grupohi.acarreos.TiposTag.TagNFC;
import mx.grupohi.acarreos.Usuario;

/**
 * Created by DBENITEZ on 26/03/2018.
 */

public class SalidaMina {
    Context context;
    ContentValues datos;
    TagNFC tag_nfc;
    public SalidaMina(Context context, ContentValues datos){
        this.context = context;
        this.datos = datos;
        //validarDatosTag();

    }

    private String validarDatosTag(){
        if (!TagModel.findTAG (context, tag_nfc.getUID())) {
            return String.valueOf(R.string.error_tag_inexistente);
        }
        // idmaterial, idorigen, fecha, usuaurio existe,
        if(tag_nfc.getIdmaterial() == null || tag_nfc.getIdorigen() == null || tag_nfc.getFecha() == "" || tag_nfc.getUsuario() == null || tag_nfc.getVolumen() == null){
            return "El TAG cuenta con un viaje activo, Favor de pasar a un filtro de salida para finalizar el viaje.";
        }
        TagModel datosTagCamion = new TagModel(context);
        datosTagCamion = datosTagCamion.find(tag_nfc.getUID(), tag_nfc.getIdcamion(), tag_nfc.getIdproyecto());
        if(datosTagCamion.estatus != 1){
            return "El camión " + datosTagCamion.economico + " se encuentra inactivo. Por favor contacta al encargado.";
        }
        Usuario usuario = new Usuario(context);
        if (tag_nfc.getIdproyecto() == usuario.getProyecto()) {
            return String.valueOf(R.string.error_proyecto);
        }
        tag_nfc.setIdmaterial((Integer) datos.get("id_material"));
        tag_nfc.setIdorigen((Integer) datos.get("id_origen"));
        tag_nfc.setFecha((String) datos.get("fecha"));
        tag_nfc.setUsuario((String) datos.get("id_usuario"));
        tag_nfc.setTipo_viaje((String) datos.get("tipo_viaje"));

        return "continuar";
    }



}
