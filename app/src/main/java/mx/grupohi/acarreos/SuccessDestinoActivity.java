package mx.grupohi.acarreos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SuccessDestinoActivity extends AppCompatActivity {

    private TextView textViewCamion,
            textViewCubicacion,
            textViewMaterial,
            textViewOrigen,
            textViewFechaHoraSalida,
            textViewDestino,
            textViewFechaHoraLlegada,
            textViewRuta,
            textViewObservaciones;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_destino);

        textViewCamion = (TextView) findViewById(R.id.textViewCamion);
        textViewCubicacion = (TextView) findViewById(R.id.textViewCubicacion);
        textViewMaterial = (TextView) findViewById(R.id.textViewMaterial);
        textViewOrigen = (TextView) findViewById(R.id.textViewOrigen);
        textViewFechaHoraSalida = (TextView) findViewById(R.id.textViewFechaHora);
        textViewDestino = (TextView) findViewById(R.id.textViewDestino);
        textViewFechaHoraLlegada = (TextView) findViewById(R.id.textViewFechaHoraLlegada);
        textViewRuta = (TextView) findViewById(R.id.textViewRuta);
        textViewObservaciones = (TextView) findViewById(R.id.textViewObservaciones);

        Button btnImprimir = (Button) findViewById(R.id.buttonImprimir);
        Button btnShowList = (Button) findViewById(R.id.buttonShowList);
        Button btnSalir = (Button) findViewById(R.id.buttonSalir);

        fillInfo();

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        /*btnShowList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListaViajesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });*/
    }

    public void fillInfo() {
        Integer idViaje = getIntent().getIntExtra("idViaje", 0);
        Viaje viaje = new Viaje(getApplicationContext());
        viaje = viaje.find(idViaje);

        textViewCamion.setText(viaje.camion.economico);
        textViewCubicacion.setText(viaje.camion.capacidad + " m3");
        textViewMaterial.setText(viaje.material.descripcion);
        textViewOrigen.setText(viaje.origen.descripcion);
        textViewFechaHoraSalida.setText(viaje.fechaSalida + " " + viaje.horaSalida);
        textViewDestino.setText(viaje.tiro.descripcion);
        textViewFechaHoraLlegada.setText(viaje.fechaLlegada + " " + viaje.horaLlegada);
        textViewRuta.setText(viaje.ruta.toString());
        textViewObservaciones.setText(viaje.observaciones);
        Log.i("ORIGEN", viaje.origen.descripcion);
        Log.i("ECONOMICO", viaje.camion.economico);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
