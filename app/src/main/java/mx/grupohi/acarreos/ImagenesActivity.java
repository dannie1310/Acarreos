package mx.grupohi.acarreos;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toolbar;

import java.util.List;


public class ImagenesActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {
    private String x;
    private GridView gridView;
    private AdaptadorImagenes adaptador;
    List<ImagenesViaje> lista;
    ImageButton camara;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagenes);
        camara = (ImageButton) findViewById(R.id.imageButton);

        x= getIntent().getStringExtra("idviaje_neto");
        System.out.println("imagenActivity "+x);
        ImagenesViaje m = new ImagenesViaje(getApplicationContext());
       // lista = ImagenesViaje.getImagen(getApplicationContext());
         m.getImagen(Integer.parseInt(x));
        int numImagenes = m.getCount(Integer.parseInt(x));

       if(numImagenes != 4){
           camara.setEnabled(true);
           camara.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(getApplicationContext(), CamaraActivity.class);
                   intent.putExtra("idviaje_neto", x.toString());
                   startActivity(intent);
               }
           });
        }
       else{
           camara.setVisibility(View.GONE);
       }
        gridView = (GridView) findViewById(R.id.grid);
        adaptador = new AdaptadorImagenes(this);
        gridView.setAdapter(adaptador);
        gridView.setOnItemClickListener(this);

    }


    @Override
    public void onBackPressed() {
        Integer list = getIntent().getIntExtra("list", 0);
        if(list == 1) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent(getApplicationContext(), ListaViajesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImagenesViaje item = (ImagenesViaje) parent.getItemAtPosition(position);
        Intent intent = new Intent(this, ImagenDetalle.class);
        intent.putExtra(ImagenDetalle.EXTRA_PARAM_ID, item.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            ActivityOptionsCompat activityOptions =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            new Pair<View, String>(view.findViewById(R.id.imagen),
                                    ImagenDetalle.VIEW_NAME_HEADER_IMAGE)
                    );

            ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
        } else
            startActivity(intent);
    }
}
