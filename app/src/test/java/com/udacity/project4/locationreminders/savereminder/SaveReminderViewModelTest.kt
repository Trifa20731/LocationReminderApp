package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
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

    private fun getFakeReminderDateItem() = ReminderDataItem(
        "test1 title",
        "test1 des",
        "test1 locate",
        60.08,
        35.99
    )

    @Test
    fun reminders_saveReminderWithEmptyTitle_returnFalse() = runBlockingTest {

        // Given
        val fakeReminderDataItem = getFakeReminderDateItem()

        // When
        fakeReminderDataItem.title = ""

        // Then
        assertThat(saveReminderViewModel.validateEnteredData(fakeReminderDataItem), `is`(false))

    }

    @Test
    fun reminders_saveReminderWithEmptyTitle_showErrorEnterTitleSnakeBar() = runBlockingTest {

        // Given
        val fakeReminderDataItem = getFakeReminderDateItem()

        // When
        fakeReminderDataItem.title = ""
        saveReminderViewModel.validateAndSaveReminder(fakeReminderDataItem)

        // Then
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun reminders_saveReminderWithEmptyLocation_showErrorEnterLocationSnakeBar() = runBlockingTest {

        // Given
        val fakeReminderDataItem = getFakeReminderDateItem()

        // When
        fakeReminderDataItem.location = ""
        saveReminderViewModel.validateAndSaveReminder(fakeReminderDataItem)

        // Then
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @Test
    fun reminder_saveReminder_showLoading() = runBlockingTest {

        // Given
        val fakeReminderDataItem = getFakeReminderDateItem()

        // When
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(fakeReminderDataItem)

        // Then
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

}