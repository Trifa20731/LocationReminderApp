package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    // Parameter for error handling test.
    private var shouldReturnError: Boolean = false

    // Set the return error value to test the error condition.
    fun setReturnError(shouldReturnError: Boolean) {
        this.shouldReturnError = shouldReturnError
    }

    // Get Whole Reminder List.
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Task Exception.")
        }
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Could not find reminders.")
    }

    // Add New Reminder List.
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    // Get Specific Reminder, use for each to find the matcher.
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Task Exception.")
        }
        reminders?.forEach { if (it.id == id) { return Result.Success(it) } }
        return Result.Error("Could not find any match reminder.")
    }

    // Clear whole list.
    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}