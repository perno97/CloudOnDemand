package it.unibs.cloudondemand.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import it.unibs.cloudondemand.R;

public class RowAdapter extends BaseAdapter{

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

        ImageView imageButton=(ImageView) convertView.findViewById(R.id.imageButton);
        if(file.isDirectory())
            imageButton.setImageResource(R.drawable.ic_folder_black_24dp);
        else
            imageButton.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);

        TextView textView = (TextView) convertView.findViewById(R.id.riga);
        textView.setText(file.getName());

        return convertView;
    }
}