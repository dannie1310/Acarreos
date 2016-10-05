package mx.grupohi.acarreos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity {
    
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView userText;
    private EditText passText;
    private TextInputLayout loginFormLayout;

    private ProgressDialog loginProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Set up the login form.
        userText = (AutoCompleteTextView) findViewById(R.id.userText);
        passText = (EditText) findViewById(R.id.passText);
        loginFormLayout = (TextInputLayout) findViewById(R.id.layout);


        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Resetear errores.
        userText.setError(null);
        passText.setError(null);

        // Store values at the time of the login attempt.
        String user = userText.getText().toString();
        String pass = passText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(user)) {
            userText.setError(getResources().getString(R.string.error_field_required));
            focusView = userText;
            cancel = true;
        } else if (TextUtils.isEmpty(pass)) {
            passText.setError(getResources().getString(R.string.error_field_required));
            focusView = passText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loginProgressDialog.show(LoginActivity.this, "Autenticando", "Por favor espere...", true);
            mAuthTask = new UserLoginTask(user, pass);
            mAuthTask.execute((Void) null);
        }
    }
    
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String user;
        private final String pass;

        UserLoginTask(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            ContentValues data = new ContentValues();
            data.put("usr", Usuario.user);
            data.put("pass", Usuario.pass);

            try {
                // Simulate network access.
                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                JSONObject JSON = HttpConnection.POST(url, data);
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                return false;
            }


            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            loginProgressDialog.dismiss();
            if (success) {
                finish();
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            loginProgressDialog.dismiss();
        }
    }
}

