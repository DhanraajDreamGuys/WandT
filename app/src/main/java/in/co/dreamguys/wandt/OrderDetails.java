package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import in.co.dreamguys.wandt.network.JSONParser;
import in.co.dreamguys.wandt.util.ProgressDlg;
import in.co.dreamguys.wandt.adapter.StockSales;

public class OrderDetails extends Activity {
    TextView title;
    ListView salesList;
    String shop_id, driver_id, url = "api/get_shop_orders", app_url;

    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    private static final String TAG_SUCCESS = "success";

    private static final String TAG_SALES = "sales";
    private static final String HY_ID = "order_id";
    private static final String HY_NAME = "shop_name";
    private static final String HY_QTY = "so_qty";
    private static final String HY_AMT = "so_amt";
    private static final String HY_DATE = "so_date";

    JSONArray sales = null;
    String[] separated;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sales_details);

        //toolbar title setup
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("ORDER DETAILS");
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        shop_id = prefs.getString("shop_id_sales", "").trim();
        driver_id = prefs.getString("driver_id", "").trim();
        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;
        new OrderTaskRunner().execute(app_url + "?shop=" + shop_id + "&driver=" + driver_id);
    }


    class OrderTaskRunner extends AsyncTask<String, String , String> {
        JSONObject json = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(OrderDetails.this, "Loading", "Please wait...");
        }

        @Override
        protected String doInBackground(String... stock_url) {
            try {
                JSONParser jHelper = new JSONParser();
                json = jHelper.getJSONFromUrl(stock_url[0]);
                System.out.println(json);
                if(json!=null){
                    // Getting JSON Array
                    try {
                        sales = json.getJSONArray(TAG_SALES);
                        //order details
                        List<String> hy_id = new ArrayList<String>();
                        List<String> hy_name = new ArrayList<String>();
                        List<String> hy_qty = new ArrayList<String>();
                        List<String> hy_amt = new ArrayList<String>();
                        List<String> hy_date = new ArrayList<String>();

                        if(sales!=null) {
                            for (int i = 0; i < sales.length(); i++) {
                                JSONObject hsyChildNode = null;
                                try {
                                    hsyChildNode = sales.getJSONObject(i);
                                    hy_id.add(hsyChildNode.getString(HY_ID));
                                    hy_name.add(hsyChildNode.getString(HY_NAME));
                                    hy_qty.add(hsyChildNode.getString(HY_QTY));
                                    hy_amt.add(hsyChildNode.getString(HY_AMT));
                                    hy_date.add(hsyChildNode.getString(HY_DATE));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                            //list to array conversion

                            String[] hy_nameArr = new String[hy_name.size()];
                            hy_nameArr = hy_name.toArray(hy_nameArr);
                            String[] hy_idArr = new String[hy_id.size()];
                            hy_idArr = hy_id.toArray(hy_idArr);
                            String[] hy_qtyArr = new String[hy_qty.size()];
                            hy_qtyArr = hy_qty.toArray(hy_qtyArr);
                            String[] hy_amtArr = new String[hy_amt.size()];
                            hy_amtArr = hy_amt.toArray(hy_amtArr);
                            String[] hy_dateArr = new String[hy_date.size()];
                            hy_dateArr = hy_date.toArray(hy_dateArr);

                           final StockSales salesAdapter = new StockSales(OrderDetails.this, hy_idArr, hy_nameArr, hy_qtyArr, hy_amtArr, hy_dateArr);
                            OrderDetails.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    salesList = (ListView) findViewById(R.id.salesList);
                                    salesList.setAdapter(salesAdapter);
                                    salesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            TextView tv = (TextView) view.findViewById(R.id.product_name);
                                            String soID = tv.getText().toString();
                                            separated = soID.split(":");
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("sales_order_id", separated[1]);
                                            editor.apply();
                                            Intent DetailsSale = new Intent(OrderDetails.this, SalesDetails.class);
                                            startActivity(DetailsSale);
                                        }
                                    });
                                }
                            });
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
            // execution of result of Long time consuming operation
            ProgressDlg.dismissProgressDialog();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Intent so = new Intent(OrderDetails.this,MainStockItem.class);
        //startActivity(so);
        finish();
    }
}