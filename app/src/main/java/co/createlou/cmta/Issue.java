package co.createlou.cmta;

import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created by Bryan on 3/6/17.
 */

public class Issue implements Parcelable {
    public String issueStatus;
    public String issueDetails;
    public String issueLocation;
    public byte[] imagedata;
    public BitmapDrawable bitmapDrawable;

    public Issue() {
        this.issueLocation = "Issue Location";
        this.issueStatus = "Issue Status";
        this.issueDetails = "Details";
    }
    public Issue(String location, String status, String details) {
        this.issueStatus = status;
        this.issueDetails = details;
        this.issueLocation = location;
    }
    public Issue(String location, String status, String details, byte[] data, BitmapDrawable bitmapDrawable) {
        this.issueStatus = status;
        this.issueDetails = details;
        this.imagedata = data;
        this.issueLocation = location;
        this.bitmapDrawable = bitmapDrawable;
    }


    public String getIssueStatus() {
        return issueStatus;
    }

    public void setIssueStatus(String issueStatus) {
        this.issueStatus = issueStatus;
    }

    public String getIssueDetails() {
        return issueDetails;
    }

    public void setIssueDetails(String issueDetails) {
        this.issueDetails = issueDetails;
    }

    public String getIssueLocation() {
        return issueLocation;
    }

    public void setIssueLocation(String issueLocation) {
        this.issueLocation = issueLocation;
    }

    public byte[] getData() {
        return imagedata;
    }

    public void setData(byte[] data) {
        this.imagedata = data;
    }
    public BitmapDrawable getIssueImage(){
        return bitmapDrawable;
    }


    //Parcelization of object
    public Issue(Parcel in){
        String[] data = new String[3];
        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.issueDetails = data[0];
        this.issueLocation = data[1];
        this.issueStatus= data[2];
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.issueDetails,
                this.issueLocation,
                this.issueStatus});
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

