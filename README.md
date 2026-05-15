# ISE Recipe Management System

## Description
ISE Recipe Management System is a desktop application for managing recipes, tracking favorites, and planning meals.
It provides a Java Swing interface backed by SQLite for persistent local data storage.

## Features
- View and browse recipes in a table-based UI
- Search recipes by name, category, or instructions
- Filter recipes by category
- Add, edit, and manage recipe details and ingredients
- Mark and view favorite recipes
- Generate random recipe suggestions
- Create meal plans by date and meal type
- Generate and export grocery lists to text files

## Installation
Standard setup.

### Prerequisites
- Java JDK 8 or higher
- SQLite JDBC driver (included): `lib/sqlite-jdbc-3.51.1.0.jar`

## How To Run

### Option 1: Run from an IDE
1. Open the project in your Java IDE.
2. Add `lib/sqlite-jdbc-3.51.1.0.jar` to your project classpath.
3. Run `src.ui.RecipeManagementApp`.

### Option 2: Run from command line
From the project root:

```bash
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -cp "lib/sqlite-jdbc-3.51.1.0.jar" @sources.txt
java -cp ".:lib/sqlite-jdbc-3.51.1.0.jar" src.ui.RecipeManagementApp
```

Note: On Windows, replace `:` in the classpath with `;`.

## Usage
1. Launch the application.
2. Use the sidebar to navigate to recipe list, add recipe, meal planner, favorites, or random recipe.
3. Double-click a recipe to view full details.
4. Manage meal plans and export grocery lists as needed.

## Project Structure
```text
ise_recipe_mac/
├── doc/
│   └── README.md
├── lib/
│   └── sqlite-jdbc-3.51.1.0.jar
├── src/
│   ├── dao/
│   │   ├── MealPlanDAO.java
│   │   └── RecipeDAO.java
│   ├── database/
│   │   └── DBConnection.java
│   ├── models/
│   │   ├── Ingredient.java
│   │   ├── MealPlan.java
│   │   └── Recipe.java
│   └── ui/
│       └── RecipeManagementApp.java
├── grocery_list_2026-01-28.txt
└── recipes.sqbpro
```

## Technologies Used
- Java (Swing for desktop UI)
- SQLite (local database)
- JDBC (database connectivity)

## Contributing
Contributions are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Commit your changes.
4. Open a pull request.

## Author
Your Name

## License
This project is licensed under the MIT License.
