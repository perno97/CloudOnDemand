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

public class RowAdapter extends BaseAdapter{

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
    public Object getItem(int position) {
        return toRead.get(position);
    }

    @Override
    public long getItemId(int position) {
        return toRead.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
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
        File file=(File) getItem(position);

        // Assign icon
        if(file.isDirectory())
            imageView.setImageResource(R.drawable.ic_file_folder);
        else {
            int drawableResource = getFileIcon(file.getName().substring(file.getName().lastIndexOf('.')+1, file.getName().length()));
            imageView.setImageResource(drawableResource);
        }
        // Assign text
        textView.setText(file.getName());

        return convertView;
    }

    private int getFileIcon (String extension) {
        int drawableResource;
        switch (extension.toLowerCase()) {
            // Image files
            case ("png"):
            case ("jpg"):
            case ("jpeg"):
            case ("gif"):
            case ("tif"):
            case ("svg"):
                drawableResource = R.drawable.ic_file_image;
                break;
            // Music files
            case ("mp3"):
            case ("m4a"):
            case ("mpa"):
            case ("wav"):
            case ("wma"):
                drawableResource = R.drawable.ic_file_music;
                break;
            // Video files
            case ("3g2"):
            case ("3gp"):
            case ("avi"):
            case ("flv"):
            case ("m4v"):
            case ("mov"):
            case ("mp4"):
            case ("mpg"):
            case ("wmv"):

                drawableResource = R.drawable.ic_file_video;
                break;
            // Plain text files
            case ("txt"):
            case ("csv"):
            case ("log"):
            case ("html"):
            case ("htm"):
            case ("js"):
            case ("php"):
            case ("css"):
            case ("xml"):
            case ("c"):
            case ("cpp"):
            case ("py"):
            case ("sh"):
            case ("lua"):
                drawableResource = R.drawable.ic_file_document;
                break;
            // Word processor files
            case ("doc"):
            case ("docx"):
            case ("odt"):
                drawableResource = R.drawable.ic_file_word;
                break;
            // Spreadsheet files
            case ("xls"):
            case ("xlsx"):
            case ("xlr"):
                drawableResource = R.drawable.ic_file_excel;
                break;
            // Pdf files
            case ("pdf"):
                drawableResource = R.drawable.ic_file_document;
                break;
            // Compressed archive files
            case ("zip"):
            case ("tar"):
            case ("gz"):
            case ("rar"):
            case ("7z"):
                drawableResource = R.drawable.ic_file_compressed_archive;
                break;
            // Extension not
            default:
                drawableResource = R.drawable.ic_file_empty;
                break;
            }

        return drawableResource;
    }
}