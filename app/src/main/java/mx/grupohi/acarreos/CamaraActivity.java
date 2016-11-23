package mx.grupohi.acarreos;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class CamaraActivity extends AppCompatActivity {

    private static String APP_DIRECTORY = "Picture/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;

    private ImageView mSetImage;
    private Button button;
    private RelativeLayout mRlView;

    Bitmap bitmap;

    private String mPath;
    private  String base64="";

    private HashMap<String, String> spinnerTiposMap;
    private Spinner tiposSpinner;
    TipoImagenes tipo;
    Integer idTipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        mSetImage = (ImageView) findViewById(R.id.set_picture);
        mRlView = (RelativeLayout) findViewById(R.id.activity_camara);
        button = (Button) findViewById(R.id.buttonGuardar);
        try {
            openCamera();
        }catch (Exception e){
            e.printStackTrace();
        }


        tipo = new TipoImagenes(getApplicationContext());
        tiposSpinner = (Spinner) findViewById(R.id.spinnerTipos);

        final ArrayList<String> tiposImagenes= tipo.getArrayListTipos();
        final ArrayList <String> ids = tipo.getArrayListId();
        final String[] spinnerTiposArray = new String[ids.size()];
        spinnerTiposMap = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            spinnerTiposMap.put(tiposImagenes.get(i), ids.get(i));
            spinnerTiposArray[i] = tiposImagenes.get(i);
        }

        final ArrayAdapter<String> arrayAdapterTiros = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item, spinnerTiposArray);
        arrayAdapterTiros.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        tiposSpinner.setAdapter(arrayAdapterTiros);

        tiposSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String descripcion = String.valueOf(parent.getItemAtPosition(position));
                idTipo = Integer.valueOf(spinnerTiposMap.get(descripcion));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //System.out.println("imagen: "+base64);
                ContentValues cv = new ContentValues();
                cv.put("idviaje_neto", getIntent().getStringExtra("idviaje_neto"));
                cv.put("idtipo_imagen", idTipo);
                cv.put("imagen", base64);
                ImagenesViaje imagenesViaje = new ImagenesViaje(CamaraActivity.this);
                imagenesViaje.create(cv);
                System.out.println("put: " + cv);
                System.out.println("TIPO: " + idTipo);
                Intent imagen= new Intent(CamaraActivity.this, ImagenesActivity.class);
                startActivity(imagen);

            }
        });




    }


    private void openCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if(!isDirectoryCreated)
            isDirectoryCreated = file.mkdirs();

        if(isDirectoryCreated){
            Long timestamp = System.currentTimeMillis() / 1000;
            String imageName = timestamp.toString() + ".jpg";

            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + imageName;

            File newFile = new File(mPath);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPath = savedInstanceState.getString("file_path");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> Uri = " + uri);
                                }
                            });


                    bitmap = BitmapFactory.decodeFile(mPath);
                    mSetImage.setImageBitmap(bitmap);
                    base64 = Usuario.encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 100);
                    break;

            }
        }
    }


}
