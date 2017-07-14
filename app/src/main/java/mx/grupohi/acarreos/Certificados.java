package mx.grupohi.acarreos;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by LERDES2 on 14/07/2017.
 */
public class Certificados {
    Context context;
    public Certificados(Context context){
        this.context = context;
        saveFiles();
    }

    private void saveFiles() {
        File folder = context.getExternalFilesDir("certificados");


        if (folder.exists())
        {
            AssetManager assetManager = context.getAssets();
            String[] files = null;
            try {
                files = assetManager.list("");
            } catch (IOException e) {
                Log.e("tag", "Failed to get asset file list.", e);
            }
            if (files != null) for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(filename);
                    File outFile = new File(folder, filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                } catch(IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
