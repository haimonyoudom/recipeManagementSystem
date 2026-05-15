package src.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe Model Class (ratana)
 */
public class Recipe {
    private int id;
    private String name;
    private String category;
    private int cookingTime;
    private String instructions;
    private boolean isFavorite;
    private List<Ingredient> ingredients;
    
    /**
     * Default constructor
     */
    public Recipe() {
        this.ingredients = new ArrayList<>();
        this.isFavorite = false;
    }
    
    /**
     * Parameterized constructor
     */
    public Recipe(int id, String name, String category, int cookingTime, 
                  String instructions, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.cookingTime = cookingTime;
        this.instructions = instructions;
        this.isFavorite = isFavorite;
        this.ingredients = new ArrayList<>();
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public int getCookingTime() {
        return cookingTime;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public boolean isFavorite() {
        return isFavorite;
    }
    
    public List<Ingredient> getIngredients() {
        return ingredients;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }
    
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
    
    /**
     * Add a single ingredient
     */
    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }
    
    /**
     * Get ingredients as formatted string
     */
    public String getIngredientsAsString() {
        StringBuilder sb = new StringBuilder();
        for (Ingredient ing : ingredients) {
            sb.append(ing.toString()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Validate recipe data
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && cookingTime >= 0;
    }
    
    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", cookingTime=" + cookingTime +
                ", isFavorite=" + isFavorite +
                ", ingredientsCount=" + ingredients.size() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Recipe recipe = (Recipe) obj;
        return id == recipe.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
 
}