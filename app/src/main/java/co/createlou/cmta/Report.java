package co.createlou.cmta;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Bryan on 3/23/2017.
 */

public class Report extends ArrayList<Issue> implements Parcelable {
    private String preparedBy;
    private String project;
    private String punchListType;
    private String siteVisitDate;

    public Report(){
        this.preparedBy = "GenericUser";
        this.project = "GenericProject";
        this.punchListType = "GenericReason";
    }
    public Report(String preparedBy, String project, String punchListType, String siteVisitDate){
        this.preparedBy = preparedBy;
        this.project = project;
        this.punchListType = punchListType;
        this.siteVisitDate = siteVisitDate;
    }

    public String getPreparedBy() {
        return preparedBy;
    }
    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }
    public String getProject() {
        return project;
    }
    public void setProject(String project) {
        this.project = project;
    }
    public String getPunchListType() {
        return punchListType;
    }
    public void setPunchListType(String punchListType) {
        this.punchListType = punchListType;
    }
    public String getSiteVisitDate() {
        return siteVisitDate;
    }
    public void setSiteVisitDate(String siteVisitDate) {
        this.siteVisitDate = siteVisitDate;
    }

    protected Report(Parcel in) {
        preparedBy = in.readString();
        project = in.readString();
        punchListType = in.readString();
        siteVisitDate = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(preparedBy);
        dest.writeString(project);
        dest.writeString(punchListType);
        dest.writeString(siteVisitDate);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Report> CREATOR = new Parcelable.Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };
}
