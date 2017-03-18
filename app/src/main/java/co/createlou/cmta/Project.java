package co.createlou.cmta;

import java.util.ArrayList;

/**
 * Created by Bryan on 1/5/17.
 */

public class Project {
    public String projectName;
    public String projectNumber;
    public String projectLocation;
    public Project() {
        this.projectName = "Project Name";
        this.projectNumber = "Project Number";
        this.projectLocation = "Project Location";
    }

    public Project(String projectName, String projectNumber, String projectLocation) {
        this.projectName = projectName;
        this.projectNumber = projectNumber;
        this.projectLocation = projectLocation;
    }


}
