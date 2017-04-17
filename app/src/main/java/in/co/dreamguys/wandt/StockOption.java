package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by gopi.s on 07-Dec-15.
 */
public class StockOption extends Activity implements View.OnClickListener {
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;

    TextView title;
    Button deliveryBtn,returnBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.stock_option);
        //toolbar title setup
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("STOCK OPTION");

        //select stock option button
        deliveryBtn = (Button) findViewById(R.id.deliveryBtn);
        deliveryBtn.setOnClickListener(this);
        returnBtn = (Button) findViewById(R.id.returnBtn);
        returnBtn.setOnClickListener(this);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = prefs.edit();
        switch (v.getId()){
            case R.id.deliveryBtn:
                editor.putString("out_page", "deliveryBtn");
                editor.apply();
                //Intent delivery_scan = new Intent(StockOption.this,StockDeliveryScan.class);
                //startActivity(delivery_scan);
                Intent delivery_bt = new Intent(StockOption.this,BTScan.class);
                startActivity(delivery_bt);
                break;
            case R.id.returnBtn:
                editor.putString("out_page", "returnBtn");
                editor.apply();
                //Intent return_scan = new Intent(StockOption.this,StockReturnScan.class);
                //startActivity(return_scan);
                Intent return_bt = new Intent(StockOption.this,BTScan.class);
                startActivity(return_bt);
                break;
        }
    }

}
