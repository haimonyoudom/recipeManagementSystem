package src.models;

/**
 * Ingredient Model Class
 */
public class Ingredient {
    private int id;
    private int recipeId;
    private String name;
    private String quantity;
    
    /**
     * Default constructor
     */
    public Ingredient() {
    }
    
    /**
     * Constructor without ID (for new ingredients)
     */
    public Ingredient(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
    }
    
    /**
     * Full constructor
     */
    public Ingredient(int id, int recipeId, String name, String quantity) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public int getRecipeId() {
        return recipeId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getQuantity() {
        return quantity;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    
    /**
     * Validate ingredient data
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        if (quantity != null && !quantity.trim().isEmpty()) {
            return quantity + " - " + name;
        }
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ingredient that = (Ingredient) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}