package co.createlou.cmta;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.ArrayList;



/**
 * Created by Bryan on 1/5/17.
 */

public class Project extends ArrayList<Report> implements Serializable,Parcelable {
    private String projectName;
    private String projectNumber;
    private String projectLocation;
    private String deviceID;
    public Project() {

    }




    public Project(String projectName, String projectNumber, String projectLocation, String userKey) {
        this.projectName = projectName;
        this.projectNumber = projectNumber;
        this.projectLocation = projectLocation;
        this.deviceID = userKey;

    }
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public String getProjectNumber() {
        return projectNumber;
    }
    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }
    public String getProjectLocation() {
        return projectLocation;
    }
    public void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation;
    }
    public String getUserKey() {
        return deviceID;
    }

    protected Project(Parcel in) {
        projectName = in.readString();
        projectNumber = in.readString();
        projectLocation = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(projectName);
        dest.writeString(projectNumber);
        dest.writeString(projectLocation);

    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

}
