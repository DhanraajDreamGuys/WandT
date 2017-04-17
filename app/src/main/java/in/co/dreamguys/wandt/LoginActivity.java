package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import in.co.dreamguys.wandt.network.LoginHelper;
import in.co.dreamguys.wandt.util.ProgressDlg;

public class LoginActivity extends Activity implements View.OnClickListener {

    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    EditText userName, password;
    String uName,uPass, isLogin, app_url,url = "api/driver_login";
    Button loginBtn;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_DATA = "driver_id";
    private static final String TAG_STOCK = "stock_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isLogin = prefs.getString("isLogin","");
        if(isLogin.contentEquals("logged")){
            Intent log_stock = new Intent(LoginActivity.this,MainStockItem.class);
            startActivity(log_stock);
        }
        //driver name
        userName = (EditText) findViewById(R.id.userName);
        userName.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        //password
        password = (EditText) findViewById(R.id.password);
        password.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        //login btn
        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);
        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginBtn:
                uName  = userName.getText().toString();
                uPass  = password.getText().toString();
                if(uName == null || uName.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter username", Toast.LENGTH_SHORT).show();
                }else if(uPass == null || uPass.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_SHORT).show();
                }else{
                    new AsyncTaskRunner().execute(app_url,uName,uPass);
                }
                break;
        }
    }
    class AsyncTaskRunner extends AsyncTask<String, String, String>{
        JSONObject json = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(LoginActivity.this, "Connecting", "Please wait...");
        }

        @Override
        protected String doInBackground(String... login_url) {
            try {
                LoginHelper jHelper = new LoginHelper();
                json = jHelper.getJSONFromUrl(login_url[0], login_url[1], login_url[2]);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            // Things to be done while execution of long running operation is in
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            ProgressDlg.dismissProgressDialog();
            if(json!=null) {
                try {
                    if (json.getString(TAG_SUCCESS).contains("y")) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("driver_name", uName);
                        editor.putString("driver_id", json.getString(TAG_DATA));
                        editor.putString("stock_id", json.getString(TAG_STOCK));
                        editor.putString("isLogin", "logged");
                        editor.apply();
                        Intent stock_log = new Intent(LoginActivity.this, MainStockItem.class);
                        startActivity(stock_log);
                    } else {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Invalid username & password", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAffinity(this);
    }
}
