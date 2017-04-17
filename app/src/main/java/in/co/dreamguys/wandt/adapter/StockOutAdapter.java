package in.co.dreamguys.wandt.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import in.co.dreamguys.wandt.R;

/**
 * Created by gopi.s on 07-Dec-15.
 */

public class StockOutAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] stock_id;
    private final String[] stock_qty;
    private final String[] stock_amt;
    private final String[] stock_date;

    public StockOutAdapter(Activity context, String[] stock_id, String[] stock_qty,String[] stock_amt,String[] stock_date) {
        super(context, R.layout.stock_out_item, stock_id);
        this.context = context;
        this.stock_id = stock_id;
        this.stock_qty = stock_qty;
        this.stock_amt = stock_amt;
        this.stock_date = stock_date;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.stock_out_item, null, true);

        TextView stId = (TextView) rowView.findViewById(R.id.stockoutID);
        TextView stQty = (TextView) rowView.findViewById(R.id.stockTotal);
        TextView stDate = (TextView) rowView.findViewById(R.id.stockDate);
        TextView stPrice = (TextView) rowView.findViewById(R.id.stockPrice);

        stId.setText("Stock ID: "+stock_id[position]);
        stQty.setText("Qty: "+stock_qty[position]);
        stDate.setText("Date: "+stock_date[position]);
        stPrice.setText("Amount: "+stock_amt[position]);
        return rowView;
    };

}