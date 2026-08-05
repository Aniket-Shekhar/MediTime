package com.levi.meditime.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.levi.meditime.data.local.converters.Converters
import com.levi.meditime.data.local.dao.MedicineDao
import com.levi.meditime.data.local.entity.EmergencyContactEntity
import com.levi.meditime.data.local.entity.FamilyMemberEntity
import com.levi.meditime.data.local.entity.HistoryEntity
import com.levi.meditime.data.local.entity.MedicineEntity
import com.levi.meditime.data.local.entity.ReminderEntity
import com.levi.meditime.data.local.entity.UserEntity
import com.levi.meditime.data.local.entity.CaretakerEntity
import com.levi.meditime.data.local.entity.SymptomEntity
import com.levi.meditime.data.local.entity.PrescriptionEntity
import com.levi.meditime.data.local.entity.AppointmentEntity
import com.levi.meditime.data.local.entity.UserProfileEntity

@Database(
    entities = [
        MedicineEntity::class,
        ReminderEntity::class,
        HistoryEntity::class,
        FamilyMemberEntity::class,
        EmergencyContactEntity::class,
        UserEntity::class,
        CaretakerEntity::class,
        SymptomEntity::class,
        PrescriptionEntity::class,
        AppointmentEntity::class,
        UserProfileEntity::class
    ],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MediTimeDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao

    companion object {
        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add selectedDays column to medicines
                db.execSQL("ALTER TABLE medicines ADD COLUMN selectedDays TEXT NOT NULL DEFAULT '[]'")
                // Add selectedDays column to reminders
                db.execSQL("ALTER TABLE reminders ADD COLUMN selectedDays TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                val currentTime = System.currentTimeMillis()
                db.execSQL("ALTER TABLE medicines ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $currentTime")
                db.execSQL("ALTER TABLE reminders ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $currentTime")
                db.execSQL("ALTER TABLE history ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $currentTime")
                db.execSQL("ALTER TABLE symptoms ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $currentTime")
                db.execSQL("ALTER TABLE appointments ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $currentTime")
                db.execSQL("ALTER TABLE emergency_contacts ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $currentTime")
                db.execSQL("ALTER TABLE caretakers ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $currentTime")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $currentTime")
            }
        }
    }
}
