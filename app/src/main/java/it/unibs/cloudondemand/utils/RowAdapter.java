package it.unibs.cloudondemand.utils;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import it.unibs.cloudondemand.MainActivity;
import it.unibs.cloudondemand.R;

/**
 * Adapter for a file explorer listview.
 */
public class RowAdapter extends BaseAdapter{
    // Array of file/folder into current directory.
    private ArrayList<FileListable> toRead;
    
    private Context context;

    public RowAdapter(Context context, ArrayList<FileListable> toRead) {
        this.context=context;
        this.toRead=toRead;
    }

    @Override
    public int getCount() {
        return toRead.size();
    }

    @Override
    public FileListable getItem(int position) {
        return toRead.get(position);
    }

    @Override
    public long getItemId(int position) {
        return toRead.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row, null);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageButton);
        TextView textView = (TextView) convertView.findViewById(R.id.riga);

        // Custom item at position 0 (go back)
        if (position == 0) {
            imageView.setImageResource(R.drawable.ic_file_folder);
            textView.setText("/..");
            return convertView;
        }

        // Retrieve file
        FileListable file = getItem(position);

        // Assign icon
        if (file.isDirectory())
            imageView.setImageResource(R.drawable.ic_file_folder);
        else {
            int drawableResource = Utils.getFileIcon(file.getName().substring(file.getName().lastIndexOf('.') + 1, file.getName().length()));
            imageView.setImageResource(drawableResource);
        }
        // Assign text
        textView.setText(file.getName());

        return convertView;
    }
}