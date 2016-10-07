package mx.grupohi.acarreos;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Creado por JFEsquivel on 07/10/2016.
 */

class HttpConnection {

    static JSONObject POST(URL url, ContentValues values) throws JSONException {

        String response = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(Util.getQuery(values));
            bw.flush();

            int statusCode = conn.getResponseCode();
            Log.i("Status Code",String.valueOf(statusCode));

            InputStream is = conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line, toAppend;

            toAppend = br.readLine() + "\n";
            sb.append(toAppend);
            while ((line = br.readLine()) != null) {
                toAppend = line + "\n";
                sb.append(toAppend);
            }
            is.close();
            response = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  new JSONObject(response);
    }
}
