package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {

        // Give
        val fakeReminderDTO: ReminderDTO = getFakeReminderDataItemList()[0]
        remindersDatabase.reminderDao().saveReminder(fakeReminderDTO)

        // When
        val loaded = remindersDatabase.reminderDao().getReminderById(fakeReminderDTO.id)

        // Then
        assertThat<ReminderDTO>( loaded as ReminderDTO, notNullValue() )
        assertThat(loaded.id, `is`(fakeReminderDTO.id))
        assertThat(loaded.longitude, `is`(fakeReminderDTO.longitude))
        assertThat(loaded.latitude, `is`(fakeReminderDTO.latitude))
        assertThat(loaded.description, `is`(fakeReminderDTO.description))
        assertThat(loaded.title, `is`(fakeReminderDTO.title))
        assertThat(loaded.location, `is`(fakeReminderDTO.location))

    }

    @Test
    fun saveReminderAndGetReminders() = runBlockingTest {

        // Give
        val fakeReminderDataItemList = getFakeReminderDataItemList()
        fakeReminderDataItemList.forEach {
            remindersDatabase.reminderDao().saveReminder(it)
        }

        // When
        val loaded = remindersDatabase.reminderDao().getReminders()[1]

        // Then
        assertThat<ReminderDTO>( loaded as ReminderDTO, notNullValue() )
        assertThat(loaded.id, `is`(fakeReminderDataItemList[1].id))
        assertThat(loaded.longitude, `is`(fakeReminderDataItemList[1].longitude))
        assertThat(loaded.latitude, `is`(fakeReminderDataItemList[1].latitude))
        assertThat(loaded.description, `is`(fakeReminderDataItemList[1].description))
        assertThat(loaded.title, `is`(fakeReminderDataItemList[1].title))
        assertThat(loaded.location, `is`(fakeReminderDataItemList[1].location))

    }

    @Test
    fun deleteAllRemindersAndGetNullValue() = runBlockingTest {

        // Give
        val fakeReminderDTO: ReminderDTO = getFakeReminderDataItemList()[0]
        remindersDatabase.reminderDao().saveReminder(fakeReminderDTO)
        val initLoaded = remindersDatabase.reminderDao().getReminderById(fakeReminderDTO.id)
        assertThat<ReminderDTO>( initLoaded as ReminderDTO, notNullValue() )

        // When
        remindersDatabase.reminderDao().deleteAllReminders()
        val currentLoaded = remindersDatabase.reminderDao().getReminderById(fakeReminderDTO.id)

        // Then
        assertThat<ReminderDTO>( currentLoaded, nullValue() )

    }

    @Test
    fun deleteAllRemindersAndGetEmptyList() = runBlockingTest {

        // Give
        val fakeReminderDataItemList = getFakeReminderDataItemList()
        fakeReminderDataItemList.forEach {
            remindersDatabase.reminderDao().saveReminder(it)
        }

        // When
        remindersDatabase.reminderDao().deleteAllReminders()
        val loadedList = remindersDatabase.reminderDao().getReminders()

        // Then
        assertThat( loadedList,  `is`(emptyList()))

    }

    private fun getFakeReminderDataItemList(): MutableList<ReminderDTO> {
        val reminderDataItem1 = ReminderDTO( "test1 title", "test1 des", "test1 locate", 60.08, 35.99 )
        val reminderDataItem2 = ReminderDTO( "test2 title", "test2 des", "test2 locate", 45.27, 13.29 )
        val reminderDataItem3 = ReminderDTO( "test3 title", "test3 des", "test3 locate", 39.09, -94.54 )
        return mutableListOf(reminderDataItem1, reminderDataItem2, reminderDataItem3)
    }


}