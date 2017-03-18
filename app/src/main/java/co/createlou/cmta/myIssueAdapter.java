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

public class myIssueAdapter extends BaseAdapter {

    private ViewHolder viewHolder;
    private Context mContext;

    private ArrayList<Issue> mDataSource;

    private static class ViewHolder {
        private TextView issueStatus;
        private TextView issueDetails;
        private ImageView issueImage;
    }
    public myIssueAdapter(Context context, ArrayList<Issue> items) {
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
            viewHolder.issueStatus = (TextView) convertView.findViewById(R.id.issueStatus);
            viewHolder.issueDetails = (TextView) convertView.findViewById(R.id.issueDetails);
            viewHolder.issueImage = (ImageView) convertView.findViewById(R.id.issueImage);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Issue item = (Issue) getItem(position);
        if (item!= null) {

            viewHolder.issueStatus.setText(mDataSource.get(position).issueStatus);
            viewHolder.issueDetails.setText(mDataSource.get(position).issueDetails);
            viewHolder.issueImage.setBackground(mDataSource.get(position).bitmapDrawable);
        }

        return convertView;
    }

}
