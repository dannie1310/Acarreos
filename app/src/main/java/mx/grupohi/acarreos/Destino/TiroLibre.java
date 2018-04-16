package mx.grupohi.acarreos.Destino;

import android.content.ContentValues;
import android.content.Context;

import mx.grupohi.acarreos.Coordenada;
import mx.grupohi.acarreos.TagModel;
import mx.grupohi.acarreos.TiposTag.TagNFC;
import mx.grupohi.acarreos.Usuario;
import mx.grupohi.acarreos.Util;
import mx.grupohi.acarreos.Viaje;

public class TiroLibre {
    Context context;
    TagNFC tag_nfc;
    public Integer idViaje;

    public TiroLibre(Context context, TagNFC tag_nfc) {
        this.context = context;
        this.tag_nfc = tag_nfc;
    }

    public String validarDatosTag() {
        Usuario usuario = new Usuario(context);
        usuario = usuario.getUsuario();
        if (!TagModel.findTAG (context, tag_nfc.getUID())) {
            return "El TAG que intentas configurar no está autorizado para éste proyecto.";
        }
        if (tag_nfc.getIdproyecto() != usuario.getProyecto()) {
            return "El TAG no pertenece al proyecto del usuario.";
        }
        TagModel datosTagCamion = new TagModel(context);
        datosTagCamion = datosTagCamion.find(tag_nfc.getUID(), tag_nfc.getIdcamion(), tag_nfc.getIdproyecto());
        if(datosTagCamion.estatus != 1){
            return "El camión " + datosTagCamion.economico + " se encuentra inactivo. Por favor contacta al encargado.";
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
