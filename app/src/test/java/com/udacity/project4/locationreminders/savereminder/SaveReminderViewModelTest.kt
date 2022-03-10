package com.udacity.project4.locationreminders.savereminder

import android.app.Application

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    //TODO: provide testing to the SaveReminderView and its live data objects

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Subject under test.
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Use a fake data source to be injected into view model.
    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var applicationContext: Application

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var activityScenarioRule = activityScenarioRule<RemindersActivity>()

    @Before
    fun setupSaveReminderViewModel() {
        stopKoin()
        // Init Fake Data Source.
        fakeDataSource = FakeDataSource()
        // Init Application
        applicationContext = ApplicationProvider.getApplicationContext()
        // Init Save Reminder View Model.
        saveReminderViewModel =
            SaveReminderViewModel(
                applicationContext,
                fakeDataSource
            )
    }

    @Test
    fun saveReminder_showLoading() = runBlockingTest {

        // Given
        val fakeReminderDataItem = ReminderDataItem(
            "test1 title",
            "test1 des",
            "test1 locate",
            60.08,
            35.99
        )

        // When
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(fakeReminderDataItem)

        // Then
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun reminders_saveReminder_saveSuccessful(){
        // Given
        val fakeReminderDataItem = ReminderDataItem(
            "test1 title",
            "test1 des",
            "test1 locate",
            60.08,
            35.99
        )

        // When
        saveReminderViewModel.saveReminder(fakeReminderDataItem)

        // Then
        assertThat(saveReminderViewModel.reminderTitle.value, `is`("test1 title"))
        assertThat(saveReminderViewModel.reminderDescription.value, `is`("test1 des"))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, `is`("test1 locate"))
        assertThat(saveReminderViewModel.latitude.value, `is`(60.08))
        assertThat(saveReminderViewModel.longitude.value, `is`(35.99))
    }

}