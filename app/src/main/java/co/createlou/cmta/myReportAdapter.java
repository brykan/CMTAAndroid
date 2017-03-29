package co.createlou.cmta;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bryan on 1/22/17.
 */

public class myReportAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private Context mContext;

    private ArrayList<Report> mDataSource = new ArrayList<>();

    private static class ViewHolder {
        private TextView report;
    }
    public myReportAdapter(Context context, ArrayList<Report> items) {
        mContext = context;
        mDataSource = items;
    }

    public int getCount() {
        return mDataSource.size();
    }
    @Override
    public Report getItem(int position) {
        return mDataSource.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.reportlistrow, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.report = (TextView) convertView.findViewById(R.id.report);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Report item = (Report) getItem(position);
        if (item!= null) {
            viewHolder.report.setText(mDataSource.get(position).getPunchListType()+" - " + mDataSource.get(position).getPreparedBy());
        }

        return convertView;
    }

}
