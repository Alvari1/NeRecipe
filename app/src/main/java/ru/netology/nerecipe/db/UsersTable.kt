package ru.netology.nerecipe.db

object UsersTable {
    const val TABLE_NAME = "users"

    val TABLE_DDL = """
        CREATE TABLE $TABLE_NAME (
            ${Column.ID.columnName} TEXT PRIMARY KEY,
            ${Column.FirstName.columnName} TEXT NOT NULL,
            ${Column.LastName.columnName} TEXT,
            ${Column.CreatedAt.columnName} INTEGER NOT NULL DEFAULT 0,
            ${Column.Password.columnName} TEXT,
            ${Column.Email.columnName} TEXT,
            ${Column.Phone.columnName} TEXT
        )
    """.trimIndent()

    val ALL_COLUMNS_NAMES = Column.values().map {
        it.columnName
    }.toTypedArray()

    enum class Column(val columnName: String) {
        ID("id"),
        FirstName("firstName"),
        LastName("lastName"),
        CreatedAt("createdAt"),
        Password("password"),
        Email("email"),
        Phone("phone"),
    }
}