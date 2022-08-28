package ru.netology.nerecipe.db

object CuisinesTable {
    const val TABLE_NAME = "cuisines"

    val TABLE_DDL = """
        CREATE TABLE $TABLE_NAME (
            ${Column.ID.columnName} INTEGER PRIMARY KEY,
            ${Column.ResName.columnName} TEXT NOT NULL,
            ${Column.IsSelected.columnName} BOOLEAN NOT NULL DEFAULT 1
        )
    """.trimIndent()

    val ALL_COLUMNS_NAMES = Column.values().map {
        it.columnName
    }.toTypedArray()

    enum class Column(val columnName: String) {
        ID("id"),
        ResName("resName"),
        IsSelected("isSelected"),
    }
}
