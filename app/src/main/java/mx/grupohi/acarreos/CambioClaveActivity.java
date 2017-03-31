package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
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

public class CambioClaveActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ProgressDialog progressDialogSync;
    private ProgressDialog progressDialogCambio;
    private Usuario usuario;
    private EditText uss;
    private EditText actual;
    private EditText pass;
    private EditText passConfirmacion;
    private Button cambio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_clave);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        usuario = new Usuario(this);
        usuario = usuario.getUsuario();
        uss = (EditText) findViewById(R.id.user);
        pass = (EditText) findViewById(R.id.pass);
        passConfirmacion = (EditText)findViewById(R.id.passCambio);
        actual = (EditText) findViewById(R.id.passAnterior);
        cambio = (Button) findViewById(R.id.CambioButton);


        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        cambio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(!checar()){
                   if(uss.getText().toString().toUpperCase().equals(usuario.usr.toUpperCase()) && actual.getText().toString().equals(usuario.pass)) {

                       if (pass.getText().toString().length()>= 8 && passConfirmacion.getText().toString().length()>=8){

                           if(pass.getText().toString().equals(passConfirmacion.getText().toString())){
                               //OK
                               new AlertDialog.Builder(CambioClaveActivity.this)
                                       .setTitle("¡ADVERTENCIA!")
                                       .setMessage("Se actualizara la contraseña de la Intranet. \n ¿Deséas continuar con el cambio de clave de acceso?")
                                       .setNegativeButton("NO", null)
                                       .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                           @Override public void onClick(DialogInterface dialog, int which) {
                                               progressDialogCambio = ProgressDialog.show(CambioClaveActivity.this, "Cambiando Contraseña", "Por favor espere...", true);
                                               new SincronizarCambioClave(getApplicationContext(), progressDialogCambio,pass.getText().toString()).execute((Void) null);
                                               uss.setText("");
                                               pass.setText("");
                                               actual.setText("");
                                               passConfirmacion.setText("");
                                           }
                                       })
                                       .create()
                                       .show();

                           }else{
                               Toast.makeText(getApplicationContext(), R.string.error_pass, Toast.LENGTH_SHORT).show();
                           }
                       } else {
                           Toast.makeText(getApplicationContext(), R.string.error_field_requiredpass, Toast.LENGTH_SHORT).show();
                       }
                   }else{
                       if(!uss.getText().toString().equals(usuario.usr)) {
                           Toast.makeText(getApplicationContext(), R.string.error_uss, Toast.LENGTH_SHORT).show();
                       }
                       else if(!actual.getText().toString().equals(usuario.pass)) {
                           Toast.makeText(getApplicationContext(), R.string.error_anter, Toast.LENGTH_SHORT).show();
                       }
                   }
               }
            }
        });

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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean checar() {

        //Reset Errors

        //sindicato.setError(null);
        uss.setError(null);
        pass.setError(null);
        passConfirmacion.setError(null);
        actual.setError(null);

        final String usuarios = uss.getText().toString();
        final String passw = pass.getText().toString();
        final String passCambio = passConfirmacion.getText().toString();
        final String vieja = actual.getText().toString();


        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(usuarios)) {
            uss.setError(getString(R.string.error_field_required));
            focusView = uss;
            cancel = true;
        }
        if(TextUtils.isEmpty(vieja)) {
            actual.setError(getString(R.string.error_field_required));
            focusView = actual;
            cancel = true;
        }
        if(TextUtils.isEmpty(passw)) {
            pass.setError(getString(R.string.error_field_required));
            focusView = pass;
            cancel = true;
        }
        if(TextUtils.isEmpty(passCambio)) {
            passConfirmacion.setError(getString(R.string.error_field_required));
            focusView = passConfirmacion;
            cancel = true;
        }

        return cancel;

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
        } else if (id == R.id.nav_sync) {
            new AlertDialog.Builder(CambioClaveActivity.this)
                    .setTitle("¡ADVERTENCIA!")
                    .setMessage("Se borrarán los registros de viajes almacenados en este dispositivo. \n ¿Deséas continuar con la sincronización?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                if(!Viaje.isSync(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(CambioClaveActivity.this, "Sincronizando datos", "Por favor espere...", true);
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
            Intent navList = new Intent(this, ListaViajesActivity.class);
            startActivity(navList);
        }else if (id == R.id.nav_desc) {
            Intent descarga = new Intent(this, DescargaActivity.class);
            startActivity(descarga);
        } else if (id == R.id.nav_logout) {
            if(!Viaje.isSync(getApplicationContext())){
                new AlertDialog.Builder(CambioClaveActivity.this)
                        .setTitle("¡ADVERTENCIA!")
                        .setMessage("Hay viajes aún sin sincronizar, se borrarán los registros de viajes almacenados en este dispositivo,  \n ¿Deséas sincronizar?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                                    progressDialogSync = ProgressDialog.show(CambioClaveActivity.this, "Sincronizando datos", "Por favor espere...", true);
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
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
