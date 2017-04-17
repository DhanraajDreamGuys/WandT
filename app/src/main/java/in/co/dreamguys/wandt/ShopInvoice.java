package in.co.dreamguys.wandt;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import in.co.dreamguys.wandt.adapter.InvoiceAdapter;
import in.co.dreamguys.wandt.network.JSONParser;
import in.co.dreamguys.wandt.util.ProgressDlg;

public class ShopInvoice extends Activity implements View.OnClickListener {
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PURCHASE = "purchased";
    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_ITEMS = "items";
    JSONArray items = null;
    TextView title, date_tv, status_tv, mode_tv, billing_tv, sub_tv, ship_tv, discount_tv, total_tv;
    String driver_id, store_tag, app_url, app_url2, url = "api/store_order_details", url2="api/store_order_update";
    ListView purchasedView;
    ImageView toolIcon;
    Button spCancel, spSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.shop_invoice);
    }

    @Override
    protected void onStart() {
        super.onStart();
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        driver_id = prefs.getString("driver_id", "");
        store_tag = prefs.getString("store_tag", "");
        app_url = app_url2 = prefs.getString("api_url", "");
        app_url = app_url + url;
        app_url2 = app_url2 + url2;
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("Purchased Details");
        toolIcon = (ImageView) findViewById(R.id.toolIcon);
        toolIcon.setImageResource(R.drawable.done);
        toolIcon.setOnClickListener(this);
        purchasedView = (ListView)findViewById(R.id.purchasedView);
        //Purchased Details
        date_tv = (TextView) findViewById(R.id.date_tv);
        status_tv = (TextView) findViewById(R.id.status_tv);
        mode_tv = (TextView) findViewById(R.id.mode_tv);
        billing_tv = (TextView) findViewById(R.id.billing_tv);
        //Amount Details
        sub_tv = (TextView) findViewById(R.id.sub_tv);
        ship_tv = (TextView) findViewById(R.id.ship_tv);
        discount_tv = (TextView) findViewById(R.id.discount_tv);
        total_tv = (TextView) findViewById(R.id.total_tv);
        new OrderSync().execute(app_url+"?store_id="+store_tag.trim());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toolIcon:
                InvioceDlg();
                break;
        }
    }

    public void InvioceDlg(){
        final Dialog dlg = new Dialog(ShopInvoice.this);
        dlg.setTitle("Order Action");
        dlg.setContentView(R.layout.shop_picker);
        spCancel = (Button) dlg.findViewById(R.id.spCancel);
        spSet = (Button) dlg.findViewById(R.id.spSet);
        spSet.setText("UPDATE");
        final NumberPicker np = (NumberPicker) dlg.findViewById(R.id.shopPicker);
        np.setMaxValue(4);
        np.setMinValue(0);
        np.setDisplayedValues(new String[]{"New", "Cancelled", "Processing", "Completed", "Refunded"});
        np.setWrapSelectorWheel(false);
        spSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                new invoiceSync().execute(app_url2+"?store_id="+store_tag.trim()+"&sho_status="+np.getValue());
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

    class OrderSync extends AsyncTask<String, String , String> {
        JSONObject json = null, purchase = null, ad = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(ShopInvoice.this, "Loading", "Please wait...");
        }
        @Override
        protected String doInBackground(String... store_url) {
            try {
                JSONParser jHelper = new JSONParser();
                json = jHelper.getJSONFromUrl(store_url[0]);
                if (json != null) {
                    try {
                        List<String> prod_name = new ArrayList<String>();
                        List<String> prod_qty = new ArrayList<String>();
                        List<String> prod_price = new ArrayList<String>();

                        if (json.getString(TAG_SUCCESS).contains("y")) {
                            purchase = json.getJSONObject(TAG_PURCHASE);
                            ad = json.getJSONObject(TAG_AMOUNT);
                            items = json.getJSONArray(TAG_ITEMS);
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject itemNode = null;
                                try {
                                    itemNode = items.getJSONObject(i);
                                    prod_name.add(itemNode.getString("name"));
                                    prod_qty.add(itemNode.getString("qty"));
                                    prod_price.add(itemNode.getString("price"));
                                }catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            String[] nameArr = new String[prod_name.size()];
                            nameArr = prod_name.toArray(nameArr);
                            String[] qtyArr = new String[prod_qty.size()];
                            qtyArr = prod_qty.toArray(qtyArr);
                            String[] amtArr = new String[prod_price.size()];
                            amtArr = prod_price.toArray(amtArr);

                            final InvoiceAdapter OnlineOrders = new InvoiceAdapter(ShopInvoice.this,nameArr,qtyArr,amtArr);
                            ShopInvoice.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    purchasedView.setAdapter(OnlineOrders);
                                }
                            });
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
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
            if(purchase!=null) {
                try {
                    //Purchased Details
                    date_tv.setText(purchase.getString("pc_date"));
                    status_tv.setText(purchase.getString("pc_sts"));
                    mode_tv.setText(purchase.getString("pc_mode"));
                    billing_tv.setText(purchase.getString("pc_bill"));
                    //Amount Details
                    sub_tv.setText(ad.getString("ad_sub"));
                    ship_tv.setText(ad.getString("ad_ship"));
                    discount_tv.setText(ad.getString("ad_discount"));
                    total_tv.setText(ad.getString("ad_total"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ProgressDlg.dismissProgressDialog();
        }
    }

    class invoiceSync extends AsyncTask<String, String , String> {
        JSONObject json = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(ShopInvoice.this, "Loading", "Please wait...");
        }
        @Override
        protected String doInBackground(String... store_up) {
            try {
                JSONParser jHelper = new JSONParser();
                json = jHelper.getJSONFromUrl(store_up[0]);
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
            if (json != null) {
                try {
                    if (json.getString(TAG_SUCCESS).contains("y")) {
                        Toast.makeText(getApplicationContext(), "Success. Order have been updated", Toast.LENGTH_LONG).show();
                        Intent so = new Intent(ShopInvoice.this,ShopOrder.class);
                        startActivity(so);
                        finish();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Error. Please try again later", Toast.LENGTH_LONG).show();
            }
            ProgressDlg.dismissProgressDialog();
        }
    }

}
