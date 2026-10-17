package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Appointment
import com.example.data.DoctorProfile
import com.example.voice.VoiceCommand
import com.example.voice.VoiceCommandParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DocCare", appName)
  }

  @Test
  fun `voice command parser correctly detects next appointment command`() {
    val command = VoiceCommandParser.parse("What is my next appointment?")
    assertEquals(VoiceCommand.ReadNextAppointment, command)
  }

  @Test
  fun `voice command parser correctly detects today appointments command`() {
    val command = VoiceCommandParser.parse("What appointments do I have today?")
    assertEquals(VoiceCommand.ReadTodayAppointments, command)
  }

  @Test
  fun `voice command parser correctly detects font size adjustment`() {
    val bigger = VoiceCommandParser.parse("Please make text bigger")
    assertEquals(VoiceCommand.MakeTextBigger, bigger)

    val smaller = VoiceCommandParser.parse("Make text smaller")
    assertEquals(VoiceCommand.MakeTextSmaller, smaller)
  }

  @Test
  fun `voice command parser correctly detects call doctor command`() {
    val command = VoiceCommandParser.parse("Call Dr. Chen")
    assertTrue(command is VoiceCommand.CallDoctor)
    assertEquals("Dr. Chen", (command as VoiceCommand.CallDoctor).doctorQuery)
  }

  @Test
  fun `voice command parser correctly detects show doctors command`() {
    val command = VoiceCommandParser.parse("Show my doctors")
    assertEquals(VoiceCommand.ShowDoctors, command)
  }

  @Test
  fun `voice command parser correctly detects add doctor command`() {
    val command = VoiceCommandParser.parse("Add doctor Dr. Jenkins")
    assertTrue(command is VoiceCommand.AddNewDoctor)
    assertEquals("Dr. Jenkins", (command as VoiceCommand.AddNewDoctor).doctorName)
  }

  @Test
  fun `doctor profile entity and appointment association work properly`() {
    val doctor = DoctorProfile(
      id = 10L,
      name = "Dr. Robert Chen",
      specialty = "Cardiologist (Heart Doctor)",
      clinicName = "Memorial Health Heart Institute",
      address = "450 Mercy Drive, Suite 204",
      phoneNumber = "555-234-8899",
      notes = "Prefers morning visits."
    )
    val appointment = Appointment(
      id = 1L,
      doctorId = doctor.id,
      doctorName = doctor.name,
      specialty = doctor.specialty,
      clinicName = doctor.clinicName,
      address = doctor.address,
      phoneNumber = doctor.phoneNumber,
      dateText = "Tomorrow",
      timeText = "9:30 AM",
      timestampMillis = System.currentTimeMillis() + 86400000L
    )

    assertEquals(10L, appointment.doctorId)
    assertEquals("Dr. Robert Chen", appointment.doctorName)
    assertEquals("555-234-8899", appointment.phoneNumber)
  }
}
