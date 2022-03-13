package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var remindersDatabase: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersLocalRepository = RemindersLocalRepository(
            remindersDatabase.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        remindersDatabase.close()
    }

    @Test
    fun saveReminder_getReminderById() = runBlocking {
        
        // Given - save a reminder in the database.
        val fakeReminderDTO = getFakeReminderDTOList()[0]
        remindersLocalRepository.saveReminder(fakeReminderDTO)

        // When - reminder retrieved by ID.
        val result = remindersLocalRepository.getReminder(fakeReminderDTO.id)

        // Then - same task is return.
        assertThat(result as Result.Success, notNullValue())
        assertThat(result.data.id, `is`(fakeReminderDTO.id))
        assertThat(result.data.location, `is`(fakeReminderDTO.location))
        assertThat(result.data.title, `is`(fakeReminderDTO.title))
        assertThat(result.data.description, `is`(fakeReminderDTO.description))
        assertThat(result.data.latitude, `is`(fakeReminderDTO.latitude))
        assertThat(result.data.longitude, `is`(fakeReminderDTO.longitude))

    }
    
    @Test
    fun saveReminder_getReminders() = runBlocking { 
        
        // Given - save reminders in the database.
        val fakeReminderDTOList = getFakeReminderDTOList()
        fakeReminderDTOList.forEach {
            remindersLocalRepository.saveReminder(it)
        }

        // When - get all reminders in the database.
        val resultList = remindersLocalRepository.getReminders()

        // Then - same tasks are return.
        assertThat(resultList as Result.Success, notNullValue())
        assertThat(resultList.data.size, `is`(fakeReminderDTOList.size))
        for ( i in 0 until resultList.data.size ) {
            assertThat(resultList.data[i].id, `is`(fakeReminderDTOList[i].id))
            assertThat(resultList.data[i].location, `is`(fakeReminderDTOList[i].location))
            assertThat(resultList.data[i].title, `is`(fakeReminderDTOList[i].title))
            assertThat(resultList.data[i].description, `is`(fakeReminderDTOList[i].description))
            assertThat(resultList.data[i].latitude, `is`(fakeReminderDTOList[i].latitude))
            assertThat(resultList.data[i].longitude, `is`(fakeReminderDTOList[i].longitude))
        }

    }

    @Test
    fun saveReminder_deleteAllReminders_returnErrorMessage() = runBlocking {

        // Given - save reminder in the database.
        val fakeReminderDTO = getFakeReminderDTOList()[0]
        remindersLocalRepository.saveReminder(fakeReminderDTO)

        val initResult = remindersLocalRepository.getReminder(fakeReminderDTO.id)
        assertThat(initResult as Result.Success, notNullValue())
        assertThat(initResult.data.id, `is`(fakeReminderDTO.id))

        // When - delete all reminder in the database.
        remindersLocalRepository.deleteAllReminders()

        // Then - return error reminder
        val currentResult = remindersLocalRepository.getReminder(fakeReminderDTO.id) as Result.Error
        assertThat(currentResult.message, `is`("Reminder not found!"))

    }

    @Test
    fun saveReminders_deleteAllReminders_returnEmptyList() = runBlocking {

        // Given - save reminder in the database.
        val fakeReminderDTOList = getFakeReminderDTOList()
        fakeReminderDTOList.forEach {
            remindersLocalRepository.saveReminder(it)
        }

        val initResultList = remindersLocalRepository.getReminders()
        assertThat(initResultList as Result.Success, notNullValue())
        assertThat(initResultList.data.size, `is`(fakeReminderDTOList.size))

        // When - delete all reminder in the database.
        remindersLocalRepository.deleteAllReminders()

        // Then - return empty list
        val currentResult = remindersLocalRepository.getReminders() as Result.Success
        assertThat(currentResult.data, `is`(emptyList()))

    }

    private fun getFakeReminderDTOList(): MutableList<ReminderDTO> {
        val reminderDataItem1 = ReminderDTO( "test1 title", "test1 des", "test1 locate", 60.08, 35.99 )
        val reminderDataItem2 = ReminderDTO( "test2 title", "test2 des", "test2 locate", 45.27, 13.29 )
        val reminderDataItem3 = ReminderDTO( "test3 title", "test3 des", "test3 locate", 39.09, -94.54 )
        return mutableListOf(reminderDataItem1, reminderDataItem2, reminderDataItem3)
    }


}