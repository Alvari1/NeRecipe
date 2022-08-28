package ru.netology.nerecipe.db

object IngredientsTable {
    const val TABLE_NAME = "ingredients"

    val TABLE_DDL = """
        CREATE TABLE $TABLE_NAME (
            ${Column.ID.columnName} TEXT PRIMARY KEY,
            ${Column.RecipeId.columnName} TEXT NOT NULL,
            ${Column.Description.columnName} TEXT NOT NULL,
            ${Column.Position.columnName} INTEGER NOT NULL

        )
    """.trimIndent()

    val ALL_COLUMNS_NAMES = Column.values().map {
        it.columnName
    }.toTypedArray()

    enum class Column(val columnName: String) {
        ID("id"),
        RecipeId("recipeId"),
        Description("description"),
        Position("position"),
    }
}