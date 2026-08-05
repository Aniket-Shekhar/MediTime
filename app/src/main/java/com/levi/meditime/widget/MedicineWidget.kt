package com.levi.meditime.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.levi.meditime.domain.model.DoseStatus
import com.levi.meditime.domain.model.History
import com.levi.meditime.domain.repository.MedicineRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.*

class MedicineWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = getRepository(context)
        val nextDose = repository.getNextUpcomingDose()
        
        provideContent {
            MedicineWidgetContent(nextDose)
        }
    }

    private fun getRepository(context: Context): MedicineRepository {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        return entryPoint.medicineRepository()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun medicineRepository(): MedicineRepository
    }
}

@Composable
fun MedicineWidgetContent(nextDose: Pair<com.levi.meditime.domain.model.Medicine, com.levi.meditime.domain.model.Reminder>?) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (nextDose != null) {
            val (med, reminder) = nextDose
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val timeStr = sdf.format(Date(reminder.time))
            
            Text(
                text = "Next Dose",
                style = TextStyle(color = FixedColorProvider(Color.Gray), fontSize = 12.sp)
            )
            Text(
                text = med.name,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FixedColorProvider(Color.Black))
            )
            Text(
                text = "$timeStr • ${med.dosage}",
                style = TextStyle(fontSize = 14.sp, color = FixedColorProvider(Color.DarkGray))
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            androidx.glance.Button(
                text = "Mark Taken",
                onClick = actionRunCallback<MarkTakenAction>(
                    actionParametersOf(
                        ActionParameters.Key<String>("medId") to med.id,
                        ActionParameters.Key<String>("remId") to reminder.id
                    )
                )
            )
        } else {
            Text(
                text = "No upcoming doses",
                style = TextStyle(fontSize = 14.sp, color = FixedColorProvider(Color.Gray))
            )
        }
    }
}

/**
 * A simple implementation of ColorProvider to avoid library group restrictions.
 */
class FixedColorProvider(val color: Color) : ColorProvider {
    override fun getColor(context: Context): Color = color
}

class MarkTakenAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val medId = parameters[ActionParameters.Key<String>("medId")] ?: return
        val remId = parameters[ActionParameters.Key<String>("remId")] ?: return
        
        val repository = EntryPointAccessors.fromApplication(context, MedicineWidget.WidgetEntryPoint::class.java).medicineRepository()
        
        val medicine = repository.getMedicineById(medId) ?: return
        val reminder = repository.getReminderById(remId) ?: return
        
        // Add History
        val history = History(
            id = UUID.randomUUID().toString(),
            medicineId = medId,
            reminderId = remId,
            scheduledTime = reminder.time,
            actionTime = System.currentTimeMillis(),
            status = DoseStatus.TAKEN
        )
        repository.addHistoryEntry(history)
        
        // Update Stock
        val updated = medicine.copy(currentStock = (medicine.currentStock - medicine.unitsPerDose).coerceAtLeast(0))
        repository.updateMedicine(updated)
        
        // Refresh Widget
        MedicineWidget().updateAll(context)
    }
}
