package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.TaskCategory
import com.example.model.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TaskPulse", appName)
  }

  @Test
  fun `task model format reminder time`() {
    val task = TaskItem(
      title = "Focus session",
      category = TaskCategory.IMPORTANT,
      reminderHour = 14,
      reminderMinute = 30,
      reminderEnabled = true
    )
    assertEquals("2:30 PM", task.formatReminderTime())

    val morningTask = TaskItem(
      title = "Morning run",
      category = TaskCategory.DAILY,
      reminderHour = 7,
      reminderMinute = 5,
      reminderEnabled = true
    )
    assertEquals("7:05 AM", morningTask.formatReminderTime())
  }
}
