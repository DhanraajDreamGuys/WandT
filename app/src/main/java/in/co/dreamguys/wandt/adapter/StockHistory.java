package in.co.dreamguys.wandt.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import in.co.dreamguys.wandt.R;

public class StockHistory extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] stock_name;
    private final String[] stock_qty;
    private final String[] stock_date;
    private final Drawable[] stock_img;

    public StockHistory(Activity context, String[] stock_name, Drawable[] stock_img,String[] stock_qty,String[] stock_date) {
        super(context, R.layout.stock_out_item, stock_name);
        this.context = context;
        this.stock_name = stock_name;
        this.stock_img = stock_img;
        this.stock_qty = stock_qty;
        this.stock_date = stock_date;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.stock_out_item, null, true);

        TextView prod_name = (TextView) rowView.findViewById(R.id.stockoutID);
        TextView prod_qty = (TextView) rowView.findViewById(R.id.stockTotal);
        TextView prod_date = (TextView) rowView.findViewById(R.id.stockDate);
        TextView prod_amt = (TextView) rowView.findViewById(R.id.stockPrice);
        ImageView thumbnail = (ImageView) rowView.findViewById(R.id.stock_icon);

        prod_name.setText(stock_name[position]);
        prod_qty.setText("Qty: "+stock_qty[position]);
        prod_date.setText("Date: " + stock_date[position]);
        thumbnail.setImageDrawable(stock_img[position]);
        prod_amt.setText("");
        return rowView;
    };
}