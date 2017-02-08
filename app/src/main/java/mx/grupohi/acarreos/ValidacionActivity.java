package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ValidacionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ProgressDialog progressDialogSync;
    Usuario usuario;
    Viaje viaje;
    Coordenada coordenada;
    String placas;
    String economico;
    String caja;
    EditText tex_economico;
    EditText tex_placas;
    TextView texto;
    TextView error;
    Button validar;
    Button  cancelar;
    Intent main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validacion);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        viaje = new Viaje(this);
        coordenada = new Coordenada(this);
        main = new Intent(getApplicationContext(), MainActivity.class);

        placas = getIntent().getStringExtra("placas");
        economico = getIntent().getStringExtra("economico");
        caja = getIntent().getStringExtra("caja");
        System.out.println("datos camion: "+placas + economico);

        tex_economico = (EditText) findViewById(R.id.textEscribirEconomico);
        tex_placas = (EditText) findViewById(R.id.textEscribirPlacas);
        texto = (TextView) findViewById(R.id.textoPlacas);
        error = (TextView) findViewById(R.id.textError);
        validar = (Button) findViewById(R.id.buttonRecibir);
        cancelar = (Button) findViewById(R.id.buttoncancelar);
        if (caja != null){
            texto.setText(texto.getText().toString()+" (Caja): ");
        }else{
            texto.setText(texto.getText().toString()+" (Camión): ");
        }


        validar.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {

                                           if(tex_economico.getText().toString().isEmpty()){
                                               error.setText("DEBE ESCRIBIR EL ECONOMICO DEL CAMIÓN.");
                                           }
                                           else if (tex_placas.getText().toString().isEmpty()){
                                               error.setText("DEBE ESCRIBIR LAS PLACAS DEL CAMIÓN.");
                                           }
                                           else{
                                               error.setText("");
                                               if(placas.toUpperCase().equals(tex_placas.getText().toString().toUpperCase()) && economico.toUpperCase().equals(tex_economico.getText().toString().toUpperCase())){
                                                  main.putExtra("validacion", "correcta");
                                                   finish();
                                               }else{
                                                   error.setText("DATOS INCORRECTOS, FAVOR DE VERIFICAR.");
                                               }
                                           }


                                       }
                                   });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(main);
            }
        });


                final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        if(drawer != null)
            drawer.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < drawer.getChildCount(); i++) {
                        View child = drawer.getChildAt(i);
                        TextView tvp = (TextView) child.findViewById(R.id.textViewProyecto);
                        TextView tvu = (TextView) child.findViewById(R.id.textViewUser);
                        TextView tvv = (TextView) child.findViewById(R.id.textViewVersion);

                        if (tvp != null) {
                            tvp.setText(usuario.descripcionBaseDatos);
                        }
                        if (tvu != null) {
                            tvu.setText(usuario.nombre);
                        }
                        if (tvv != null) {
                            tvv.setText("Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                        }
                    }
                }
            });
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       startActivity(main);
    }





    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
        } else if (id == R.id.nav_sync) {
            new AlertDialog.Builder(ValidacionActivity.this)
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(ValidacionActivity.this, "Sincronizando datos", "Por favor espere...", true);
                                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);
                                } else {
                                    Toast.makeText(getApplicationContext(), "No es necesaria la sincronización en este momento", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.error_internet, Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .create()
                    .show();

        } else if (id == R.id.nav_list) {
            Intent listaViajes = new Intent(this, ListaViajesActivity.class);
            startActivity(listaViajes);
        }else if (id == R.id.nav_desc) {
           Intent descarga = new Intent(this, DescargaActivity.class);
            startActivity(descarga);
        } else if (id == R.id.nav_logout) {
            if(!Viaje.isSync(getApplicationContext())){
                new AlertDialog.Builder(ValidacionActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(ValidacionActivity.this, "Sincronizando datos", "Por favor espere...", true);
                                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);

                                    Intent login_activity = new Intent(getApplicationContext(), LoginActivity.class);
                                    usuario.destroy();
                                    startActivity(login_activity);
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.error_internet, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .create()
                        .show();
            }
            else {
                Intent login_activity = new Intent(getApplicationContext(), LoginActivity.class);
                usuario.destroy();
                startActivity(login_activity);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
