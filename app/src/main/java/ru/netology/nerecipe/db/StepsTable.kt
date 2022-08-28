package ru.netology.nerecipe.db

object StepsTable {
    const val TABLE_NAME = "steps"

    val TABLE_DDL = """
        CREATE TABLE $TABLE_NAME (
            ${Column.ID.columnName} TEXT PRIMARY KEY,
            ${Column.RecipeId.columnName} TEXT NOT NULL,
            ${Column.Position.columnName} INTEGER NOT NULL,
            ${Column.Description.columnName} TEXT NOT NULL,
            ${Column.Image.columnName} TEXT NOT NULL
        )
    """.trimIndent()

    val ALL_COLUMNS_NAMES = Column.values().map {
        it.columnName
    }.toTypedArray()

    enum class Column(val columnName: String) {
        ID("id"),
        RecipeId("recipeId"),
        Position("position"),
        Description("description"),
        Image("image"),
    }
}