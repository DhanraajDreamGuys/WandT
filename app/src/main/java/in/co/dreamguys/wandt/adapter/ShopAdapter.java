package in.co.dreamguys.wandt.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import in.co.dreamguys.wandt.R;

/**
 * Created by gopi.s on 30-Jan-16.
 */

public class ShopAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] shop_id;
    private final String[] shop_name;
    public ShopAdapter(Activity context, String[] shop_id,String[] shop_name) {
        super(context, R.layout.stock_out_item, shop_id);
        this.context = context;
        this.shop_id = shop_id;
        this.shop_name = shop_name;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.stock_out_item, null, true);

        TextView prod_name = (TextView) rowView.findViewById(R.id.stockoutID);
        TextView prod_qty = (TextView) rowView.findViewById(R.id.stockTotal);
        TextView prod_date = (TextView) rowView.findViewById(R.id.stockDate);
        TextView prod_amt = (TextView) rowView.findViewById(R.id.stockPrice);

        prod_name.setText("#"+shop_id[position]);
        prod_qty.setText(shop_name[position]);
        prod_amt.setText("");
        prod_date.setText("");
        return rowView;
    };
}