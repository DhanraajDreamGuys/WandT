package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import in.co.dreamguys.wandt.util.DatabaseHelper;

public class StockReturnScan extends Activity implements View.OnClickListener{
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    TextView title;
    String stock_id;
    Button returnScan,returnProceed;
    ListView returnProducts;
    ScanMoreReturn returnItem;


    DecimalFormat dec = new DecimalFormat("#.00");
    ArrayList<String> item_id = new ArrayList<String>();
    ArrayList<String> item_name = new ArrayList<String>();
    ArrayList<String> item_barcode = new ArrayList<String>();
    ArrayList<String> item_price = new ArrayList<String>();
    ArrayList<String> item_qty = new ArrayList<String>();
    ArrayList<String> barcode_arr;
    String scan_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.stock_return_scan);
        DatabaseHelper emptyReturn = new DatabaseHelper(StockReturnScan.this);
        emptyReturn.open();
        emptyReturn.deleteReturnDetails();
        emptyReturn.close();
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("RETURN ITEM");
        returnProducts = (ListView) findViewById(R.id.returnProducts);
        returnScan = (Button) findViewById(R.id.returnScan);
        returnScan.setOnClickListener(this);
        returnProceed = (Button) findViewById(R.id.returnProceed);
        returnProceed.setOnClickListener(this);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        stock_id = prefs.getString("stock_id", "");
        scan_switch = prefs.getString("scan_switch", "");
        if(scan_switch.contentEquals("BT")){
            returnScan.setVisibility(View.INVISIBLE);
            barcode_arr = (ArrayList<String>) getIntent().getSerializableExtra("barcode_arr");
            BarcodeTask();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(scan_switch.contentEquals("CAMERA")) {
            //Intent so = new Intent(StockReturnScan.this, MainStockItem.class);
            //startActivity(so);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.returnScan:
                /*IntentIntegrator integrator = new IntentIntegrator(StockReturnScan.this);
                integrator.initiateScan();*/

                Intent intent = new Intent("com.summi.scan");
                intent.setPackage("com.sunmi.sunmiqrcodescanner");
                intent.putExtra("CURRENT_PPI", 0X0003);//The current preview resolution ,PPI_1920_1080 = 0X0001;PPI_1280_720 = 0X0002;PPI_BEST = 0X0003;

                intent.putExtra("PLAY_SOUND", true);// Prompt tone after scanning  ,default true

                intent.putExtra("PLAY_VIBRATE", false);//vibrate after scanning,default false,only support M1 right now.

                intent.putExtra("IDENTIFY_INVERSE_QR_CODE", true);//Whether to identify inverse code

                intent.putExtra("IDENTIFY_MORE_CODE", false);// Whether to identify several code，default false

                intent.putExtra("IS_SHOW_SETTING", true);// Wether display set up button  at the top-right corner，default true

                intent.putExtra("IS_SHOW_ALBUM", true);// Wether display album，default true
                startActivityForResult(intent, 1001);
                break;

            case R.id.returnProceed:
                if(returnItem!=null){
                    DatabaseHelper insertReturn = new DatabaseHelper(StockReturnScan.this);
                    insertReturn.open();
                    int count = returnProducts.getAdapter().getCount();
                    String[] listData = new String[count];
                    int totQty = 0;
                    float totAmt =0;
                    for (int i = 0; i < count; i++) {
                        //View v1 = returnProducts.getChildAt(i);
                        View v1 = getViewByPosition(i, returnProducts);
                        TextView prodid = (TextView) v1.findViewById(R.id.prod_id);
                        TextView prodname = (TextView) v1.findViewById(R.id.prod_name);
                        TextView prodcode = (TextView) v1.findViewById(R.id.prod_code);
                        TextView prodprice = (TextView) v1.findViewById(R.id.prod_price);
                        //EditText prod_qty = (EditText) v1.findViewById(R.id.prod_qty);
                        Spinner prod_qty = (Spinner) v1.findViewById(R.id.prod_qty);

                        String fd1 = prodid.getText().toString().trim();
                        String fd2 = prodcode.getText().toString().trim();
                        //String fd3 = prod_qty.getText().toString().trim();
                        String fd3 = String.valueOf(prod_qty.getSelectedItem().toString());
                        String fd4 = prodprice.getText().toString().trim();
                        if(!fd3.isEmpty() && !fd3.equals("0")) {
                            /*String[] fd1_arr;
                            fd1_arr = fd1.split(":");
                            String fd1_str = fd1_arr[1];*/
                            String fd1_str = fd1.replace("#","");
                            insertReturn.insertReturnDetails(stock_id, fd1_str, fd2, fd3);
                        }
                        totQty = totQty + Integer.parseInt(fd3);
                        totAmt = totAmt + ( Float.parseFloat(fd4) * Integer.parseInt(fd3));
                    }
                    insertReturn.close();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("totQty", String.valueOf(totQty));
                    editor.putString("totAmt", dec.format(totAmt));
                    editor.apply();
                    Intent soReturn = new Intent(StockReturnScan.this,StockOutReturn.class);
                    startActivity(soReturn);
                }else{
                    Toast.makeText(getApplicationContext(), "Error..There is no products", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void BarcodeTask(){
        for (int i=0; i < barcode_arr.size(); i++){
            DatabaseHelper getDelivery = new DatabaseHelper(StockReturnScan.this);
            getDelivery.open();
            ArrayList<String> prodData = getDelivery.getStockDetails(barcode_arr.get(i).trim());
            if(prodData.size() > 0){
                if(!item_barcode.contains(barcode_arr.get(i).trim())){
                    String[] prod_Arr = new String[prodData.size()];
                    prod_Arr = prodData.toArray(prod_Arr);
                    item_id.add(prod_Arr[0]);
                    item_name.add(prod_Arr[1]);
                    item_barcode.add(prod_Arr[2]);
                    item_price.add(prod_Arr[3]);
                    item_qty.add(prod_Arr[4]);
                    String [] id_arr = item_id.toArray(new String[item_id.size()]);
                    String [] name_arr = item_name.toArray(new String[item_name.size()]);
                    String [] code_arr = item_barcode.toArray(new String[item_barcode.size()]);
                    String [] price_arr = item_price.toArray(new String[item_price.size()]);
                    String [] qty_arr = item_qty.toArray(new String[item_qty.size()]);
                    ArrayList<String> mQty = new ArrayList<String>();
                    String [] mQty_arr = mQty.toArray(new String[mQty.size()]);
                    returnItem = new ScanMoreReturn(this,id_arr,name_arr,code_arr,price_arr,qty_arr,mQty_arr);
                    returnProducts.setAdapter(returnItem);
                    returnItem.notifyDataSetChanged();
                }
            }
            getDelivery.close();
        }
    }

   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                DatabaseHelper getDelivery = new DatabaseHelper(StockReturnScan.this);
                getDelivery.open();

                ArrayList<String> prodData = getDelivery.getStockDetails(contents.trim());
                if(prodData.size() > 0){
                    if(item_barcode.contains(contents.trim())){
                        Toast.makeText(getApplicationContext(), "Product already exists", Toast.LENGTH_LONG).show();
                    }else{
                        String[] prod_Arr = new String[prodData.size()];
                        prod_Arr = prodData.toArray(prod_Arr);
                        item_id.add(prod_Arr[0]);
                        item_name.add(prod_Arr[1]);
                        item_barcode.add(prod_Arr[2]);
                        item_price.add(prod_Arr[3]);
                        item_qty.add(prod_Arr[4]);

                        String [] id_arr = item_id.toArray(new String[item_id.size()]);
                        String [] name_arr = item_name.toArray(new String[item_name.size()]);
                        String [] code_arr = item_barcode.toArray(new String[item_barcode.size()]);
                        String [] price_arr = item_price.toArray(new String[item_price.size()]);
                        String [] qty_arr = item_qty.toArray(new String[item_qty.size()]);

                        ArrayList<String> mQty = new ArrayList<String>();
                        int dlCount = 0;
                        try {
                            dlCount = returnProducts.getAdapter().getCount();
                        } catch ( NullPointerException e ) {
                            //e
                        }
                        mQty.clear();
                        for (int i = 0; i < dlCount; i++) {
                            try {
                                View dpView = getViewByPosition(i, returnProducts);
                                try{
                                    Spinner dp_qty = (Spinner) dpView.findViewById(R.id.prod_qty);
                                    mQty.add(String.valueOf(dp_qty.getSelectedItemPosition()));
                                }catch(Exception e){
                                }
                            }catch ( NullPointerException e ) {
                            }
                        }
                        String [] mQty_arr = mQty.toArray(new String[mQty.size()]);
                        returnItem = new ScanMoreReturn(this,id_arr,name_arr,code_arr,price_arr,qty_arr,mQty_arr);
                        returnProducts.setAdapter(returnItem);
                        returnItem.notifyDataSetChanged();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Invalid Product", Toast.LENGTH_LONG).show();
                }
                getDelivery.close();
            }else{
                Toast.makeText(getApplicationContext(), "Invalid Product", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Invalid Product", Toast.LENGTH_LONG).show();
        }
    }*/

    public View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition ) {
            return listView.getAdapter().getView(position, null, listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public  class ScanMoreReturn extends BaseAdapter {

        private Context yContext;
        private final String[] item_id;
        private final String[] item_name;
        private final String[] item_barcode;
        private final String[] item_price;
        private final String[] item_qty;
        private final String[] mQty;

        ScanMoreReturn(Context context, String[] item_id, String[] item_name, String[] item_barcode, String[] item_price, String[] item_qty, String[] mQty) {
            yContext = context;
            this.item_id = item_id;
            this.item_name = item_name;
            this.item_barcode = item_barcode;
            this.item_price = item_price;
            this.item_qty = item_qty;
            this.mQty = mQty;
        }

        @Override
        public int getCount() {
            return item_name.length;
        }

        @Override
        public Object getItem(int position) {
            return item_name[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View returnList;
            LayoutInflater inflater = (LayoutInflater) yContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                returnList = new View(yContext);
                returnList = inflater.inflate(R.layout.return_scan,parent, false);
            } else {
                returnList = (View) convertView;
            }
            TextView prod_id = (TextView)returnList.findViewById(R.id.prod_id);
            prod_id.setText("#"+item_id[position]);
            TextView prod_name = (TextView)returnList.findViewById(R.id.prod_name);
            prod_name.setText(item_name[position]);
            TextView prod_code = (TextView)returnList.findViewById(R.id.prod_code);
            prod_code.setText(item_barcode[position]);
            TextView prod_price = (TextView)returnList.findViewById(R.id.prod_price);
            prod_price.setText(""+dec.format(Float.parseFloat(item_price[position])));
            ArrayList<String> prod_li = new ArrayList<String>();
            for (int i = 1; i <= Integer.parseInt(item_qty[position]); ++i) {
                prod_li.add(String.valueOf(i));
            }
            final Spinner np = (Spinner)returnList.findViewById(R.id.prod_qty);
            ArrayAdapter<String> li_adapter = new ArrayAdapter<String>(StockReturnScan.this, android.R.layout.simple_spinner_dropdown_item, prod_li);
            np.setAdapter(li_adapter);
            try {
                np.setSelection(Integer.parseInt(mQty[position]));
            } catch ( IndexOutOfBoundsException e ) {
                //e
            }
            //EditText prod_qty = (EditText)returnList.findViewById(R.id.prod_qty);
            //prod_qty.addTextChangedListener(new MyTextWatcher(returnList));
            return returnList;
        }
    }

    /*private class MyTextWatcher implements TextWatcher {

        private View view;
        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //do nothing
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //
        }
        public void afterTextChanged(Editable arg0) {


            if (arg0.length() > 0) {
                String chk_prod_id;
                TextView chk_prod = (TextView) view.findViewById(R.id.prod_id);
                String[] separated;
                chk_prod_id = chk_prod.getText().toString().trim();
                separated = chk_prod_id.split(":");
                DatabaseHelper checkQty = new DatabaseHelper(StockReturnScan.this);
                checkQty.open();
                String dbQty = checkQty.checkStockQty(separated[1]);
                int fld1 = Integer.parseInt(arg0.toString()), fld2 = Integer.parseInt(dbQty);
                if (fld1 > fld2){
                    EditText chk_prod_qty = (EditText)view.findViewById(R.id.prod_qty);
                    chk_prod_qty.setText(dbQty);
                }
                checkQty.close();
            }
            return;
        }
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
           if (requestCode == 1001 && data != null) {
            Bundle bundle = data.getExtras();
            ArrayList<HashMap<String, String>> result = (ArrayList<HashMap<String, String>>) bundle.getSerializable("data");

            assert result != null;

            for (HashMap<String, String> hashMap : result) {
                Log.i("sunmi", hashMap.get("TYPE"));//this is the type of the code
                Log.i("sunmi", hashMap.get("VALUE"));//this is the result of the code
            }
        } else {
            Toast.makeText(getApplicationContext(), "Invalid Product", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
