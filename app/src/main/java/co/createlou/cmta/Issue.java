package co.createlou.cmta;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bryan on 3/6/17.
 */

public class Issue implements Parcelable {
    public String status;
    public String details;
    public String location;
    public String image;
    public byte[] data;
    public BitmapDrawable bitmapDrawable;
    public Bitmap bmap;
    public String report;

    public Issue() {
    }
    public Issue(String location, String image, String status, String details, String report) {
        this.status = status;
        this.details = details;
        this.report = report;
        this.location = location;
        this.image=image;

    }
    public Issue(String location, String status, String details, String report) {
        this.status = status;
        this.details = details;
        this.report = report;
        this.location = location;
    }
    public Issue(String location, String status, String details, String report, byte[] data,String image, BitmapDrawable bitmapDrawable, Bitmap bmap) {
        this.status = status;
        this.details = details;
        this.data = data;
        this.report = report;
        this.location = location;
        this.bitmapDrawable = bitmapDrawable;
        this.bmap = bmap;
        this.image = image;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getLocation()     {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public BitmapDrawable getIssueImage(){
        return bitmapDrawable;
    }

    public Bitmap getBmap() {
        return bmap;
    }

    public void setBitmapDrawable(BitmapDrawable bitmapDrawable) {
        this.bitmapDrawable = bitmapDrawable;
    }

    public void setBmap(Bitmap bmap) {
        this.bmap = bmap;
    }

    //Parcelization of object
    public Issue(Parcel in){
        String[] data = new String[3];
        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.details = data[0];
        this.location = data[1];
        this.status = data[2];
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.details,
                this.location,
                this.status});
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Issue createFromParcel(Parcel in) {
            return new Issue(in);
        }

        public Issue[] newArray(int size) {
            return new Issue[size];
        }
    };
}

