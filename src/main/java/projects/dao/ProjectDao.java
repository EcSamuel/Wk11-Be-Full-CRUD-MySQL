package projects.dao;

import projects.entity.Project;
import projects.exception.DbException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        return new Project(
            rs.getInt("project_id"),
            rs.getString("project_name"),
            rs.getBigDecimal("estimated_hours"),
            rs.getBigDecimal("actual_hours"),
            rs.getInt("difficulty"),
            rs.getString("notes")
        );
    }
}