package mx.grupohi.acarreos.Mina;

import android.content.ContentValues;
import android.content.Context;

import mx.grupohi.acarreos.InicioViaje;
import mx.grupohi.acarreos.R;
import mx.grupohi.acarreos.TagModel;
import mx.grupohi.acarreos.TiposTag.TagNFC;
import mx.grupohi.acarreos.Usuario;
import mx.grupohi.acarreos.InicioViaje;

/**
 * Created by DBENITEZ on 26/03/2018.
 */

public class SalidaMina {
    Context context;
    TagNFC tag_nfc;
    public Integer idInicio;
    public SalidaMina(Context context, TagNFC tag_nfc){
        this.context = context;
        this.tag_nfc = tag_nfc;
        //validarDatosTag();

    }

    public String validarDatosTag(){
        Usuario usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        if (!TagModel.findTAG (context, tag_nfc.getUID())) {
            return "El TAG que intentas configurar no está autorizado para éste proyecto.";
        }
        if(tag_nfc.getIdmaterial() != null && tag_nfc.getIdorigen() != null && tag_nfc.getFecha() != "" && tag_nfc.getUsuario() != null && tag_nfc.getVolumen() != null){
            if(usuario.tipo_permiso == 4){
                return "volumen_entrada";
            }else {
                return "El TAG cuenta con un viaje activo, Favor de pasar a un filtro de salida para finalizar el viaje.";
            }
        }
        TagModel datosTagCamion = new TagModel(context);
        datosTagCamion = datosTagCamion.find(tag_nfc.getUID(), tag_nfc.getIdcamion(), tag_nfc.getIdproyecto());
        if(datosTagCamion.estatus != 1){
            return "El camión " + datosTagCamion.economico + " se encuentra inactivo. Por favor contacta al encargado.";
        }
        if (tag_nfc.getIdproyecto() != usuario.getProyecto()) {
            return "El TAG no pertenece al proyecto del usuario.";
        }
        /*tag_nfc.setIdmaterial((Integer) datos.get("id_material"));
        tag_nfc.setIdorigen((Integer) datos.get("id_origen"));
        tag_nfc.setFecha((String) datos.get("fecha"));
        tag_nfc.setUsuario((String) datos.get("id_usuario"));
        tag_nfc.setTipo_viaje((String) datos.get("tipo_viaje"));*/

        return "continuar";
    }

    /**
     * Metodo para guardarlos datos en la base de datos,
     * el Content Values que va a llegar se debe complementar con
     * los datos basicos del tag
     * @param datos
     */
    public Boolean guardarDatosDB(ContentValues datos){
        /// datos.put(llave, tag_nfc.getIdorigen())   ---> este es un ejemplo
        //// despues de completar los datos, ya se hace la insercion en base de datos, para esto
        //// se debe ocupar el mismo metodo que se utiliza
        try {
            InicioViaje in = new InicioViaje(context);
            Boolean result = in.create(datos);
            idInicio = in.id;
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public  Boolean tipoviaje(){
        Usuario usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        if(usuario.tipo_permiso == 1) { // Perfil Checador de Origen
            return true;
        }
        if(usuario.tipo_permiso == 4){ // Perfil Checador de Entrada
            return false;
        }
        return false;
    }
}
