package in.co.dreamguys.wandt;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import in.co.dreamguys.wandt.util.DatabaseHelper;
import in.co.dreamguys.wandt.util.DrawableClickListener;
import in.co.dreamguys.wandt.network.PostHelper;
import in.co.dreamguys.wandt.util.ProgressDlg;

public class StockOutDelivery extends Activity implements View.OnClickListener {
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    private static final String TAG_SUCCESS = "success";

    String stock_id, totQty, totAmt,shopStr, driver_id, app_url,url = "api/update_product_delivery";
    TextView title, pl_stock_id,pl_stock_qty,pl_stock_amt, pl_sub, rate;
    EditText shipTo;
    Button placeOrderBtn, spSet, spCancel;
    JSONArray sJson = null;
    Spinner pl_discount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.stock_out_delivery);
        //retrieve stock id
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        stock_id = prefs.getString("stock_id", "").trim();
        driver_id = prefs.getString("driver_id", "").trim();
        totQty = prefs.getString("totQty", "");
        totAmt = prefs.getString("totAmt", "");
        initializeVars();
        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;
    }


    public void initializeVars(){

        pl_stock_id = (TextView) findViewById(R.id.pl_stock_id);
        pl_stock_id.setText(stock_id);
        pl_stock_qty = (TextView) findViewById(R.id.pl_stock_qty);
        pl_stock_qty.setText(totQty);
        pl_stock_amt = (TextView) findViewById(R.id.pl_stock_amt);
        pl_stock_amt.setText("\u00a3"+totAmt);
        rate = (TextView) findViewById(R.id.rate);

        pl_sub = (TextView) findViewById(R.id.pl_sub);
        pl_sub.setText("\u00a3"+totAmt);

        pl_discount = (Spinner) findViewById(R.id.pl_discount);
        ArrayList<String> dis_li = new ArrayList<String>();
        for (int i = 0; i <= 100; ++i) {
            dis_li.add(String.valueOf(i));
        }
        ArrayAdapter<String> dis_adapter = new ArrayAdapter<String>(StockOutDelivery.this, android.R.layout.simple_spinner_dropdown_item, dis_li);
        pl_discount.setAdapter(dis_adapter);

        pl_discount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String items = pl_discount.getSelectedItem().toString().trim();
                double f_amt = Double.parseDouble(totAmt);
                double discount_amt = (f_amt - (f_amt * ((double) Integer.valueOf(items) / (double) 100)));
                double detected_amt = f_amt - discount_amt;
                pl_sub.setText("\u00a3"+String.valueOf(discount_amt));
                if(items.contentEquals("0")){
                    rate.setText("");
                }else{
                    rate.setText("- \u00a3"+String.format("%.2f", detected_amt));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        //toolbar title setup
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("ORDER DELIVERY");
        //shipTo name
        shipTo = (EditText) findViewById(R.id.shipTo);
        shipTo.getBackground().setColorFilter(getResources().getColor(R.color.shipTo), PorterDuff.Mode.SRC_ATOP);
        //placeOrderBtn
        placeOrderBtn = (Button) findViewById(R.id.placeOrderBtn);
        if(totQty.equals("0")){
            shipTo.setVisibility(View.GONE);
            placeOrderBtn.setVisibility(View.GONE);
        }
        placeOrderBtn.setOnClickListener(this);
        shipTo.setOnTouchListener(new DrawableClickListener.RightDrawableClickListener(shipTo) {
            @Override
            public boolean onDrawableClick() {
                dlgShow();
                return true;
            }
        });

        //get shop details
        DatabaseHelper getShop = new DatabaseHelper(StockOutDelivery.this);
        getShop.open();
        try {
            sJson = getShop.getShopDetails();
            shopStr = sJson.toString();
            shopStr = shopStr.replaceAll("\\[", "");
            shopStr = shopStr.replaceAll("\\]", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getShop.close();
    }

    public void dlgShow()
    {
        if(sJson!=null && sJson.length()!=0) {
            final Dialog dlg = new Dialog(StockOutDelivery.this);
            dlg.setTitle("Shop Picker");
            dlg.setContentView(R.layout.shop_picker);
            spCancel = (Button) dlg.findViewById(R.id.spCancel);
            spSet = (Button) dlg.findViewById(R.id.spSet);
            final NumberPicker np = (NumberPicker) dlg.findViewById(R.id.shopPicker);
            int shCount = sJson.length()-1;
            np.setMaxValue(shCount);
            np.setMinValue(0);
            final String testArr[] = new String[sJson.length()];
            for (int i=0;i< sJson.length(); i++){
                try {
                    testArr[i] = sJson.getString(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            np.setDisplayedValues(testArr);
            np.setWrapSelectorWheel(false);
            spSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shipTo.setText(testArr[np.getValue()]);
                    dlg.dismiss();
                }
            });
            spCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                }
            });
            dlg.show();
        }
    }

    @Override
    public void onClick(View v) {
        String delivery_to = shipTo.getText().toString();
        String rate_dis = pl_discount.getSelectedItem().toString().trim();
        switch (v.getId()) {
            case R.id.placeOrderBtn:
                if(delivery_to.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter receipient name", Toast.LENGTH_LONG).show();
                }else{
                    DatabaseHelper getDelivery = new DatabaseHelper(StockOutDelivery.this);
                    getDelivery.open();
                    try {
                        JSONObject deliveryRes =  getDelivery.getDeliveryDetails(stock_id, delivery_to, rate_dis,driver_id);
                        new StockOutDeliveryTaskRunner().execute(app_url, deliveryRes.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    getDelivery.close();
                }
                break;
        }
    }

    class StockOutDeliveryTaskRunner extends AsyncTask<String, String, String>{
        JSONObject json = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(StockOutDelivery.this, "Loading", "Please wait...");
        }
        @Override
        protected String doInBackground(String... order_url) {
            try {
                PostHelper jHelper = new PostHelper();
                json = jHelper.getJSONFromUrl(order_url[0], order_url[1]);
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
                        String po_delivery = shipTo.getText().toString();
                        String po_dis_per = pl_discount.getSelectedItem().toString().trim();
                        String pt_dis_amt = rate.getText().toString();
                        String pt_total = pl_sub.getText().toString();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("po_delivery", po_delivery);
                        editor.putString("po_dis_per", po_dis_per);
                        editor.putString("pt_dis_amt", pt_dis_amt);
                        editor.putString("pt_total", pt_total);
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "Success. Order have been placed", Toast.LENGTH_LONG).show();
                        Intent stOut = new Intent(StockOutDelivery.this,OrderPrinter.class);
                        startActivity(stOut);
                    }else{
                        Toast.makeText(getApplicationContext(), "Error. Please try again", Toast.LENGTH_LONG).show();
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
        DatabaseHelper emptyDelivery = new DatabaseHelper(StockOutDelivery.this);
        emptyDelivery.open();
        emptyDelivery.deleteDeliveryDetails();
        emptyDelivery.close();
    }

}
