package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.KuwaitGovernorate
import com.example.model.PrayerType
import com.example.ui.KuwaitSalatViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kuwait Salat", appName)
  }

  @Test
  fun `viewModel instantiates without error and initializes defaults`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val vm = KuwaitSalatViewModel(application)
    assertNotNull(vm.dailyPrayerTimes.value)
    assertNotNull(vm.nextPrayerInfo.value)
    assertEquals(KuwaitGovernorate.AL_ASIMAH, vm.selectedGovernorate.value)
  }

  @Test
  fun `tasbih increment and reset works properly`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val vm = KuwaitSalatViewModel(application)

    vm.resetTasbih()
    assertEquals(0, vm.tasbihCurrent.value)

    vm.incrementTasbih()
    assertEquals(1, vm.tasbihCurrent.value)

    vm.incrementTasbih()
    assertEquals(2, vm.tasbihCurrent.value)

    vm.resetTasbih()
    assertEquals(0, vm.tasbihCurrent.value)
  }

  @Test
  fun `governorate selection updates prayer times`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val vm = KuwaitSalatViewModel(application)

    vm.selectGovernorate(KuwaitGovernorate.JAHRA)
    assertEquals(KuwaitGovernorate.JAHRA, vm.selectedGovernorate.value)
    assertEquals(KuwaitGovernorate.JAHRA, vm.dailyPrayerTimes.value.governorate)

    vm.selectGovernorate(KuwaitGovernorate.AHMADI)
    assertEquals(KuwaitGovernorate.AHMADI, vm.selectedGovernorate.value)
    assertEquals(KuwaitGovernorate.AHMADI, vm.dailyPrayerTimes.value.governorate)
  }

  @Test
  fun `prayer completion toggle works correctly`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val vm = KuwaitSalatViewModel(application)

    val initialFajr = vm.prayerLog.value[PrayerType.FAJR] ?: false
    vm.togglePrayerCompleted(PrayerType.FAJR)
    val toggledFajr = vm.prayerLog.value[PrayerType.FAJR] ?: false
    assertNotEquals(initialFajr, toggledFajr)
  }
}
