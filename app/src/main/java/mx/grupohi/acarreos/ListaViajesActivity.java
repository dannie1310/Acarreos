package mx.grupohi.acarreos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class ListaViajesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViajeFragment.OnFragmentInteractionListener  {

    private ProgressDialog progressDialogSync;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_viajes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usuario = new Usuario(this);
        usuario = usuario.getUsuario();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ViajeFragment viajeFragment = (ViajeFragment)
                getSupportFragmentManager().findFragmentById(R.id.content_lista_viajes);

        if (viajeFragment == null) {
            viajeFragment = ViajeFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_lista_viajes, viajeFragment)
                    .commit();
        }

        if(drawer != null)
            drawer.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < drawer.getChildCount(); i++) {
                        View child = drawer.getChildAt(i);
                        TextView tvp = (TextView) child.findViewById(R.id.textViewProyecto);
                        TextView tvu = (TextView) child.findViewById(R.id.textViewUser);

                        if (tvp != null) {
                            tvp.setText(usuario.descripcionBaseDatos);
                        }
                        if (tvu != null) {
                            tvu.setText(usuario.nombre);
                        }
                    }
                }
            });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            if (Util.isNetworkStatusAvialable(this)) {
                if(!Viaje.isSync(getApplicationContext())) {
                    progressDialogSync = ProgressDialog.show(this, "Sincronizando datos", "Por favor espere...", true);
                    new Sync(getApplicationContext(), progressDialogSync).execute((Void) null);
                } else {
                    Toast.makeText(getApplicationContext(), "No es necesaria la sincronización en este momento", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.error_internet, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_pair_on) {

        } else if (id == R.id.nav_pair_off) {

        } else if (id == R.id.nav_logout) {
            Intent login_activity = new Intent(this, LoginActivity.class);
            usuario.destroy();
            startActivity(login_activity);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
