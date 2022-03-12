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

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

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
    fun reminderList_loadReminderList_showData() {

        // Give
        val reminderDataItem4 = ReminderDTO( "test4 title", "test4 des", "test4 locate", 39.09, -94.54 )


        // When

        // Then


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

    private fun getFakeReminderDataItemList(): MutableList<ReminderDTO>? {
        val reminderDataItem1 = ReminderDTO( "test1 title", "test1 des", "test1 locate", 60.08, 35.99 )
        val reminderDataItem2 = ReminderDTO( "test2 title", "test2 des", "test1 locate", 45.27, 13.29 )
        val reminderDataItem3 = ReminderDTO( "test3 title", "test3 des", "test1 locate", 39.09, -94.54 )
        return mutableListOf(reminderDataItem1, reminderDataItem2, reminderDataItem3)
    }
}