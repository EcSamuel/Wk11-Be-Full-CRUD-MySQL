package projects.service;

import projects.entity.Project;
import projects.dao.ProjectDao;
import java.math.BigDecimal;


public class ProjectService {
    private ProjectDao projectDao = new ProjectDao();

    public Project addProject(Project project) {
        return projectDao.insertProject(project);
    }

    public List<Project> fetchAllProjects() {
        return projectDao.fetchAllProjects();
    }

    public void addProject(String projectName, BigDecimal projectBudget) {
        System.out.println("Adding project " + projectName);
        Project project = new Project();
        project.setProjectName(projectName);
        project.setEstimatedHours(projectBudget);
        projectDao.insertProject(project);
    }
}
