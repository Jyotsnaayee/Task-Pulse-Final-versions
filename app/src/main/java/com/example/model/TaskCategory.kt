package com.example.model

enum class TaskCategory(
    val title: String,
    val subtitle: String
) {
    IMPORTANT(
        title = "Important Work",
        subtitle = "High priority & critical focus"
    ),
    LESS_IMPORTANT(
        title = "Less Important Work",
        subtitle = "Medium priority & secondary tasks"
    ),
    DAILY(
        title = "Daily Work",
        subtitle = "Routine & daily habits"
    );

    companion object {
        fun fromString(name: String?): TaskCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: IMPORTANT
        }
    }
}
