package in.co.dreamguys.wandt.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import in.co.dreamguys.wandt.R;

public class StockSales extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] order_id;
    private final String[] shop_name;
    private final String[] sale_qty;
    private final String[] sale_amt;
    private final String[] sale_date;

    public StockSales(Activity context, String[] order_id,String[] shop_name, String[] sale_qty,String[] sale_amt,String[] sale_date) {
        super(context, R.layout.stock_out_item, order_id);
        this.context = context;
        this.order_id = order_id;
        this.shop_name = shop_name;
        this.sale_qty = sale_qty;
        this.sale_amt = sale_amt;
        this.sale_date = sale_date;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.order_view, null, true);
        TextView product_name = (TextView)rowView.findViewById(R.id.product_name);
        TextView product_cat = (TextView)rowView.findViewById(R.id.product_category);
        TextView product_qty = (TextView)rowView.findViewById(R.id.product_qty);
        TextView product_amt = (TextView)rowView.findViewById(R.id.product_amt);
        TextView product_date = (TextView)rowView.findViewById(R.id.product_date);
        product_name.setText("ORDER: "+order_id[position]);
        product_cat.setText(shop_name[position]);
        product_qty.setText(sale_qty[position]);
        product_date.setText(sale_date[position]);
        product_amt.setText("\u00a3" + sale_amt[position]);
        return rowView;
    };
}