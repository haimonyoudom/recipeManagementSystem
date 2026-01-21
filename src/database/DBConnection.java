package src.database;

import java.sql.*;

/**
 * Singleton class for database connection management
 */
public class DBConnection {
    private static DBConnection instance;
    private Connection connection;
    private static final String URL = "jdbc:sqlite:recipes.db";
    
    private DBConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(URL);
            initializeDatabase();
            System.out.println("Database connected successfully!");
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get singleton instance
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get database connection
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Initialize database schema and sample data
     */
    private void initializeDatabase() {
        try {
            Statement stmt = connection.createStatement();
            
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Create Recipes table
            stmt.execute("CREATE TABLE IF NOT EXISTS Recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "category TEXT," +
                "cookingTime INTEGER," +
                "instructions TEXT," +
                "isFavorite INTEGER DEFAULT 0)");
            
            // Create Ingredients table
            stmt.execute("CREATE TABLE IF NOT EXISTS Ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "recipeId INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "quantity TEXT," +
                "FOREIGN KEY(recipeId) REFERENCES Recipes(id) ON DELETE CASCADE)");
            
            // Create MealPlan table
            stmt.execute("CREATE TABLE IF NOT EXISTS MealPlan (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "recipeId INTEGER NOT NULL," +
                "planDate TEXT NOT NULL," +
                "mealType TEXT NOT NULL," +
                "FOREIGN KEY(recipeId) REFERENCES Recipes(id) ON DELETE CASCADE)");
            
            // Check if sample data exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Recipes");
            if (rs.next() && rs.getInt(1) == 0) {
                insertSampleData();
                System.out.println("Sample data inserted successfully!");
            }
            
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Insert sample recipe data
     */
    private void insertSampleData() {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO Recipes (name, category, cookingTime, instructions) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            
            // Sample Recipe 1: Spaghetti Carbonara
            pstmt.setString(1, "Spaghetti Carbonara");
            pstmt.setString(2, "Italian");
            pstmt.setInt(3, 25);
            pstmt.setString(4, "1. Boil pasta in salted water until al dente\n" +
                "2. Fry bacon until crispy\n" +
                "3. Mix eggs and grated Parmesan cheese\n" +
                "4. Drain pasta and combine with bacon\n" +
                "5. Remove from heat and stir in egg mixture\n" +
                "6. Season with black pepper and serve");
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                addIngredients(rs.getInt(1), new String[][]{
                    {"Spaghetti", "400g"},
                    {"Bacon", "200g"},
                    {"Eggs", "4"},
                    {"Parmesan cheese", "100g"},
                    {"Black pepper", "to taste"}
                });
            }
            
            // Sample Recipe 2: Chicken Stir Fry
            pstmt.setString(1, "Chicken Stir Fry");
            pstmt.setString(2, "Asian");
            pstmt.setInt(3, 20);
            pstmt.setString(4, "1. Cut chicken into bite-sized pieces\n" +
                "2. Slice bell peppers and onions\n" +
                "3. Heat oil in wok over high heat\n" +
                "4. Stir fry chicken until cooked\n" +
                "5. Add vegetables and stir fry for 3-4 minutes\n" +
                "6. Add soy sauce and serve over rice");
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                addIngredients(rs.getInt(1), new String[][]{
                    {"Chicken breast", "500g"},
                    {"Bell peppers", "2"},
                    {"Onion", "1"},
                    {"Soy sauce", "3 tbsp"},
                    {"Vegetable oil", "2 tbsp"}
                });
            }
            
            // Sample Recipe 3: Greek Salad
            pstmt.setString(1, "Greek Salad");
            pstmt.setString(2, "Mediterranean");
            pstmt.setInt(3, 10);
            pstmt.setString(4, "1. Chop tomatoes and cucumber into chunks\n" +
                "2. Slice red onion thinly\n" +
                "3. Combine vegetables in a large bowl\n" +
                "4. Add feta cheese and olives\n" +
                "5. Dress with olive oil and lemon juice\n" +
                "6. Season with oregano, salt, and pepper");
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                addIngredients(rs.getInt(1), new String[][]{
                    {"Tomatoes", "4"},
                    {"Cucumber", "1"},
                    {"Feta cheese", "200g"},
                    {"Kalamata olives", "100g"},
                    {"Red onion", "1"},
                    {"Olive oil", "3 tbsp"},
                    {"Lemon juice", "2 tbsp"}
                });
            }
            
            // Sample Recipe 4: Beef Tacos
            pstmt.setString(1, "Beef Tacos");
            pstmt.setString(2, "Mexican");
            pstmt.setInt(3, 15);
            pstmt.setString(4, "1. Brown ground beef in a skillet\n" +
                "2. Add taco seasoning and water\n" +
                "3. Simmer until sauce thickens\n" +
                "4. Warm taco shells in oven\n" +
                "5. Fill shells with beef\n" +
                "6. Top with lettuce, cheese, and salsa");
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                addIngredients(rs.getInt(1), new String[][]{
                    {"Ground beef", "500g"},
                    {"Taco shells", "12"},
                    {"Lettuce", "1 head"},
                    {"Cheddar cheese", "150g"},
                    {"Taco seasoning", "1 packet"},
                    {"Salsa", "200g"}
                });
            }
            
            // Sample Recipe 5: Chocolate Cake
            pstmt.setString(1, "Chocolate Cake");
            pstmt.setString(2, "Dessert");
            pstmt.setInt(3, 45);
            pstmt.setString(4, "1. Preheat oven to 180°C\n" +
                "2. Mix flour, sugar, cocoa powder, and baking powder\n" +
                "3. Add eggs, milk, and oil\n" +
                "4. Beat until smooth\n" +
                "5. Pour into greased pan\n" +
                "6. Bake for 30-35 minutes\n" +
                "7. Cool completely before frosting");
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                addIngredients(rs.getInt(1), new String[][]{
                    {"All-purpose flour", "300g"},
                    {"Sugar", "250g"},
                    {"Cocoa powder", "50g"},
                    {"Eggs", "3"},
                    {"Milk", "200ml"},
                    {"Vegetable oil", "100ml"},
                    {"Baking powder", "2 tsp"}
                });
            }
            
        } catch (SQLException e) {
            System.err.println("Error inserting sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to add ingredients for a recipe
     */
    private void addIngredients(int recipeId, String[][] ingredients) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO Ingredients (recipeId, name, quantity) VALUES (?, ?, ?)");
        
        for (String[] ing : ingredients) {
            pstmt.setInt(1, recipeId);
            pstmt.setString(2, ing[0]);
            pstmt.setString(3, ing[1]);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test connection
     */
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}