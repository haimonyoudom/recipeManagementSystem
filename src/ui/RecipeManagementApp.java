package src.ui;

import src.dao.MealPlanDAO;
import src.dao.RecipeDAO;
import src.models.Ingredient;
import src.models.MealPlan;
import src.models.Recipe;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Main Recipe Management Application with Swing GUI
 * macOS Compatible Version
 */
public class RecipeManagementApp extends JFrame {
    // CardLayout for switching between panels
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // DAO instances
    private RecipeDAO recipeDAO;
    private MealPlanDAO mealPlanDAO;
    
    // UI Components
    private JTable recipeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    
    // Current state
    private Recipe currentRecipe;
    private Recipe editingRecipe;
    
    // Color scheme - adjusted for better macOS rendering
    private final Color SIDEBAR_COLOR = new Color(52, 73, 94);
    private final Color BUTTON_COLOR = new Color(41, 128, 185);
    private final Color BUTTON_HOVER = new Color(52, 152, 219);
    
    // Detect macOS
    private final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
    
    /**
     * Constructor
     */
    public RecipeManagementApp() {
        // Initialize DAOs
        recipeDAO = new RecipeDAO();
        mealPlanDAO = new MealPlanDAO();
        
        // Setup frame
        setTitle("Recipe Management System");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // macOS-specific frame settings
        if (IS_MAC) {
            // Enable macOS full screen
            try {
                @SuppressWarnings("unchecked")
                Class<?> util = Class.forName("com.apple.eawt.FullScreenUtilities");
                @SuppressWarnings("unchecked")
                java.lang.reflect.Method method = util.getMethod("setWindowCanFullScreen", 
                    java.awt.Window.class, boolean.class);
                method.invoke(util, this, true);
            } catch (Exception e) {
                // Ignore if not available
            }
        }
        
        // Initialize UI
        initializeUI();
        
        setVisible(true);
    }
    
    /**
     * Initialize the user interface
     */
    private void initializeUI() {
        setLayout(new BorderLayout());

        // Create sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Create main content area with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create all panels
        mainPanel.add(createRecipeListPanel(), "LIST");
        mainPanel.add(createRecipeDetailsPanel(), "DETAILS");
        mainPanel.add(createAddEditRecipePanel(), "ADD_EDIT");
        mainPanel.add(createMealPlannerPanel(), "PLANNER");

        add(mainPanel, BorderLayout.CENTER);

        // Show recipe list by default
        showPanel("LIST");
    }

    /**
     * Create recipe details panel
     */
    private JPanel createRecipeDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Details area with macOS-friendly font
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(IS_MAC ? 
            new Font("SF Pro Text", Font.PLAIN, 14) : 
            new Font("Arial", Font.PLAIN, 14));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton backBtn = createStyledButton("← Back to List");
        backBtn.addActionListener(e -> showPanel("LIST"));

        JButton editBtn = createStyledButton("Edit Recipe");
        editBtn.addActionListener(e -> editCurrentRecipe());

        JButton favoriteBtn = createStyledButton("Favorite ⭐");
        favoriteBtn.addActionListener(e -> toggleFavorite());

        JButton addToPlanBtn = createStyledButton("Add to Meal Plan");
        addToPlanBtn.addActionListener(e -> addCurrentRecipeToMealPlan());

        btnPanel.add(backBtn);
        btnPanel.add(editBtn);
        btnPanel.add(favoriteBtn);
        btnPanel.add(addToPlanBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        // Store reference to details area
        panel.putClientProperty("detailsArea", detailsArea);

        return panel;
    }
    
    /**
     * Create a styled button with macOS compatibility
     */
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        if (!IS_MAC) {
            // Only apply custom styling on non-Mac systems
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
        }
        return btn;
    }
    
    /**
     * Create sidebar with navigation buttons
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Title - use SF Pro on macOS
        JLabel title = new JLabel("Recipe Manager");
        title.setFont(IS_MAC ? 
            new Font("SF Pro Display", Font.BOLD, 20) : 
            new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(title);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel subtitle = new JLabel("v1.0");
        subtitle.setFont(IS_MAC ? 
            new Font("SF Pro Text", Font.PLAIN, 12) : 
            new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(Color.LIGHT_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(subtitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        // Navigation buttons
        addSidebarButton(sidebar, "📋 View Recipes", e -> {
            showPanel("LIST");
            refreshRecipeList();
        });

        addSidebarButton(sidebar, "➕ Add Recipe", e -> {
            editingRecipe = null;
            showPanel("ADD_EDIT");
            clearAddEditForm();
        });

        addSidebarButton(sidebar, "📅 Meal Planner", e -> showPanel("PLANNER"));

        addSidebarButton(sidebar, "⭐ Favorites", e -> showFavorites());

        addSidebarButton(sidebar, "🎲 Random Recipe", e -> showRandomRecipe());

        sidebar.add(Box.createVerticalGlue());

        addSidebarButton(sidebar, "ℹ️ Help", e -> showHelp());

        addSidebarButton(sidebar, "❌ Exit", e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        return sidebar;
    }
    
    /**
     * Add a button to the sidebar
     */
    private void addSidebarButton(JPanel sidebar, String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // macOS uses native button rendering
        if (IS_MAC) {
            btn.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(true);
        } else {
            btn.setBackground(BUTTON_COLOR);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            
            // Hover effect for non-Mac
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(BUTTON_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(BUTTON_COLOR);
                }
            });
        }

        btn.addActionListener(action);

        sidebar.add(btn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    /**
     * Create recipe list panel
     */
    private JPanel createRecipeListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel with search and filter
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JLabel searchLabel = new JLabel("🔍 Search:");
        searchField = new JTextField(20);
        JButton searchBtn = createStyledButton("Search");
        searchBtn.addActionListener(e -> performSearch());

        JButton clearSearchBtn = createStyledButton("Clear");
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            refreshRecipeList();
        });

        JLabel filterLabel = new JLabel("Category:");
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        refreshCategoryFilter();
        categoryFilter.addActionListener(e -> applyFilter());

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(clearSearchBtn);
        topPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        topPanel.add(filterLabel);
        topPanel.add(categoryFilter);

        panel.add(topPanel, BorderLayout.NORTH);

        // Recipe table
        String[] columnNames = { "ID", "Recipe Name", "Category", "Time (min)", "Favorite" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recipeTable = new JTable(tableModel);
        recipeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeTable.setRowHeight(IS_MAC ? 28 : 25); // Slightly taller rows on macOS
        recipeTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        recipeTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        recipeTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        recipeTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        recipeTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        // Use native font on macOS
        if (IS_MAC) {
            recipeTable.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        }

        // Double-click to view details
        recipeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showRecipeDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(recipeTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton viewBtn = createStyledButton("View Details");
        viewBtn.addActionListener(e -> showRecipeDetails());

        JButton editBtn = createStyledButton("Edit");
        editBtn.addActionListener(e -> editRecipe());

        JButton deleteBtn = createStyledButton("Delete");
        if (!IS_MAC) {
            deleteBtn.setBackground(new Color(231, 76, 60));
            deleteBtn.setForeground(Color.WHITE);
        }
        deleteBtn.addActionListener(e -> deleteRecipe());

        JButton refreshBtn = createStyledButton("Refresh");
        refreshBtn.addActionListener(e -> refreshRecipeList());

        bottomPanel.add(viewBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(refreshBtn);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Initial load
        refreshRecipeList();

        return panel;
    }
    
    /**
     * Refresh recipe list
     */
    private void refreshRecipeList() {
        tableModel.setRowCount(0);
        List<Recipe> recipes = recipeDAO.getAllRecipes();

        for (Recipe recipe : recipes) {
            tableModel.addRow(new Object[] {
                    recipe.getId(),
                    recipe.getName(),
                    recipe.getCategory(),
                    recipe.getCookingTime(),
                    recipe.isFavorite() ? "⭐" : ""
            });
        }

        refreshCategoryFilter();
    }
    
    /**
     * Refresh category filter dropdown
     */
    private void refreshCategoryFilter() {
        String selected = (String) categoryFilter.getSelectedItem();
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");

        List<String> categories = recipeDAO.getAllCategories();
        for (String cat : categories) {
            categoryFilter.addItem(cat);
        }

        if (selected != null && !selected.equals("All Categories")) {
            categoryFilter.setSelectedItem(selected);
        }
    }
    
    /**
     * Perform search
     */
    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refreshRecipeList();
            return;
        }

        tableModel.setRowCount(0);
        List<Recipe> recipes = recipeDAO.searchRecipes(keyword);

        for (Recipe recipe : recipes) {
            tableModel.addRow(new Object[] {
                    recipe.getId(),
                    recipe.getName(),
                    recipe.getCategory(),
                    recipe.getCookingTime(),
                    recipe.isFavorite() ? "⭐" : ""
            });
        }
    }
    
    /**
     * Apply category filter
     */
    private void applyFilter() {
        String category = (String) categoryFilter.getSelectedItem();
        
        if (category == null || category.equals("All Categories")) {
            refreshRecipeList();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Recipe> recipes = recipeDAO.filterByCategory(category);
        
        for (Recipe recipe : recipes) {
            tableModel.addRow(new Object[]{
                recipe.getId(),
                recipe.getName(),
                recipe.getCategory(),
                recipe.getCookingTime(),
                recipe.isFavorite() ? "⭐" : ""
            });
        }
    }
    
    /**
     * Show recipe details
     */
    private void showRecipeDetails() {
        int selectedRow = recipeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a recipe to view!",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int recipeId = (int) tableModel.getValueAt(selectedRow, 0);
        currentRecipe = recipeDAO.getRecipeById(recipeId);
        
        if (currentRecipe != null) {
            displayRecipeDetails(currentRecipe);
            showPanel("DETAILS");
        }
    }
    
    /**
     * Display recipe details in the details panel
     */
    private void displayRecipeDetails(Recipe recipe) {
        JPanel detailsPanel = (JPanel) mainPanel.getComponent(1);
        JTextArea detailsArea = (JTextArea) detailsPanel.getClientProperty("detailsArea");
        
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("  ").append(recipe.getName().toUpperCase()).append("\n");
        sb.append("═══════════════════════════════════════════════\n\n");
        
        sb.append("Category: ").append(recipe.getCategory()).append("\n");
        sb.append("Cooking Time: ").append(recipe.getCookingTime()).append(" minutes\n");
        sb.append("Favorite: ").append(recipe.isFavorite() ? "⭐ Yes" : "No").append("\n\n");
        
        sb.append("─────────────────────────────────────────────\n");
        sb.append("INGREDIENTS:\n");
        sb.append("─────────────────────────────────────────────\n");
        
        if (recipe.getIngredients().isEmpty()) {
            sb.append("  No ingredients listed\n");
        } else {
            for (Ingredient ing : recipe.getIngredients()) {
                sb.append("  • ").append(ing.toString()).append("\n");
            }
        }
        
        sb.append("\n─────────────────────────────────────────────\n");
        sb.append("INSTRUCTIONS:\n");
        sb.append("─────────────────────────────────────────────\n");
        sb.append(recipe.getInstructions());
        
        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }
    
    /**
     * Edit selected recipe
     */
    private void editRecipe() {
        int selectedRow = recipeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a recipe to edit!",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int recipeId = (int) tableModel.getValueAt(selectedRow, 0);
        editingRecipe = recipeDAO.getRecipeById(recipeId);
        
        if (editingRecipe != null) {
            showPanel("ADD_EDIT");
            populateAddEditForm(editingRecipe);
        }
    }
    
    /**
     * Edit current recipe from details view
     */
    private void editCurrentRecipe() {
        if (currentRecipe != null) {
            editingRecipe = currentRecipe;
            showPanel("ADD_EDIT");
            populateAddEditForm(editingRecipe);
        }
    }
    
    /**
     * Delete selected recipe
     */
    private void deleteRecipe() {
        int selectedRow = recipeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a recipe to delete!",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int recipeId = (int) tableModel.getValueAt(selectedRow, 0);
        String recipeName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete '" + recipeName + "'?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (recipeDAO.deleteRecipe(recipeId)) {
                JOptionPane.showMessageDialog(this,
                    "Recipe deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                refreshRecipeList();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error deleting recipe!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Toggle favorite status
     */
    private void toggleFavorite() {
        if (currentRecipe != null) {
            currentRecipe.setFavorite(!currentRecipe.isFavorite());
            
            if (recipeDAO.updateRecipe(currentRecipe)) {
                JOptionPane.showMessageDialog(this,
                    currentRecipe.isFavorite() ? 
                        "Added to favorites! ⭐" : "Removed from favorites",
                    "Favorite Updated",
                    JOptionPane.INFORMATION_MESSAGE);
                displayRecipeDetails(currentRecipe);
                refreshRecipeList();
            }
        }
    }
    
    /**
     * Show favorite recipes
     */
    private void showFavorites() {
        tableModel.setRowCount(0);
        List<Recipe> recipes = recipeDAO.getFavoriteRecipes();
        
        if (recipes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No favorite recipes yet!\nMark recipes as favorites to see them here.",
                "No Favorites",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        for (Recipe recipe : recipes) {
            tableModel.addRow(new Object[]{
                recipe.getId(),
                recipe.getName(),
                recipe.getCategory(),
                recipe.getCookingTime(),
                "⭐"
            });
        }
        
        showPanel("LIST");
    }
    
    /**
     * Show random recipe
     */
    private void showRandomRecipe() {
        List<Recipe> recipes = recipeDAO.getAllRecipes();
        
        if (recipes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No recipes available!",
                "Empty Database",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Random random = new Random();
        Recipe randomRecipe = recipes.get(random.nextInt(recipes.size()));
        currentRecipe = randomRecipe;
        
        displayRecipeDetails(randomRecipe);
        showPanel("DETAILS");
    }
    
    /**
     * Show help dialog
     */
    private void showHelp() {
        String helpText = "═══════════════════════════════════════\n" +
            "    RECIPE MANAGEMENT SYSTEM - HELP\n" +
            "═══════════════════════════════════════\n\n" +
            "FEATURES:\n\n" +
            "📋 View Recipes\n" +
            "   - Browse all recipes in a table\n" +
            "   - Search by name, category, or instructions\n" +
            "   - Filter by category\n" +
            "   - Double-click to view details\n\n" +
            "➕ Add Recipe\n" +
            "   - Create new recipes with ingredients\n" +
            "   - Add cooking time and instructions\n" +
            "   - Categorize your recipes\n\n" +
            "✏️ Edit Recipe\n" +
            "   - Modify existing recipes\n" +
            "   - Update ingredients and instructions\n\n" +
            "📅 Meal Planner\n" +
            "   - Plan meals for specific dates\n" +
            "   - View weekly meal plans\n" +
            "   - Generate grocery lists\n\n" +
            "⭐ Favorites\n" +
            "   - Mark recipes as favorites\n" +
            "   - Quick access to favorite recipes\n\n" +
            "🎲 Random Recipe\n" +
            "   - Get a random recipe suggestion\n" +
            "   - Great for meal inspiration!\n\n" +
            "SHORTCUTS:\n" +
            "   - Double-click recipe to view details\n" +
            "   - Enter in search field to search\n\n" +
            "Version 1.0\n" +
            "Developed with Java Swing & SQLite\n" +
            "macOS Compatible";
        
        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setFont(IS_MAC ? 
            new Font("SF Mono", Font.PLAIN, 12) : 
            new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        
        JOptionPane.showMessageDialog(this,
            scrollPane,
            "Help - Recipe Management System",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Switch to a different panel
     */
    private void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    
    /**
     * Create Add/Edit Recipe Panel
     */
    private JPanel createAddEditRecipePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Add New Recipe");
        titleLabel.setFont(IS_MAC ? 
            new Font("SF Pro Display", Font.BOLD, 24) : 
            new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Recipe Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Recipe Name: *"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(30);
        formPanel.add(nameField, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        JTextField categoryField = new JTextField(30);
        formPanel.add(categoryField, gbc);
        
        // Cooking Time
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Cooking Time (min):"), gbc);
        gbc.gridx = 1;
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 999, 5));
        formPanel.add(timeSpinner, gbc);
        
        // Instructions
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Instructions:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        JTextArea instructionsArea = new JTextArea(6, 30);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        if (IS_MAC) {
            instructionsArea.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        }
        JScrollPane instrScroll = new JScrollPane(instructionsArea);
        formPanel.add(instrScroll, gbc);
        
        // Ingredients
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        formPanel.add(new JLabel("Ingredients (one per line):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        JTextArea ingredientsArea = new JTextArea(6, 30);
        ingredientsArea.setLineWrap(true);
        ingredientsArea.setWrapStyleWord(true);
        if (IS_MAC) {
            ingredientsArea.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        }
        JScrollPane ingScroll = new JScrollPane(ingredientsArea);
        formPanel.add(ingScroll, gbc);
        
        // Help text for ingredients
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        JLabel helpLabel = new JLabel("<html><i>Format: quantity - name (e.g., 200g - Flour)</i></html>");
        helpLabel.setFont(IS_MAC ? 
            new Font("SF Pro Text", Font.PLAIN, 11) : 
            new Font("Arial", Font.PLAIN, 11));
        formPanel.add(helpLabel, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton saveBtn = createStyledButton("💾 Save Recipe");
        saveBtn.setFont(IS_MAC ? 
            new Font("SF Pro Text", Font.BOLD, 14) : 
            new Font("Arial", Font.BOLD, 14));
        if (!IS_MAC) {
            saveBtn.setBackground(new Color(46, 204, 113));
            saveBtn.setForeground(Color.WHITE);
        }
        saveBtn.addActionListener(e -> saveRecipe(nameField, categoryField, timeSpinner, 
            instructionsArea, ingredientsArea));
        
        JButton cancelBtn = createStyledButton("Cancel");
        cancelBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Discard changes?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                showPanel("LIST");
            }
        });
        
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // Store references
        panel.putClientProperty("titleLabel", titleLabel);
        panel.putClientProperty("nameField", nameField);
        panel.putClientProperty("categoryField", categoryField);
        panel.putClientProperty("timeSpinner", timeSpinner);
        panel.putClientProperty("instructionsArea", instructionsArea);
        panel.putClientProperty("ingredientsArea", ingredientsArea);
        
        return panel;
    }
    
    /**
     * Clear the add/edit form
     */
    private void clearAddEditForm() {
        JPanel panel = (JPanel) mainPanel.getComponent(2);
        JLabel titleLabel = (JLabel) panel.getClientProperty("titleLabel");
        JTextField nameField = (JTextField) panel.getClientProperty("nameField");
        JTextField categoryField = (JTextField) panel.getClientProperty("categoryField");
        JSpinner timeSpinner = (JSpinner) panel.getClientProperty("timeSpinner");
        JTextArea instructionsArea = (JTextArea) panel.getClientProperty("instructionsArea");
        JTextArea ingredientsArea = (JTextArea) panel.getClientProperty("ingredientsArea");
        
        titleLabel.setText("Add New Recipe");
        nameField.setText("");
        categoryField.setText("");
        timeSpinner.setValue(30);
        instructionsArea.setText("");
        ingredientsArea.setText("");
    }
    
    /**
     * Populate form for editing
     */
    private void populateAddEditForm(Recipe recipe) {
        JPanel panel = (JPanel) mainPanel.getComponent(2);
        JLabel titleLabel = (JLabel) panel.getClientProperty("titleLabel");
        JTextField nameField = (JTextField) panel.getClientProperty("nameField");
        JTextField categoryField = (JTextField) panel.getClientProperty("categoryField");
        JSpinner timeSpinner = (JSpinner) panel.getClientProperty("timeSpinner");
        JTextArea instructionsArea = (JTextArea) panel.getClientProperty("instructionsArea");
        JTextArea ingredientsArea = (JTextArea) panel.getClientProperty("ingredientsArea");
        
        titleLabel.setText("Edit Recipe");
        nameField.setText(recipe.getName());
        categoryField.setText(recipe.getCategory());
        timeSpinner.setValue(recipe.getCookingTime());
        instructionsArea.setText(recipe.getInstructions());
        
        // Populate ingredients
        StringBuilder ingText = new StringBuilder();
        for (Ingredient ing : recipe.getIngredients()) {
            ingText.append(ing.getQuantity()).append(" - ").append(ing.getName()).append("\n");
        }
        ingredientsArea.setText(ingText.toString());
    }
    
    /**
     * Save recipe (add or update)
     */
    private void saveRecipe(JTextField nameField, JTextField categoryField, JSpinner timeSpinner,
                            JTextArea instructionsArea, JTextArea ingredientsArea) {
        // Validate
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Recipe name is required!",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check duplicate name (except when editing same recipe)
        int excludeId = (editingRecipe != null) ? editingRecipe.getId() : -1;
        if (recipeDAO.recipeNameExists(name, excludeId)) {
            JOptionPane.showMessageDialog(this,
                "A recipe with this name already exists!",
                "Duplicate Name",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create or update recipe
        Recipe recipe = (editingRecipe != null) ? editingRecipe : new Recipe();
        recipe.setName(name);
        recipe.setCategory(categoryField.getText().trim());
        recipe.setCookingTime((Integer) timeSpinner.getValue());
        recipe.setInstructions(instructionsArea.getText().trim());
        
        // Parse ingredients
        List<Ingredient> ingredients = new ArrayList<>();
        String[] lines = ingredientsArea.getText().split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                String[] parts = line.split("-", 2);
                if (parts.length == 2) {
                    ingredients.add(new Ingredient(parts[1].trim(), parts[0].trim()));
                } else {
                    ingredients.add(new Ingredient(line, ""));
                }
            }
        }
        recipe.setIngredients(ingredients);
        
        // Save
        boolean success;
        if (editingRecipe != null) {
            success = recipeDAO.updateRecipe(recipe);
        } else {
            success = recipeDAO.insertRecipe(recipe);
        }
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Recipe saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            editingRecipe = null;
            clearAddEditForm();
            refreshRecipeList();
            showPanel("LIST");
        } else {
            JOptionPane.showMessageDialog(this,
                "Error saving recipe!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Create Meal Planner Panel
     */
    private JPanel createMealPlannerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("📅 Meal Planner");
        titleLabel.setFont(IS_MAC ? 
            new Font("SF Pro Display", Font.BOLD, 24) : 
            new Font("Arial", Font.BOLD, 24));
        
        // Top container to hold title and buttons
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.add(titleLabel, BorderLayout.NORTH);
        
        // Top panel with date selection and actions
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JTextField dateField = new JTextField(sdf.format(new Date()), 12);
        
        JButton loadBtn = createStyledButton("Load Plans");
        JButton addBtn = createStyledButton("Add to Plan");
        JButton deleteBtn = createStyledButton("Delete from Plan");
        if (!IS_MAC) {
            deleteBtn.setBackground(new Color(231, 76, 60));
            deleteBtn.setForeground(Color.WHITE);
        }
        JButton groceryBtn = createStyledButton("Generate Grocery List");
        
        topPanel.add(new JLabel("Date (yyyy-MM-dd):"));
        topPanel.add(dateField);
        topPanel.add(loadBtn);
        topPanel.add(addBtn);
        topPanel.add(deleteBtn);
        topPanel.add(groceryBtn);
        
        topContainer.add(topPanel, BorderLayout.CENTER);
        panel.add(topContainer, BorderLayout.NORTH);
        
        // Meal plan table instead of text area for better selection
        String[] columnNames = {"ID", "Meal Type", "Recipe Name", "Cooking Time (min)"};
        DefaultTableModel mealPlanTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable mealPlanTable = new JTable(mealPlanTableModel);
        mealPlanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mealPlanTable.setRowHeight(IS_MAC ? 28 : 25);
        mealPlanTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        mealPlanTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        mealPlanTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        mealPlanTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        if (IS_MAC) {
            mealPlanTable.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
        }
        
        JScrollPane scrollPane = new JScrollPane(mealPlanTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Store references
        panel.putClientProperty("dateField", dateField);
        panel.putClientProperty("mealPlanTable", mealPlanTable);
        panel.putClientProperty("mealPlanTableModel", mealPlanTableModel);
        
        // Load button action
        loadBtn.addActionListener(e -> loadMealPlanToTable(dateField.getText(), 
            mealPlanTableModel, mealPlanTable));
        
        // Add to plan button action
        addBtn.addActionListener(e -> addToMealPlan(dateField.getText(), 
            mealPlanTableModel, mealPlanTable));
        
        // Delete button action
        deleteBtn.addActionListener(e -> deleteMealPlanEntry(mealPlanTable, 
            mealPlanTableModel, dateField.getText()));
        
        // Grocery list button action
        groceryBtn.addActionListener(e -> generateGroceryList(dateField.getText()));
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton backBtn = createStyledButton("← Back");
        backBtn.addActionListener(e -> showPanel("LIST"));
        
        JButton clearAllBtn = createStyledButton("Clear All for Date");
        if (!IS_MAC) {
            clearAllBtn.setBackground(new Color(230, 126, 34));
            clearAllBtn.setForeground(Color.WHITE);
        }
        clearAllBtn.addActionListener(e -> clearAllMealPlansForDate(dateField.getText(), 
            mealPlanTableModel, mealPlanTable));
        
        bottomPanel.add(backBtn);
        bottomPanel.add(clearAllBtn);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Load meal plan for a date into table
     */
    private void loadMealPlanToTable(String date, DefaultTableModel tableModel, JTable table) {
        tableModel.setRowCount(0);
        List<MealPlan> plans = mealPlanDAO.getMealPlanForDate(date);
        
        if (plans.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No meals planned for " + date + "\n\nClick 'Add to Plan' to add recipes!",
                "No Meal Plans",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (MealPlan mp : plans) {
                tableModel.addRow(new Object[]{
                    mp.getId(),
                    mp.getMealType(),
                    mp.getRecipe().getName(),
                    mp.getRecipe().getCookingTime()
                });
            }
        }
    }
    
    /**
     * Add recipe to meal plan
     */
    private void addToMealPlan(String date, DefaultTableModel tableModel, JTable table) {
        // Select recipe
        List<Recipe> recipes = recipeDAO.getAllRecipes();
        if (recipes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No recipes available!\n\nPlease add some recipes first.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String[] recipeNames = recipes.stream()
            .map(Recipe::getName)
            .toArray(String[]::new);
        
        String selectedRecipe = (String) JOptionPane.showInputDialog(this,
            "Select a recipe to add to meal plan:",
            "Add to Meal Plan",
            JOptionPane.QUESTION_MESSAGE,
            null,
            recipeNames,
            recipeNames[0]);
        
        if (selectedRecipe == null) return;
        
        // Select meal type
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        String mealType = (String) JOptionPane.showInputDialog(this,
            "Select meal type:",
            "Meal Type",
            JOptionPane.QUESTION_MESSAGE,
            null,
            mealTypes,
            mealTypes[0]);
        
        if (mealType == null) return;
        
        // Find recipe ID
        int recipeId = recipes.stream()
            .filter(r -> r.getName().equals(selectedRecipe))
            .findFirst()
            .map(Recipe::getId)
            .orElse(-1);
        
        if (recipeId == -1) return;
        
        // Add to plan
        MealPlan mp = new MealPlan(recipeId, date, mealType);
        if (mealPlanDAO.insertMealPlan(mp)) {
            JOptionPane.showMessageDialog(this,
                "Added '" + selectedRecipe + "' to " + mealType + " for " + date + "!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            loadMealPlanToTable(date, tableModel, table);
        } else {
            JOptionPane.showMessageDialog(this,
                "Error adding to meal plan!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Add current recipe to meal plan
     */
    private void addCurrentRecipeToMealPlan() {
        if (currentRecipe == null) return;
        
        // Select date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = JOptionPane.showInputDialog(this,
            "Enter date (yyyy-MM-dd):",
            sdf.format(new Date()));
        
        if (date == null || date.trim().isEmpty()) return;
        
        // Select meal type
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        String mealType = (String) JOptionPane.showInputDialog(this,
            "Select meal type:",
            "Meal Type",
            JOptionPane.QUESTION_MESSAGE,
            null,
            mealTypes,
            mealTypes[0]);
        
        if (mealType == null) return;
        
        // Add to plan
        MealPlan mp = new MealPlan(currentRecipe.getId(), date, mealType);
        if (mealPlanDAO.insertMealPlan(mp)) {
            JOptionPane.showMessageDialog(this,
                "Added '" + currentRecipe.getName() + "' to meal plan!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Delete selected meal plan entry
     */
    private void deleteMealPlanEntry(JTable table, DefaultTableModel tableModel, String date) {
        int selectedRow = table.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a meal plan entry to delete!",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int mealPlanId = (int) tableModel.getValueAt(selectedRow, 0);
        String mealType = (String) tableModel.getValueAt(selectedRow, 1);
        String recipeName = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete '" + recipeName + "' from " + mealType + "?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (mealPlanDAO.deleteMealPlan(mealPlanId)) {
                JOptionPane.showMessageDialog(this,
                    "Meal plan entry deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadMealPlanToTable(date, tableModel, table);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error deleting meal plan entry!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Clear all meal plans for a specific date
     */
    private void clearAllMealPlansForDate(String date, DefaultTableModel tableModel, JTable table) {
        List<MealPlan> plans = mealPlanDAO.getMealPlanForDate(date);
        
        if (plans.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No meal plans found for " + date,
                "Nothing to Clear",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete ALL " + plans.size() + " meal plan(s) for " + date + "?",
            "Clear All Meal Plans",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int deletedCount = 0;
            for (MealPlan mp : plans) {
                if (mealPlanDAO.deleteMealPlan(mp.getId())) {
                    deletedCount++;
                }
            }
            
            if (deletedCount > 0) {
                JOptionPane.showMessageDialog(this,
                    "Cleared " + deletedCount + " meal plan(s) for " + date,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadMealPlanToTable(date, tableModel, table);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error clearing meal plans!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Generate grocery list
     */
    private void generateGroceryList(String date) {
        List<MealPlan> plans = mealPlanDAO.getMealPlanForDate(date);
        
        if (plans.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No meals planned for this date!",
                "Empty Plan",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Collect all ingredients
        Map<String, String> groceryMap = new LinkedHashMap<>();
        for (MealPlan mp : plans) {
            for (Ingredient ing : mp.getRecipe().getIngredients()) {
                String key = ing.getName().toLowerCase();
                if (!groceryMap.containsKey(key)) {
                    groceryMap.put(key, ing.toString());
                }
            }
        }
        
        // Build grocery list
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("   GROCERY LIST FOR ").append(date).append("\n");
        sb.append("═══════════════════════════════════════\n\n");
        
        int count = 1;
        for (String item : groceryMap.values()) {
            sb.append(count++).append(". ").append(item).append("\n");
        }
        
        // Display in dialog
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(IS_MAC ? 
            new Font("SF Mono", Font.PLAIN, 12) : 
            new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        
        int option = JOptionPane.showConfirmDialog(this,
            scrollPane,
            "Grocery List",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            // Ask if they want to save
            int save = JOptionPane.showConfirmDialog(this,
                "Save grocery list to file?",
                "Save",
                JOptionPane.YES_NO_OPTION);
            
            if (save == JOptionPane.YES_OPTION) {
                saveGroceryList(sb.toString(), date);
            }
        }
    }
    
    /**
     * Save grocery list to file
     */
    private void saveGroceryList(String content, String date) {
        try {
            String filename = "grocery_list_" + date + ".txt";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(content);
            writer.close();
            
            JOptionPane.showMessageDialog(this,
                "Grocery list saved to: " + filename,
                "Saved",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error saving file: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Main method to run the application
     */
    public static void main(String[] args) {
        // macOS-specific settings - MUST be set before any GUI creation
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            // Use macOS menu bar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // Set application name in menu bar
            System.setProperty("apple.awt.application.name", "Recipe Manager");
            // Enable full-screen mode
            System.setProperty("apple.awt.application.appearance", "system");
        }
        
        // Set look and feel
        try {
            // For macOS, use the native look and feel
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                // For other systems, try to use Nimbus for a modern look
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fall back to default if setting look and feel fails
        }
        
        // Run on EDT
        SwingUtilities.invokeLater(() -> new RecipeManagementApp());
    }
}