package co.createlou.cmta;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bryan on 1/22/17.
 */

public class myProjectAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private Context mContext;

    private ArrayList<Project> mDataSource;

    private static class ViewHolder {
        private TextView projectName;
    }
    public myProjectAdapter(Context context, ArrayList<Project> items) {
        mContext = context;
        mDataSource = items;
    }

    public int getCount() {
        return mDataSource.size();
    }
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.itemlistrow, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.projectName = (TextView) convertView.findViewById(R.id.projectName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Project item = (Project) getItem(position);
        if (item!= null) {

            viewHolder.projectName.setText(mDataSource.get(position).getProjectName());
        }

        return convertView;
    }

}
