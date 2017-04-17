package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import in.co.dreamguys.wandt.util.DatabaseHelper;
import in.co.dreamguys.wandt.network.PostHelper;
import in.co.dreamguys.wandt.util.ProgressDlg;

public class StockOutReturn extends Activity implements View.OnClickListener {
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    private static final String TAG_SUCCESS = "success";
    String stock_id, totQty, totAmt, driver_id, app_url,url ="api/update_product_return";
    TextView title, pl_stock_id,pl_stock_qty,pl_stock_amt, pl_sub;
    Button returnOrderBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.stock_out_return);
        //retrieve stock id
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        stock_id = prefs.getString("stock_id", "").trim();
        driver_id = prefs.getString("driver_id", "").trim();
        totQty = prefs.getString("totQty", "");
        totAmt = prefs.getString("totAmt", "");
        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;
        initializeVars();
    }

    public void initializeVars(){
        pl_stock_id = (TextView) findViewById(R.id.pl_stock_id);
        pl_stock_id.setText(stock_id);
        pl_stock_qty = (TextView) findViewById(R.id.pl_stock_qty);
        pl_stock_qty.setText(totQty);
        pl_stock_amt = (TextView) findViewById(R.id.pl_stock_amt);
        pl_stock_amt.setText("\u00a3"+totAmt);
        pl_sub = (TextView) findViewById(R.id.pl_sub);
        pl_sub.setText("\u00a3"+totAmt);
        //toolbar title setup
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("STOCK RETURN");
        //hide discount
        //returnOrderBtn
        returnOrderBtn = (Button) findViewById(R.id.returnOrderBtn);
        if(totQty.equals("0")){
            returnOrderBtn.setVisibility(View.GONE);
        }
        returnOrderBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.returnOrderBtn:

                DatabaseHelper getReturn = new DatabaseHelper(StockOutReturn.this);
                getReturn.open();
                try {
                    JSONObject returnRes =  getReturn.getReturnDetails(stock_id, driver_id);
                    //System.out.println("json: "+returnRes+"\n");
                    new StockOutReturnTaskRunner().execute(app_url,returnRes.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getReturn.close();

                break;
        }
    }

    class StockOutReturnTaskRunner extends AsyncTask<String, String, String> {
        JSONObject json = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(StockOutReturn.this, "Loading", "Please wait...");
        }
        @Override
        protected String doInBackground(String... return_url) {
            try {
                PostHelper jHelper = new PostHelper();
                json = jHelper.getJSONFromUrl(return_url[0], return_url[1]);
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
            if(json!=null){
                try {
                    if (json.getString(TAG_SUCCESS).contains("y")) {
                        Toast.makeText(getApplicationContext(), "Success. Stock have been returned", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Error. Please try again", Toast.LENGTH_LONG).show();
                    }
                    Intent stOut = new Intent(StockOutReturn.this,MainStockItem.class);
                    startActivity(stOut);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //delete return stock details
        DatabaseHelper emptyReturn = new DatabaseHelper(StockOutReturn.this);
        emptyReturn.open();
        emptyReturn.deleteReturnDetails();
        emptyReturn.close();
    }

}