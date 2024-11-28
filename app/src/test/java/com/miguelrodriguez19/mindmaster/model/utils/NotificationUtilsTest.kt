package com.miguelrodriguez19.mindmaster.model.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.miguelrodriguez19.mindmaster.model.structures.abstractClasses.AbstractActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationUtilsTest {

    private val dateFormatter = DateTimeFormatter.ofPattern(DateTimeUtils.DEFAULT_DATE_FORMAT)

    private lateinit var appContext: Context
    private lateinit var absActivity: AbstractActivity

    private lateinit var datetime: LocalDateTime
    private lateinit var futureDatetime: LocalDateTime

    @BeforeEach
    fun setUp() {
        mockkObject(NotificationUtils)

        appContext = mockk(relaxed = true)
        absActivity = mockk(relaxed = true)

        datetime = LocalDateTime.now()
        futureDatetime = datetime.plusMinutes(5)

        mockkObject(DateTimeUtils)
        every { DateTimeUtils.getCurrentDate() } returns datetime.format(dateFormatter)

        mockkObject(Preferences)
        every { Preferences.getUserPreferredNotificationHour() } returns futureDatetime.hour
        every { Preferences.getUserPreferredNotificationMinute() } returns futureDatetime.minute
    }

    @AfterEach
    fun tearDown() {
        unmockkAll() // Limpiar los mocks después de cada test
    }

    @Test
    fun `test createActivityNotification with valid date`() {
        // given
        mockkObject(AbstractActivity)
        every {
            AbstractActivity.getFormattedDateOf(any())
        } returns datetime.plusDays(1).format(dateFormatter)

        every { DateTimeUtils.compareDates(any(), any()) } returns 1

        // Intercept Intent and its methods
        mockkConstructor(Intent::class)
        every {
            anyConstructed<Intent>().putExtra(any<String>(), any<String>())
        } returns mockk(relaxed = true)

        mockkStatic(PendingIntent::class)
        every {
            PendingIntent.getBroadcast(any<Context>(), any<Int>(), any<Intent>(), any<Int>())
        } returns mockk(relaxed = true)

        every {
            appContext.getSystemService(any<String>())
        } throws RuntimeException("Stop Test")

        // assert
        assertThrows<RuntimeException>("Stop Test") {
            NotificationUtils.createActivityNotification(appContext, absActivity)
        }

        // verify
        verify(exactly = 1)  { AbstractActivity.getFormattedDateOf(any()) }
    }

}
