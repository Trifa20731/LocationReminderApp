package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

/**
 * Please comment the code in View Model and Fragment before doing test:
 *     ReminderListFragment: Line 73.
 *     ReminderListViewModel: Line 23 to Line 29.
 * */

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test.
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake data source to be injected into the view model.
    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var applicationContent: Application

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        // Init fake data source
        fakeDataSource = FakeDataSource(getFakeReminderDataItemList())
        // Add task into fake data source
        applicationContent = ApplicationProvider.getApplicationContext()
        // Init view model
        remindersListViewModel =
            RemindersListViewModel(
                applicationContent,
                fakeDataSource
        )
        FirebaseApp.initializeApp(applicationContent)
    }

    @Test
    fun reminderList_loadReminderList_returnError() {

        // Give
        fakeDataSource.setReturnError(true)

        // When
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Task Exception."))


    }

    @Test
    fun reminderList_loadReminderList_showNoData() = runBlockingTest() {

        // Give
        fakeDataSource.deleteAllReminders()

        // When
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))

    }

    @Test
    fun reminderList_loadReminderList_showData() = runBlockingTest() {

        // Give

        // When
        remindersListViewModel.loadReminders()

        // Then
        val initData: ReminderDTO = getFakeReminderDataItemList()!![1]
        val result: ReminderDataItem = remindersListViewModel.remindersList.getOrAwaitValue()!![1]
        assertThat(result.description, `is`(initData.description))
        assertThat(result.latitude, `is`(initData.latitude))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false) )

    }

    @Test
    fun reminderList_loadReminderList_showLoading() = runBlockingTest() {
        // Give

        // When
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }


    private fun getFakeReminderDataItemList(): MutableList<ReminderDTO>? {
        val reminderDataItem1 = ReminderDTO( "test1 title", "test1 des", "test1 locate", 60.08, 35.99 )
        val reminderDataItem2 = ReminderDTO( "test2 title", "test2 des", "test2 locate", 45.27, 13.29 )
        val reminderDataItem3 = ReminderDTO( "test3 title", "test3 des", "test3 locate", 39.09, -94.54 )
        return mutableListOf(reminderDataItem1, reminderDataItem2, reminderDataItem3)
    }
}