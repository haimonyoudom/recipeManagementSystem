package src.dao;

import src.database.DBConnection;
import src.models.MealPlan;
import src.models.Recipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for MealPlan operations
 */
public class MealPlanDAO {
    private Connection conn;
    private RecipeDAO recipeDAO;
    
    public MealPlanDAO() {
        this.conn = DBConnection.getInstance().getConnection();
        this.recipeDAO = new RecipeDAO();
    }
    
    /**
     * Insert new meal plan
     */
    public boolean insertMealPlan(MealPlan mealPlan) {
        if (!mealPlan.isValid()) {
            System.err.println("Invalid meal plan data");
            return false;
        }
        
        String sql = "INSERT INTO MealPlan (recipeId, planDate, mealType) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, mealPlan.getRecipeId());
            pstmt.setString(2, mealPlan.getPlanDate());
            pstmt.setString(3, mealPlan.getMealType());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    mealPlan.setId(rs.getInt(1));
                }
                System.out.println("Meal plan inserted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting meal plan: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get meal plans for a specific date
     */
    public List<MealPlan> getMealPlanForDate(String date) {
        List<MealPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM MealPlan WHERE planDate = ? ORDER BY " +
                     "CASE mealType " +
                     "WHEN 'Breakfast' THEN 1 " +
                     "WHEN 'Lunch' THEN 2 " +
                     "WHEN 'Dinner' THEN 3 " +
                     "WHEN 'Snack' THEN 4 " +
                     "ELSE 5 END";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                MealPlan plan = mapResultSetToMealPlan(rs);
                plan.setRecipe(recipeDAO.getRecipeById(plan.getRecipeId()));
                plans.add(plan);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching meal plans for date: " + e.getMessage());
            e.printStackTrace();
        }
        
        return plans;
    }
    
    /**
     * Get all meal plans
     */
    public List<MealPlan> getAllMealPlans() {
        List<MealPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM MealPlan ORDER BY planDate DESC";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                MealPlan plan = mapResultSetToMealPlan(rs);
                plan.setRecipe(recipeDAO.getRecipeById(plan.getRecipeId()));
                plans.add(plan);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all meal plans: " + e.getMessage());
            e.printStackTrace();
        }
        
        return plans;
    }
    
    /**
     * Get meal plans within a date range (for weekly summary)
     */
    public List<MealPlan> getWeeklySummary(String startDate, String endDate) {
        List<MealPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM MealPlan WHERE planDate BETWEEN ? AND ? " +
                     "ORDER BY planDate, " +
                     "CASE mealType " +
                     "WHEN 'Breakfast' THEN 1 " +
                     "WHEN 'Lunch' THEN 2 " +
                     "WHEN 'Dinner' THEN 3 " +
                     "WHEN 'Snack' THEN 4 " +
                     "ELSE 5 END";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                MealPlan plan = mapResultSetToMealPlan(rs);
                plan.setRecipe(recipeDAO.getRecipeById(plan.getRecipeId()));
                plans.add(plan);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching weekly summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return plans;
    }
    
    /**
     * Update meal plan
     */
    public boolean updateMealPlan(MealPlan mealPlan) {
        if (!mealPlan.isValid()) {
            System.err.println("Invalid meal plan data");
            return false;
        }
        
        String sql = "UPDATE MealPlan SET recipeId=?, planDate=?, mealType=? WHERE id=?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mealPlan.getRecipeId());
            pstmt.setString(2, mealPlan.getPlanDate());
            pstmt.setString(3, mealPlan.getMealType());
            pstmt.setInt(4, mealPlan.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Meal plan updated successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating meal plan: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete meal plan by ID
     */
    public boolean deleteMealPlan(int id) {
        String sql = "DELETE FROM MealPlan WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Meal plan deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting meal plan: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete all meal plans for a specific date
     */
    public boolean deleteMealPlansForDate(String date) {
        String sql = "DELETE FROM MealPlan WHERE planDate = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            int affectedRows = pstmt.executeUpdate();
            System.out.println(affectedRows + " meal plans deleted for date: " + date);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting meal plans for date: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Check if a recipe is already planned for a specific date and meal type
     */
    public boolean isMealPlanned(int recipeId, String date, String mealType) {
        String sql = "SELECT COUNT(*) FROM MealPlan WHERE recipeId = ? AND planDate = ? AND mealType = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            pstmt.setString(2, date);
            pstmt.setString(3, mealType);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking meal plan: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get count of meal plans for a date
     */
    public int getMealPlanCountForDate(String date) {
        String sql = "SELECT COUNT(*) FROM MealPlan WHERE planDate = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting meal plans: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Map ResultSet to MealPlan object
     */
    private MealPlan mapResultSetToMealPlan(ResultSet rs) throws SQLException {
        return new MealPlan(
            rs.getInt("id"),
            rs.getInt("recipeId"),
            rs.getString("planDate"),
            rs.getString("mealType")
        );
    }
}