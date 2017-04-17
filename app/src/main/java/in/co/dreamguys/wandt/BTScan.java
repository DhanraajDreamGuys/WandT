package in.co.dreamguys.wandt;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import in.co.dreamguys.wandt.adapter.BTStock;
import in.co.dreamguys.wandt.util.DatabaseHelper;

public class BTScan extends Activity implements View.OnClickListener{
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    TextView title;
    Button btProceed, spCancel, spSet;
    String out_page;
    EditText btVal, mBarcode;
    ListView btProducts;
    ImageView toolIcon;
    ArrayList<String> BTlist = new ArrayList<String>();
    private Handler handler = new Handler();
    ArrayAdapter<String> BTAdapter;
    BTStock listProducts;
    ArrayList<String> item_id = new ArrayList<String>();
    ArrayList<String> item_name = new ArrayList<String>();
    ArrayList<String> item_img = new ArrayList<String>();
    ArrayList<String> item_price = new ArrayList<String>();
    ArrayList<String> item_qty = new ArrayList<String>();
    ArrayList<String> item_barcode = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.bt_scan);
        //toolbar title setup
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("BT SCANNER");
        toolIcon = (ImageView) findViewById(R.id.toolIcon);
        toolIcon.setImageResource(R.drawable.file_plus);
        toolIcon.setOnClickListener(this);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        out_page = prefs.getString("out_page", "");

        btVal = (EditText) findViewById(R.id.btVal);
        btVal.setFocusableInTouchMode(true);
        btVal.setFocusable(true);
        btVal.requestFocus();
        btVal.setText("");

        btVal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        });
        //btVal.setInputType(InputType.TYPE_NULL);
        btProducts = (ListView) findViewById(R.id.btProducts);
        btProceed = (Button) findViewById(R.id.btProceed);
        btProceed.setOnClickListener(this);
        BarcodeTaskRunner.run();
    }

    private Runnable BarcodeTaskRunner = new Runnable()
    {
        public void run()
        {
            String ed_bar = btVal.getText().toString();
            if(BTlist.contains(ed_bar.trim())){
                //Toast.makeText(getApplicationContext(), "Barcode already exists", Toast.LENGTH_LONG).show();
            }else if(!ed_bar.isEmpty()){
                BTlist.add(ed_bar.trim());

                DatabaseHelper getDelivery = new DatabaseHelper(BTScan.this);
                getDelivery.open();
                ArrayList<String> prodData = getDelivery.getStockDetails(ed_bar.trim());
                if(prodData.size() > 0){
                    item_barcode.add(ed_bar.trim());
                    //list to array conversion
                    String[] prod_Arr = new String[prodData.size()];
                    prod_Arr = prodData.toArray(prod_Arr);
                    item_id.add(prod_Arr[0]);
                    item_name.add(prod_Arr[1]);
                    item_price.add(prod_Arr[3]);
                    item_qty.add(prod_Arr[4]);
                    item_img.add(prod_Arr[5]);
                    String [] id_arr = item_id.toArray(new String[item_id.size()]);
                    String [] name_arr = item_name.toArray(new String[item_name.size()]);
                    String [] img_arr = item_img.toArray(new String[item_img.size()]);
                    String [] price_arr = item_price.toArray(new String[item_price.size()]);
                    String [] qty_arr = item_qty.toArray(new String[item_qty.size()]);
                    listProducts = new BTStock(BTScan.this,id_arr,name_arr,qty_arr,price_arr,img_arr);
                    btProducts.setAdapter(listProducts);
                }
                getDelivery.close();
                btVal.setText("");
            }
            handler.postDelayed(this, 2000);
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btProceed:
                if(listProducts!=null){
                    if(out_page.contains("deliveryBtn")){
                        Intent delivery_process = new Intent(BTScan.this,StockDeliveryScan.class);
                        delivery_process.putExtra("barcode_arr",item_barcode);
                        startActivity(delivery_process);
                    }else if(out_page.contains("returnBtn")){
                        Intent return_process = new Intent(BTScan.this,StockReturnScan.class);
                        return_process.putExtra("barcode_arr",item_barcode);
                        startActivity(return_process);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Error..There is no products", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.toolIcon:
                manualDlg();
                break;
        }
    }

    public void manualDlg(){
        final Dialog dlg = new Dialog(BTScan.this);
        dlg.setTitle("Enter Barcode");
        dlg.setContentView(R.layout.bt_manual_add);
        mBarcode = (EditText)dlg.findViewById(R.id.mBarcode);
        spCancel = (Button) dlg.findViewById(R.id.spCancel);
        spSet = (Button) dlg.findViewById(R.id.spSet);
        spSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String maCode = mBarcode.getText().toString().trim();
                maCode= maCode.replace(" ","");
                if(!maCode.isEmpty()){
                    if(!BTlist.contains(maCode)){
                        BTlist.add(maCode);
                        DatabaseHelper getEntry = new DatabaseHelper(BTScan.this);
                        getEntry.open();
                        ArrayList<String> maData = getEntry.getStockDetails(maCode);
                        if(maData.size() > 0){
                            item_barcode.add(maCode);
                            //list to array conversion
                            String[] prod_Arr = new String[maData.size()];
                            prod_Arr = maData.toArray(prod_Arr);
                            item_id.add(prod_Arr[0]);
                            item_name.add(prod_Arr[1]);
                            item_price.add(prod_Arr[3]);
                            item_qty.add(prod_Arr[4]);
                            item_img.add(prod_Arr[5]);
                            String [] id_arr = item_id.toArray(new String[item_id.size()]);
                            String [] name_arr = item_name.toArray(new String[item_name.size()]);
                            String [] img_arr = item_img.toArray(new String[item_img.size()]);
                            String [] price_arr = item_price.toArray(new String[item_price.size()]);
                            String [] qty_arr = item_qty.toArray(new String[item_qty.size()]);
                            listProducts = new BTStock(BTScan.this,id_arr,name_arr,qty_arr,price_arr,img_arr);
                            btProducts.setAdapter(listProducts);
                        }
                        getEntry.close();
                        mBarcode.setText("");
                    }
                    dlg.dismiss();
                }
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
