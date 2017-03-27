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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
    String capacidad;
    EditText tex_economico;
    EditText tex_placas;
    EditText tex_placasCaja;
    EditText metros;
    TextView texto;
    TextView error;
    RadioButton gondola;
    RadioButton volteo;
    Button validar;
    Button  cancelar;
    LinearLayout checkbox;
    LinearLayout pGondola;

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
        caja = getIntent().getStringExtra("placasCaja");
        capacidad = getIntent().getStringExtra("capacidad");

        tex_economico = (EditText) findViewById(R.id.textEscribirEconomico);
        tex_placas = (EditText) findViewById(R.id.textEscribirPlacas);
        tex_placasCaja = (EditText) findViewById(R.id.textEscribirPlacasCaja);
        metros = (EditText) findViewById(R.id.textEscribirMetros);
        texto = (TextView) findViewById(R.id.textoPlacasCaja);
        error = (TextView) findViewById(R.id.textError);
        validar = (Button) findViewById(R.id.buttonRecibir);
        cancelar = (Button) findViewById(R.id.buttoncancelar);
        gondola = (RadioButton) findViewById(R.id.checkBoxG);
        volteo = (RadioButton) findViewById(R.id.checkBoxV);
        pGondola = (LinearLayout) findViewById(R.id.PeticionGondola);


        pGondola.setVisibility(View.GONE);
        texto.setVisibility(View.GONE);
        tex_placasCaja.setVisibility(View.GONE);

        gondola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(gondola.isChecked()){
                    pGondola.setVisibility(View.VISIBLE);
                    texto.setVisibility(View.VISIBLE);
                    tex_placasCaja.setVisibility(View.VISIBLE);
                    volteo.setChecked(false);
                    tex_placasCaja.setText("");
                    tex_placas.setText("");
                    metros.setText("");
                    tex_economico.setText("");
                    error.setText("");

                }
            }

        });
        volteo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(volteo.isChecked()){
                    pGondola.setVisibility(View.VISIBLE);
                    texto.setVisibility(View.GONE);
                    tex_placasCaja.setVisibility(View.GONE);
                    gondola.setChecked(false);
                    tex_placasCaja.setText("");
                    tex_placas.setText("");
                    metros.setText("");
                    tex_economico.setText("");
                    error.setText("");
                }
            }

        });

        validar.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {

                                           if(tex_economico.getText().toString().isEmpty()){
                                               error.setText("DEBE ESCRIBIR EL ECONOMICO DEL CAMIÓN.");
                                           }
                                           else if (tex_placas.getText().toString().isEmpty()){
                                               error.setText("DEBE ESCRIBIR LAS PLACAS DEL TRACTOR.");
                                           }
                                           else if (!caja.equals("null") && tex_placasCaja.getText().toString().isEmpty()){
                                               error.setText("DEBE ESCRIBIR LAS PLACAS DE LA CAJA.");
                                           }
                                           else if (metros.getText().toString().isEmpty()){
                                               error.setText("DEBE ESCRIBIR LA CAPACIDAD (METROS CUADRADOS).");
                                           }
                                           else{
                                               error.setText("");
                                               if(gondola.isChecked() && !caja.equals("null")){
                                                   if(caja.replace("-","").toUpperCase().equals(tex_placasCaja.getText().toString().replace("-","").toUpperCase()) && placas.replace("-","").toUpperCase().equals(tex_placas.getText().toString().replace("-","").toUpperCase()) && economico.replace("-","").toUpperCase().equals(tex_economico.getText().toString().replace("-","").toUpperCase()) && capacidad.equals(metros.getText().toString())){
                                                       main.putExtra("validacion", "correcta");
                                                       finish();
                                                   }else{
                                                       mensaje();
                                                   }
                                               }else if(volteo.isChecked() && caja.equals("null")){
                                                   if(placas.replace("-","").toUpperCase().equals(tex_placas.getText().toString().replace("-","").toUpperCase()) && economico.replace("-","").toUpperCase().equals(tex_economico.getText().toString().replace("-","").toUpperCase()) && capacidad.equals(metros.getText().toString())){
                                                       main.putExtra("validacion", "correcta");
                                                       finish();
                                                   }else{
                                                       mensaje();
                                                   }
                                               }else {
                                                mensaje();
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


    public  void mensaje(){
        error.setText("DATOS INCORRECTOS, FAVOR DE VERIFICAR.");
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
        }else if(id == R.id.nav_cambio){
            Intent cambio = new Intent(this, CambioClaveActivity.class);
            startActivity(cambio);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
