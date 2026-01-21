package src.models;

/**
 * MealPlan Model Class
 */
public class MealPlan {
    private int id;
    private int recipeId;
    private String planDate;
    private String mealType;
    private Recipe recipe; // For joining with Recipe data
    
    /**
     * Default constructor
     */
    public MealPlan() {
    }
    
    /**
     * Constructor without ID (for new meal plans)
     */
    public MealPlan(int recipeId, String planDate, String mealType) {
        this.recipeId = recipeId;
        this.planDate = planDate;
        this.mealType = mealType;
    }
    
    /**
     * Full constructor
     */
    public MealPlan(int id, int recipeId, String planDate, String mealType) {
        this.id = id;
        this.recipeId = recipeId;
        this.planDate = planDate;
        this.mealType = mealType;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public int getRecipeId() {
        return recipeId;
    }
    
    public String getPlanDate() {
        return planDate;
    }
    
    public String getMealType() {
        return mealType;
    }
    
    public Recipe getRecipe() {
        return recipe;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }
    
    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }
    
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
    
    /**
     * Validate meal plan data
     */
    public boolean isValid() {
        return recipeId > 0 && 
               planDate != null && !planDate.trim().isEmpty() &&
               mealType != null && !mealType.trim().isEmpty();
    }
    
    /**
     * Check if this meal plan is for today
     */
    public boolean isToday() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return planDate.equals(today.toString());
    }
    
    @Override
    public String toString() {
        String recipeName = (recipe != null) ? recipe.getName() : "Recipe #" + recipeId;
        return mealType + ": " + recipeName + " (" + planDate + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MealPlan mealPlan = (MealPlan) obj;
        return id == mealPlan.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}