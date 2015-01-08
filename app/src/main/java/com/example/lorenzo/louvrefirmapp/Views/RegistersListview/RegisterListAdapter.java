package com.example.lorenzo.louvrefirmapp.Views.RegistersListview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.lorenzo.louvrefirmapp.R;

import java.util.List;

/**
 Adapter used to visualize register entries
 */
public class RegisterListAdapter<T> extends ArrayAdapter<RegisterItems.Item>
{

    List<RegisterItems.Item> itemsList;


    static class ViewHolder
    {
        public TextView t1;
        public TextView t2;
    }


    public RegisterListAdapter(Context context, int resource, List<RegisterItems.Item> objects)
    {
        super(context, resource, objects);

        this.itemsList = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;

        if(rowView == null) // No reusable row view, inflate XML and associate a new ViewHolder
        {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.register_list_item, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.t1 = (TextView)rowView.findViewById(R.id.row_header);
            viewHolder.t2 = (TextView)rowView.findViewById(R.id.row_content);
            rowView.setTag(viewHolder);
        }

        // Fill the view with data
        ViewHolder viewHolder = (ViewHolder)rowView.getTag();
        RegisterItems.Item item = itemsList.get(position);
        viewHolder.t1.setText(item.getId());
        viewHolder.t2.setText(item.getContent());

        return rowView;
    }
}
