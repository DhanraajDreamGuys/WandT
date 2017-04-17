package in.co.dreamguys.wandt.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import in.co.dreamguys.wandt.R;

public class StockOutDetailsAdapter extends BaseAdapter {

    private Context yContext;
    private final String[] prod_id;
    private final String[] prod_name;
    private final String[] prod_price;
    private final String[] prod_qty;
    private final Drawable[] prod_img;

    public StockOutDetailsAdapter(Context context, String[] prod_id, String[] prod_name, String[] prod_qty, String[] prod_price,Drawable[] prod_img) {
        yContext = context;
        this.prod_id = prod_id;
        this.prod_name = prod_name;
        this.prod_price = prod_price;
        this.prod_qty = prod_qty;
        this.prod_img = prod_img;
    }

    @Override
    public int getCount()
    {
        return  prod_id.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View productList;
        LayoutInflater inflater = (LayoutInflater) yContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            productList = new View(yContext);
            productList = inflater.inflate(R.layout.stock_out_details_item, null);
        } else {
            productList = (View) convertView;
        }
        TextView product_id = (TextView)productList.findViewById(R.id.product_id);
        TextView product_name = (TextView)productList.findViewById(R.id.product_name);
        TextView product_qty = (TextView)productList.findViewById(R.id.product_qty);
        TextView product_amt = (TextView)productList.findViewById(R.id.product_amt);
        ImageView thumbnail = (ImageView) productList.findViewById(R.id.thumbnail);

        product_id.setText("Product ID: "+prod_id[position]);
        product_name.setText(prod_name[position]);
        product_qty.setText("Qty: "+prod_qty[position]);
        product_amt.setText("Price: "+prod_price[position]);
        thumbnail.setImageDrawable(prod_img[position]);
        return productList;
    }
}
