package projects.service;

import projects.entity.Project;
import projects.dao.ProjectDao;
import projects.exception.DbException;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ProjectService {
    private ProjectDao projectDao = new ProjectDao();

    public Project addProject(Project project) {
        return projectDao.insertProject(project);
    }

    public List<Project> fetchAllProjects() {
        return projectDao.fetchAllProjects();
    }

    public void addProject(String projectName, BigDecimal estimatedHours) {
        System.out.println("Adding project " + projectName);
        Project project = new Project();
        project.setProjectName(projectName);
        project.setEstimatedHours(estimatedHours);
        projectDao.insertProject(project);
    }

    public Project fetchProjectById(Integer projectId) {
        return projectDao.fetchProjectById(projectId)
                .orElseThrow(() -> new NoSuchElementException("A Project with id " + projectId + " does not exist"));
    }

    public void modifyProjectDetails(Project newProject) {
        if (!projectDao.modifyProjectDetails(newProject)) {
            throw new DbException("Project with ID=" + newProject.getProjectId() + " does not exist");
        }
    }
}