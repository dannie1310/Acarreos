package mx.grupohi.acarreos.Mina;

import android.content.ContentValues;
import android.content.Context;
import com.crashlytics.android.Crashlytics;
import mx.grupohi.acarreos.Camion;
import mx.grupohi.acarreos.Coordenada;
import mx.grupohi.acarreos.InicioViaje;
import mx.grupohi.acarreos.TagModel;
import mx.grupohi.acarreos.TiposTag.TagNFC;
import mx.grupohi.acarreos.Usuario;
import mx.grupohi.acarreos.InicioViaje;
import mx.grupohi.acarreos.Util;

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
    }

    public String validarDatosTag(Integer volumen){
        Usuario usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        Camion camion = new Camion(context);
        camion = camion.find(tag_nfc.getIdcamion());
        if (!TagModel.findTAG (context, tag_nfc.getUID())) {
            return "El TAG que intentas configurar no está autorizado para éste proyecto.";
        }

        if(!tag_nfc.getIdmaterial().equals("") && !tag_nfc.getIdorigen().equals("") && tag_nfc.getFecha() != "" && !tag_nfc.getUsuario().equals("") && !tag_nfc.getVolumen().equals("") && !tag_nfc.getTipo_perfil().equals("")){
            if(usuario.tipo_permiso == 4 && tag_nfc.getTipo_viaje().equals("1")){ // Perfil de Checador Entrada y Viaje de Origen.
                return "volumen_entrada";
            }else {
                return "El TAG cuenta con un viaje activo, Favor de pasar a un filtro de salida para finalizar el viaje.";
            }
        }
        TagModel datosTagCamion = new TagModel(context);
        datosTagCamion = datosTagCamion.find(tag_nfc.getUID(), tag_nfc.getIdcamion(), tag_nfc.getIdproyecto());
        if(datosTagCamion.estatus != 1) {
            return "El camión " + datosTagCamion.economico + " se encuentra inactivo. Por favor contacta al encargado.";
        }
        if (tag_nfc.getIdproyecto() != usuario.getProyecto()) {
            return "El TAG no pertenece al proyecto del usuario.";
        }
        if(camion.capacidad != 0 && camion.capacidad!= null && volumen > camion.capacidad) {
            return "El volumen es mayor a la capacidad del camión.";
        }
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
            Crashlytics.logException(e);
            return false;
        }
    }

    /**
     * Metodo para identificar el tipo de perfil
     */
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

    public  Boolean rollbackDB(Integer inicio){
        try {
            InicioViaje in = new InicioViaje(context);
            return in.borrar(inicio);
        }catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
            return false;
        }
    }

}
