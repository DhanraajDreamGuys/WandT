package in.co.dreamguys.wandt.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import in.co.dreamguys.wandt.R;

public class InvoiceAdapter extends BaseAdapter {
    private Context yContext;
    private final String[] prod_name;
    private final String[] prod_qty;
    private final String[] prod_amt;

    public InvoiceAdapter(Context context, String[] prod_name, String[] prod_qty, String[] prod_amt) {
        yContext = context;
        this.prod_name = prod_name;
        this.prod_qty = prod_qty;
        this.prod_amt = prod_amt;
    }

    @Override
    public int getCount()
    {
        return  prod_name.length;
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
            productList = inflater.inflate(R.layout.purchased_custom_list_item, null);
        } else {
            productList = (View) convertView;
        }
        TextView name_tv = (TextView)productList.findViewById(R.id.name_tv);
        name_tv.setTypeface(null, Typeface.NORMAL);
        TextView qty_tv = (TextView)productList.findViewById(R.id.qty_tv);
        qty_tv.setTypeface(null, Typeface.NORMAL);
        TextView price_tv = (TextView)productList.findViewById(R.id.price_tv);
        price_tv.setTypeface(null, Typeface.NORMAL);

        name_tv.setText(prod_name[position]);
        qty_tv.setText(prod_qty[position]);
        price_tv.setText(prod_amt[position]);
        return productList;
    }
}
