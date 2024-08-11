import projects.entity.Project;
import projects.service.ProjectService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.math.BigDecimal;
import java.lang.reflect.Field;

public class ProjectsApp {
    private List<String> operations = List.of(
            "1) Add a project",
            "2) List projects",
            "3) Select a project",
            "4) Update a project",
            "5) DELETE PROJECT"
    );

    private Project curProject;

    private Scanner scanner = new Scanner(System.in);

    private ProjectService projectService = new ProjectService();

    public void processUserSelections() {
        boolean done = false;

        while (!done) {
            try {
                int selection = getUserSelection();
                switch (selection) {
                    case -1 -> done = exitMenu();
                    case 1 -> {
                        System.out.println("Adding a project");
                        createProject();
                    }
                    case 2 -> {
                        System.out.println("Listing projects");
                        listProjects();
                    }
                    case 3 -> {
                        System.out.println("Selecting a project");
                        selectProject();
                    }
                    case 4 -> {
                        System.out.println("Updating project");
                        updateProjectDetails();
                    }
                    default -> System.out.println("\n" + selection + " is not a valid selection. Try again.");
                    case 5 -> {
                        System.out.println("Enter the project_id of the project you wish to delete. This action cannot be undone!");
                        listProjects();
                        Scanner scanner = new Scanner(System.in);
                        int projectId = scanner.nextInt();
                        try {
                            projectService.deleteProject(projectId);
                            System.out.println("Attempting to delete project " + projectId);

                            if (curProject != null && curProject.getProjectId() == projectId) {
                                curProject = null;
                                System.out.println("Current project set to null after deletion");
                            }
                        } catch (Exception e) {
                            System.out.println("Project with Id" + projectId + " cannot be deleted." + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("\nError: " + e + " Try Again.");
            }
        }
    }

    private void updateProjectDetails() {
        // a. Check if curProject is null
        if (curProject == null) {
            System.out.println("\nPlease select a project.");
            return;
        }

        // b. Print current project details
        System.out.println("Current project details:");
        try {
            Field[] fields = Project.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(curProject);
                System.out.println(fieldName + ": " + fieldValue);
            }
        } catch (IllegalAccessException e) {
            System.out.println("Error accessing project fields: " + e.getMessage());
        }

        // c. Create new Project object and update fields
        Project newProject = new Project();
        Scanner scanner = new Scanner(System.in);

        try {
            Field[] fields = Project.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if (fieldName.equals("projectId") || fieldName.equals("materials") || fieldName.equals("categories")|| fieldName.equals("steps")) {
                    continue; // Skip fields that shouldn't be modified here
                }
                System.out.print("Enter new value for " + fieldName + " (or press Enter to keep current value): ");
                String userInput = scanner.nextLine().trim();

                if (!userInput.isEmpty()) {
                    setFieldValue(newProject, field, userInput);
                } else {
                    field.set(newProject, field.get(curProject));
                }
            }
        } catch (IllegalAccessException e) {
            System.out.println("Error updating project fields: " + e.getMessage());
        }

        // d. Set the project ID
        newProject.setProjectId(curProject.getProjectId());

        // e. Call projectService.modifyProjectDetails()
        projectService.modifyProjectDetails(newProject);

        // f. Reread the current project
        curProject = projectService.fetchProjectById(curProject.getProjectId());

        System.out.println("Project details updated successfully.");
    }

    private void setFieldValue(Project project, Field field, String value) {
        try {
            Class<?> fieldType = field.getType();
            if (fieldType == String.class) {
                field.set(project, value);
            } else if (fieldType == Integer.class || fieldType == int.class) {
                field.set(project, Integer.parseInt(value));
            } else if (fieldType == BigDecimal.class) {
                field.set(project, new BigDecimal(value));
            } else if (fieldType == Double.class || fieldType == double.class) {
                field.set(project, Double.parseDouble(value));
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            System.out.println("Error setting field " + field.getName() + ": " + e.getMessage());
        }
    }

    private void createProject() {
        String projectName = getStringInput("Enter Project Name:");
        BigDecimal estimatedHours = getDecimalInput("Enter Estimated Hours:");
        BigDecimal actualHours = getDecimalInput("Enter Actual Hours:");
        Integer difficulty = getIntInput("Enter Difficulty, on a scale of 1(low) to 5(high):");
        String notes = getStringInput("Enter Notes(optional):");
        Project project = new Project();

        project.setProjectName(projectName);
        project.setEstimatedHours(estimatedHours);
        project.setActualHours(actualHours);
        project.setDifficulty(difficulty);
        project.setNotes(notes);

        Project dbProject = projectService.addProject(project);
        System.out.println(dbProject + " successfully created");
    }

    private BigDecimal getDecimalInput(String prompt) {
        String input = getStringInput(prompt);

        if (Objects.isNull(input)) {
            return null;
        }

        try {
            BigDecimal decimal = new BigDecimal(input).setScale(2);
            return decimal;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(input + " is not a valid decimal number.");
        }
    }

    private boolean exitMenu() {
        System.out.println("\nExiting program...");
        return true;
    }

    private int getUserSelection() {
        printOperations();

        Integer input = getIntInput("Enter a menu selection");

        return Objects.isNull(input) ? -1 : input;
    }

    private void printOperations() {
        System.out.println("\nThese are the available selections. Press the Enter key to quit");

        operations.forEach(line -> System.out.println(" " + line));

        if(Objects.isNull(curProject)) {
            System.out.println("\nYou are not working with a project.");
        } else {
            System.out.println("\nYou are working with project: " + curProject);
        }
    }

    private void listProjects() {
        List<Project> projects = projectService.fetchAllProjects();
        System.out.println("\nProjects:");
        projects.forEach(project -> System.out.println(
                " " + project.getProjectId()
                        + ": " + project.getProjectName()));
    }

    private Integer getIntInput(String prompt) {
        String input = getStringInput(prompt);

        if (Objects.isNull(input)) {
            return null;
        }

        try {
            return Integer.valueOf(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(input + " is not a valid number.");
        }
    }

    private void selectProject() {
        listProjects();
        Integer projectId = null;

        while (projectId == null) {
            projectId = getIntInput("Enter a project ID to select a project");
            if (projectId == null) {
                System.out.println("Please enter a valid project ID");
            }
        }

        try {
            curProject = projectService.fetchProjectById(projectId);
            System.out.println("\nYou have selected project: " + curProject.getProjectName());
        } catch (NoSuchElementException e) {
            System.out.println("\nError: " + e.getMessage());
            curProject = null;
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine();
        return input.isBlank() ? null : input.trim();
    }

    public static void main(String[] args) {
        new ProjectsApp().processUserSelections();
    }
}