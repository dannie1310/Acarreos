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
import android.widget.GridView;
import android.widget.Toolbar;


public class ImagenesActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private GridView gridView;
    private AdaptadorImagenes adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagenes);


        gridView = (GridView) findViewById(R.id.grid);
        adaptador = new AdaptadorImagenes(this);
        gridView.setAdapter(adaptador);
        gridView.setOnItemClickListener(this);

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
