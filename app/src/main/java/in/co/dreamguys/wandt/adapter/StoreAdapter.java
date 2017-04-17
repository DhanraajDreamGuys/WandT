package in.co.dreamguys.wandt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import in.co.dreamguys.wandt.R;



public class StoreAdapter extends BaseAdapter {
    private Context yContext;
    private final String[] st_id;
    private final String[] st_bill;
    private final String[] st_item;
    private final String[] st_amt;

    public StoreAdapter(Context context, String[] st_id, String[] st_bill, String[] st_item, String[] st_amt) {
        yContext = context;
        this.st_id = st_id;
        this.st_bill = st_bill;
        this.st_item = st_item;
        this.st_amt = st_amt;
    }

    @Override
    public int getCount()
    {
        return  st_id.length;
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
            productList = inflater.inflate(R.layout.home_view, null);
        } else {
            productList = (View) convertView;
        }
        TextView tag = (TextView)productList.findViewById(R.id.tag);
        TextView product_name = (TextView)productList.findViewById(R.id.product_name);
        TextView product_cat = (TextView)productList.findViewById(R.id.product_category);
        TextView product_qty = (TextView)productList.findViewById(R.id.product_qty);
        TextView product_amt = (TextView)productList.findViewById(R.id.product_amt);
        ImageView thumbnail = (ImageView) productList.findViewById(R.id.thumbnail);
        thumbnail.setImageResource(R.drawable.sales_report);
        tag.setText(st_id[position]);
        product_name.setText(st_bill[position]);
        product_cat.setText(st_item[position]+" items");
        product_qty.setText("\u00a3"+st_amt[position]);
        product_amt.setText("");
        return productList;
    }
}
