package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.launch

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNot.not
import org.junit.After

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import java.util.EnumSet.allOf

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
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

    @Before
    fun registerIdlingResources() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    // Launch in the list fragment, click the add button and add new location with empty title, show the snake bar.
    @Test
    fun addNewReminderWithEmptyTitle_showSnakeBar() = runBlocking {
        // Start Up a Reminder Screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click the FAB, Move to Save Reminder Fragment.
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Select the location
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.save_poi_btn)).perform(click())

        // Check the location, Enter the information in the save reminder fragment.
        onView(withText(Constants.DEFAULT_LOCATION_NAME)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).perform(replaceText(Constants.DEFAULT_DESCRIPTION))
        onView(withId(R.id.saveReminder)).perform(click())

        // Show the Snake Bar with empty title
        onView(withText(R.string.err_enter_title)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    // Launch in the list fragment, click the add button and add the new location, then showing save successful toast.
    @Test
    fun addNewReminder_showTheSuccessfulToast() = runBlocking {
        // Start Up a Reminder Screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click the FAB, Move to Save Reminder Fragment.
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Select the location
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.save_poi_btn)).perform(click())

        // Check the location, Enter the information in the save reminder fragment.
        onView(withText(Constants.DEFAULT_LOCATION_NAME)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).perform(replaceText(Constants.DEFAULT_TITLE))
        onView(withId(R.id.reminderDescription)).perform(replaceText(Constants.DEFAULT_DESCRIPTION))
        onView(withId(R.id.saveReminder)).perform(click())

        // Show the Toast.
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(getActivity(activityScenario)?.window?.decorView)))).check(matches(isDisplayed()))

        activityScenario.close()
    }


    // Launch in the list fragment, click the add button add the new location, then show in the list fragment.
    @Test
    fun addNewReminder_showInReminderList() = runBlocking {

        // Save Data into Repo
        val fakeReminderDTOList = getFakeReminderDTOList()
        val job = launch {
            fakeReminderDTOList.forEach {
                repository.saveReminder(it)
            }
        }

        // Start Up a Reminder Screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)


        // Click the FAB, Move to Save Reminder Fragment.
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Select the location
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.save_poi_btn)).perform(click())

        // Check the location, Enter the information in the save reminder fragment.
        onView(withText(Constants.DEFAULT_LOCATION_NAME)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).perform(replaceText(Constants.DEFAULT_TITLE))
        onView(withId(R.id.reminderDescription)).perform(replaceText(Constants.DEFAULT_DESCRIPTION))
        onView(withId(R.id.saveReminder)).perform(click())

        // Make sure the activity is closed before resetting the db.
        onView(withText(Constants.DEFAULT_TITLE)).check(matches(isDisplayed()))
        onView(withText(Constants.DEFAULT_DESCRIPTION)).check(matches(isDisplayed()))
        onView(withText(Constants.DEFAULT_LOCATION_NAME)).check(matches(isDisplayed()))

        activityScenario.close()

        job.cancel()

    }

    private fun getFakeReminderDTOList(): MutableList<ReminderDTO> {
        val reminderDataItem1 = ReminderDTO( "test1 title", "test1 des", "test1 locate", 60.08, 35.99 )
        val reminderDataItem2 = ReminderDTO( "test2 title", "test2 des", "test2 locate", 45.27, 13.29 )
        val reminderDataItem3 = ReminderDTO( "test3 title", "test3 des", "test3 locate", 39.09, -94.54 )
        return mutableListOf(reminderDataItem1, reminderDataItem2, reminderDataItem3)
    }

    // get activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

}
