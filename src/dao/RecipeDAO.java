package src.dao;

import src.database.DBConnection;
import src.models.Ingredient;
import src.models.Recipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Recipe operations
 */
public class RecipeDAO {
    private Connection conn;
    
    public RecipeDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }
    
    /**
     * Get all recipes from database ,ratana
     */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT * FROM Recipes ORDER BY name";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Recipe recipe = mapResultSetToRecipe(rs);
                recipe.setIngredients(getIngredientsForRecipe(recipe.getId()));
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all recipes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recipes;

    }
    
    /**
     * Get recipe by ID , ratana
     */
    public Recipe getRecipeById(int id) {
         String sql = "SELECT * FROM Recipes WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Recipe recipe = mapResultSetToRecipe(rs);
                recipe.setIngredients(getIngredientsForRecipe(id));
                return recipe;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching recipe by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;

    }
    
    /**
     * Insert new recipe , ratana
     */
    public boolean insertRecipe(Recipe recipe) {
        if (!recipe.isValid()) {
            System.err.println("Invalid recipe data");
            return false;
        }
        
        String sql = "INSERT INTO Recipes (name, category, cookingTime, instructions, isFavorite) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, recipe.getName());
            pstmt.setString(2, recipe.getCategory());
            pstmt.setInt(3, recipe.getCookingTime());
            pstmt.setString(4, recipe.getInstructions());
            pstmt.setInt(5, recipe.isFavorite() ? 1 : 0);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int recipeId = rs.getInt(1);
                    recipe.setId(recipeId);
                    
                    // Insert ingredients
                    for (Ingredient ingredient : recipe.getIngredients()) {
                        insertIngredient(recipeId, ingredient);
                    }
                    
                    System.out.println("Recipe inserted successfully with ID: " + recipeId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting recipe: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;

    }
    
    /**
     * Update existing recipe , ratana
     */
    public boolean updateRecipe(Recipe recipe) {
       if (!recipe.isValid()) {
            System.err.println("Invalid recipe data");
            return false;
        }
        
        String sql = "UPDATE Recipes SET name=?, category=?, cookingTime=?, " +
                     "instructions=?, isFavorite=? WHERE id=?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, recipe.getName());
            pstmt.setString(2, recipe.getCategory());
            pstmt.setInt(3, recipe.getCookingTime());
            pstmt.setString(4, recipe.getInstructions());
            pstmt.setInt(5, recipe.isFavorite() ? 1 : 0);
            pstmt.setInt(6, recipe.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Delete old ingredients and insert new ones
                deleteIngredientsForRecipe(recipe.getId());
                
                for (Ingredient ingredient : recipe.getIngredients()) {
                    insertIngredient(recipe.getId(), ingredient);
                }
                
                System.out.println("Recipe updated successfully: " + recipe.getId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating recipe: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;

    }
    
    /**
     * Delete recipe by ID
     */
    public boolean deleteRecipe(int id) {
        String sql = "DELETE FROM Recipes WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Recipe deleted successfully: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting recipe: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Search recipes by keyword (name or category)
     */
    public List<Recipe> searchRecipes(String keyword) {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT * FROM Recipes WHERE name LIKE ? OR category LIKE ? " +
                     "OR instructions LIKE ? ORDER BY name";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Recipe recipe = mapResultSetToRecipe(rs);
                recipe.setIngredients(getIngredientsForRecipe(recipe.getId()));
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            System.err.println("Error searching recipes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recipes;
    }
    
    /**
     * Filter recipes by category
     */
    public List<Recipe> filterByCategory(String category) {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT * FROM Recipes WHERE category = ? ORDER BY name";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Recipe recipe = mapResultSetToRecipe(rs);
                recipe.setIngredients(getIngredientsForRecipe(recipe.getId()));
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            System.err.println("Error filtering recipes by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recipes;
    }
    
    /**
     * Get all favorite recipes
     */
    public List<Recipe> getFavoriteRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT * FROM Recipes WHERE isFavorite = 1 ORDER BY name";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Recipe recipe = mapResultSetToRecipe(rs);
                recipe.setIngredients(getIngredientsForRecipe(recipe.getId()));
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching favorite recipes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recipes;
    }
    
    /**
     * Get all unique categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM Recipes WHERE category IS NOT NULL " +
                     "ORDER BY category";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }
    
    /**
     * Check if recipe name already exists
     */
    public boolean recipeNameExists(String name, int excludeId) {
        String sql = "SELECT COUNT(*) FROM Recipes WHERE name = ? AND id != ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, excludeId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking recipe name: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Recipe object
     */
    private Recipe mapResultSetToRecipe(ResultSet rs) throws SQLException {
        return new Recipe(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getInt("cookingTime"),
            rs.getString("instructions"),
            rs.getInt("isFavorite") == 1
        );
    }
    
    /**
     * Get all ingredients for a recipe
     */
    private List<Ingredient> getIngredientsForRecipe(int recipeId) {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT * FROM Ingredients WHERE recipeId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ingredients.add(new Ingredient(
                    rs.getInt("id"),
                    rs.getInt("recipeId"),
                    rs.getString("name"),
                    rs.getString("quantity")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ingredients: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ingredients;
    }
    
    /**
     * Insert ingredient for a recipe
     */
    private void insertIngredient(int recipeId, Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO Ingredients (recipeId, name, quantity) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            pstmt.setString(2, ingredient.getName());
            pstmt.setString(3, ingredient.getQuantity());
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Delete all ingredients for a recipe
     */
    private void deleteIngredientsForRecipe(int recipeId) throws SQLException {
        String sql = "DELETE FROM Ingredients WHERE recipeId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            pstmt.executeUpdate();
        }
    }
}