import projects.entity.Project;
import projects.service.ProjectService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.math.BigDecimal;

public class ProjectsApp {
    private List<String> operations = List.of(
            "1) Add a project",
            "2) List projects",
            "3) Select a project"
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
                    default -> System.out.println("\n" + selection + " is not a valid selection. Try again.");
                }
            } catch (Exception e) {
                System.out.println("\nError: " + e + " Try Again.");
            }
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