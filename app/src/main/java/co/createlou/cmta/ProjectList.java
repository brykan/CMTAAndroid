package co.createlou.cmta;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Bryan on 3/23/2017.
 */

public class ProjectList implements Parcelable{
    public ArrayList<Project> projectList = new ArrayList<>();
    public ProjectList(){

    }

    public ArrayList<Project> getProjectList() {
        return projectList;
    }

    public void setProjectList(ArrayList<Project> projectList) {
        this.projectList = projectList;
    }

    public void addProject(Project project) {
        projectList.add(project);

    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(projectList);
    }

    private ProjectList(Parcel in) {
        in.readTypedList(projectList, Project.CREATOR);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProjectList> CREATOR = new Creator<ProjectList>() {
        @Override
        public ProjectList createFromParcel(Parcel in) {
            return new ProjectList(in);
        }

        @Override
        public ProjectList[] newArray(int size) {
            return new ProjectList[size];
        }
    };

}
