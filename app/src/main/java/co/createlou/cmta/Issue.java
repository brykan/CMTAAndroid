package co.createlou.cmta;

import android.graphics.drawable.BitmapDrawable;

import java.net.URI;

/**
 * Created by Bryan on 3/6/17.
 */

public class Issue {
    public String issueStatus;
    public String issueDetails;
    public String issueLocation;
    public byte[] data;
    public BitmapDrawable bitmapDrawable;

    public Issue() {
        this.issueLocation = "Issue Location";
        this.issueStatus = "Issue Status";
        this.issueDetails = "Details";
    }

    public Issue(String location, String status, String details, byte[] data, BitmapDrawable bitmapDrawable) {
        this.issueStatus = status;
        this.issueDetails = details;
        this.data = data;
        this.issueLocation = location;
        this.bitmapDrawable = bitmapDrawable;
    }


}

