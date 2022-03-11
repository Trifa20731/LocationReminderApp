package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
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
        fakeDataSource = FakeDataSource()
        // Add task into fake data source
        applicationContent = ApplicationProvider.getApplicationContext()
        // Init view model
        remindersListViewModel =
            RemindersListViewModel(
                applicationContent,
                fakeDataSource
        )
    }

    private fun getFakeReminderDataItemList(): List<ReminderDataItem> {
        val reminderDataItem1 = ReminderDataItem( "test1 title", "test1 des", "test1 locate", 60.08, 35.99 )
        val reminderDataItem2 = ReminderDataItem( "test2 title", "test2 des", "test1 locate", 45.27, 13.29 )
        val reminderDataItem3 = ReminderDataItem( "test3 title", "test3 des", "test1 locate", 39.09, -94.54 )
        return listOf(reminderDataItem1, reminderDataItem2, reminderDataItem3)
    }
}