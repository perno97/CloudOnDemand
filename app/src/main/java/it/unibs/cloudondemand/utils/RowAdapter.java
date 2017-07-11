package it.unibs.cloudondemand.utils;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibs.cloudondemand.R;

public class RowAdapter <File> extends BaseAdapter{

    private ArrayList<File> toRead=null;
    Context context=null;

    public RowAdapter(Context context, ArrayList<File> toRead) {
        this.context=context;
        this.toRead=toRead;
    }

    @Override
    public int getCount() {
        return toRead.size();
    }

    @Override
    public Object getItem(int position) {
        return toRead.get(position);
    }

    @Override
    public long getItemId(int position) {
        return toRead.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)
        {
            convertView= LayoutInflater.from(context).inflate(R.layout.row, null);
        }
        File file=(File) getItem(position);
        ImageButton ib=(ImageButton) convertView.findViewById(R.id.imageButton);
        return convertView;
    }
}