package projects.dao;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ProjectDao extends DaoBase {
    @SuppressWarnings("unused")
    private static final String CATEGORY_TABLE = "category";
    private static final String MATERIAL_TABLE = "material";
    private static final String PROJECT_TABLE = "project";
    private static final String PROJECT_CATEGORY_TABLE = "project_category";
    private static final String STEP_TABLE = "step";

    public Project insertProject(Project project) {
        // Changed getName() to getProjectName()
        System.out.println("ProjectDao: Inserting project " + project.getProjectName());
        String sql = ""
                + "INSERT INTO " + PROJECT_TABLE + " "
                + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
                + "VALUES "
                + "(?, ?, ?, ?, ?)";
        try(Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);
            try(PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameter(stmt, 1, project.getProjectName(), String.class);
                setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
                setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
                setParameter(stmt, 4, project.getDifficulty(), Integer.class);
                setParameter(stmt, 5, project.getNotes(), String.class);

                stmt.executeUpdate();

                Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
                commitTransaction(conn);

                project.setProjectId(projectId);
                return project;
            }
            catch(Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        }
        catch(SQLException e) {
            throw new DbException(e);
        }
    }

    public List<Project> fetchAllProjects() {
        String sql = "SELECT * FROM project ORDER BY project_name ASC";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Project> projects = new ArrayList<>();

                    while (rs.next()) {
                        projects.add(extractProject(rs));
                    }

                    commitTransaction(conn);
                    return projects;
                }
            }
            catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        }
        catch (SQLException e) {
            throw new DbException(e);
        }
    }

    private Project extractProject(ResultSet rs) throws SQLException {
        Project project = new Project();

        project.setProjectId(rs.getInt("project_id"));
        project.setProjectName(rs.getString("project_name"));
        project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
        project.setActualHours(rs.getBigDecimal("actual_hours"));
        project.setDifficulty(rs.getInt("difficulty"));
        project.setNotes(rs.getString("notes"));

        return project;
    }

    public Optional<Project> fetchProjectById(Integer projectId) {
        String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

        try(Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try {
                Project project = null;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    setParameter(stmt, 1, projectId, Integer.class);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            project = extractProject(rs);
                        }
                    }
                }

                if(Objects.nonNull(project)) {
                    project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
                    project.getSteps().addAll(fetchStepsForProject(conn, projectId));
                    project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
                }

                commitTransaction(conn);

                return Optional.ofNullable(project);
            }
            catch(Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        }
        catch (SQLException e) {
            throw new DbException(e);
        }
    }

    private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
        String sql = ""
                + "SELECT m.* FROM " + MATERIAL_TABLE + " m "
                + "JOIN " + PROJECT_TABLE + " p USING (project_id) "
                + "WHERE project_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameter(stmt, 1, projectId, Integer.class);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Material> materials = new LinkedList<>();

                while (rs.next()) {
                    materials.add(extract(rs, Material.class));
                }
                return materials;
            }
        }
    }

    private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
        String sql = ""
                + "SELECT c.* FROM " +CATEGORY_TABLE + " c "
                + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
                + "WHERE project_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameter(stmt, 1, projectId, Integer.class);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Category> categories = new LinkedList<>();

                while (rs.next()) {
                    categories.add(extract(rs, Category.class));
                }
                return categories;
            }
        }
    }

    private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
        String sql = ""
                + "SELECT s.* FROM " +STEP_TABLE + " s "
                + "JOIN " + PROJECT_TABLE + " p USING (project_id) "
                + "WHERE project_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameter(stmt, 1, projectId, Integer.class);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Step> steps = new LinkedList<>();

                while (rs.next()) {
                    steps.add(extract(rs, Step.class));
                }
                return steps;
            }
        }
    }
}