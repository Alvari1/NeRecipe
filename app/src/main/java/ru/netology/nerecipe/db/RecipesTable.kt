package ru.netology.nerecipe.db

object RecipesTable {
    const val TABLE_NAME = "recipes"

    val TABLE_DDL = """
        CREATE TABLE $TABLE_NAME (
            ${Column.ID.columnName} TEXT PRIMARY KEY,
            ${Column.Author.columnName} TEXT NOT NULL,
            ${Column.Title.columnName} TEXT NOT NULL,
            ${Column.Description.columnName} TEXT,
            ${Column.Published.columnName} INTEGER NOT NULL DEFAULT 0,
            ${Column.LikedByMe.columnName} BOOLEAN NOT NULL DEFAULT 0,
            ${Column.Likes.columnName} INTEGER NOT NULL DEFAULT 0,
            ${Column.Shares.columnName} INTEGER NOT NULL DEFAULT 0,
            ${Column.Views.columnName} INTEGER NOT NULL DEFAULT 0,
            ${Column.Cuisine.columnName} INTEGER NOT NULL DEFAULT 0,
            ${Column.Image.columnName} INTEGER NOT NULL DEFAULT 0
        )
    """.trimIndent()

    val ALL_COLUMNS_NAMES = Column.values().map {
        it.columnName
    }.toTypedArray()

    enum class Column(val columnName: String) {
        ID("id"),
        Author("author"),
        Title("title"),
        Description("description"),
        Published("published"),
        Likes("likes"),
        LikedByMe("likedByMe"),
        Shares("shares"),
        Views("views"),
        Cuisine("cuisine"),
        Image("images"),
    }
}