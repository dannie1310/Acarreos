package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class Util {

    static boolean isNetworkStatusAvialable (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if(netInfos != null)
                if(netInfos.isConnected())
                    return true;
        }
        return false;
    }

    static String getQuery(ContentValues values) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : values.valueSet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }
        return result.toString();
    }

    static String timeStamp() {
        return (String) android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date());
    }

    static Integer getIdCamion(String string) {

        try {
            Integer result = Integer.valueOf(string.substring(0,4));
            if (result != null) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    static Integer getIdProyecto(String string) {
        try {
            Integer result = Integer.valueOf(string.substring(4,8));
            if(result != null) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    static Integer getIdMaterial(String string) {
        try {
            Integer result = Integer.valueOf(string.substring(0,4));
            if(result != null) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e){
            return null;
        }
    }

   static Integer getIdOrigen(String string) {
       try {
           Integer result = Integer.valueOf(string.substring(4,8));
           if(result != null) {
               return result;
           } else {
               return null;
           }
       } catch (Exception e){
           return null;
       }
   }

    static String concatenar(String id, String id1){
        String aux = id;
        String aux1 = id1;
        for(int i = id.length(); i < 4; i++){
            aux = 0 + aux;
        }
        for(int i = id1.length(); i < 4; i++){
            aux1 = 0 + aux1;
        }
        return aux + aux1;
    }

    static String getFechaHora() {
        return (String) android.text.format.DateFormat.format("hhmmssyyyyMMdd", new java.util.Date());
    }
}
