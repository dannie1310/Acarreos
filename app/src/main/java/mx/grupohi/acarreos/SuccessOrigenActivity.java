package mx.grupohi.acarreos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SuccessOrigenActivity extends AppCompatActivity {

    Usuario usuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_origen);
        usuario = new Usuario(getApplicationContext());
        Button btnSalir = (Button) findViewById(R.id.buttonSalir);
        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer tipo = usuario.getTipo_permiso();
                Intent mainActivity;
                if(tipo == 0){
                    mainActivity = new Intent(getApplicationContext(), SetOrigenActivity.class);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainActivity);
                }else if(tipo == 1){
                    mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainActivity);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent mainActivity;
        Integer tipo = usuario.getTipo_permiso();
        if(tipo == 0){
            mainActivity = new Intent(getApplicationContext(), SetOrigenActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainActivity);
        }else if(tipo == 1){
            mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainActivity);
        }
    }
}
