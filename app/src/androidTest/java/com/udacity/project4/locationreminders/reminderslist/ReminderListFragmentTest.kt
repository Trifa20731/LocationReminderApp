package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest

import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }



    // Test the navigation of the fragments.
    @Test
    fun clickAddReminderFAB_navigateToSaveReminderFragment() {

        // Given - Launch Reminder List Fragment.
        val scenario = launchFragmentInContainer<ReminderListFragment> ( Bundle(), R.style.AppTheme )
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // When - Click the FAB Button.
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Then - Navigate to the save reminder fragment.
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())

    }


    // Test the displayed data on the UI.
    @Test
    fun addReminderToList_displayInFragment() = runBlockingTest {

        // Given
        val fakeReminderDTOList = getFakeReminderDTOList()
        val job = launch {
            fakeReminderDTOList.forEach {
                repository.saveReminder(it)
            }
        }

        // When
        launchFragmentInContainer<ReminderListFragment> ( Bundle(), R.style.AppTheme )

        // Then
        fakeReminderDTOList.forEach {
            onView(withText(it.title)).check(matches(isDisplayed()))
            onView(withText(it.description)).check(matches(isDisplayed()))
            onView(withText(it.location)).check(matches(isDisplayed()))
        }
        job.cancel()

    }


    // TODO: add testing for the error messages.
    @Test
    fun showNoDataMessage() = runBlockingTest {

        // Give

        // When
        launchFragmentInContainer<ReminderListFragment> ( Bundle(), R.style.AppTheme )

        // Then
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }


    private fun getFakeReminderDTOList(): MutableList<ReminderDTO> {
        val reminderDataItem1 = ReminderDTO( "test1 title", "test1 des", "test1 locate", 60.08, 35.99 )
        val reminderDataItem2 = ReminderDTO( "test2 title", "test2 des", "test2 locate", 45.27, 13.29 )
        val reminderDataItem3 = ReminderDTO( "test3 title", "test3 des", "test3 locate", 39.09, -94.54 )
        return mutableListOf(reminderDataItem1, reminderDataItem2, reminderDataItem3)
    }
}