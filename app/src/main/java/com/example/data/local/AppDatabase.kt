package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.QuestionDao
import com.example.data.local.dao.RecommendationDao
import com.example.data.local.dao.ResponseDao
import com.example.data.local.dao.SessionDao
import com.example.data.local.dao.SubjectDao
import com.example.data.model.RevisionRecommendation
import com.example.data.model.Subject
import com.example.data.model.Topic
import com.example.data.model.VivaQuestion
import com.example.data.model.VivaResponse
import com.example.data.model.VivaSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        Subject::class,
        Topic::class,
        VivaSession::class,
        VivaQuestion::class,
        VivaResponse::class,
        RevisionRecommendation::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun sessionDao(): SessionDao
    abstract fun questionDao(): QuestionDao
    abstract fun responseDao(): ResponseDao
    abstract fun recommendationDao(): RecommendationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vivamate_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default subjects and topics in background
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            val csSubjectId = UUID.randomUUID().toString()
            val eeSubjectId = UUID.randomUUID().toString()
            val medSubjectId = UUID.randomUUID().toString()

            val csSubject = Subject(
                id = csSubjectId,
                name = "Computer Science & Engineering",
                description = "Data structures, algorithms, operating systems, and DBMS",
                level = "Undergraduate",
                color = 0xFF4F46E5 // Indigo
            )
            val eeSubject = Subject(
                id = eeSubjectId,
                name = "Electrical & Electronic Engineering",
                description = "Circuit theory, digital logic, signals & microprocessors",
                level = "Undergraduate",
                color = 0xFF0D9488 // Teal
            )
            val medSubject = Subject(
                id = medSubjectId,
                name = "Medicine & Health Sciences",
                description = "Human anatomy, cardiovascular physiology & pharmacology",
                level = "Undergraduate",
                color = 0xFFBE123C // Rose
            )

            database.subjectDao().insertSubject(csSubject)
            database.subjectDao().insertSubject(eeSubject)
            database.subjectDao().insertSubject(medSubject)

            // Seed Topics
            database.subjectDao().insertTopic(
                Topic(
                    subjectId = csSubjectId,
                    title = "Binary Search Trees & Balancing",
                    syllabusText = "Tree properties, BST insertion and deletion, AVL rotations, time complexity of searches and amortized balancing.",
                    importance = "High",
                    targetConfidence = 85
                )
            )
            database.subjectDao().insertTopic(
                Topic(
                    subjectId = csSubjectId,
                    title = "Database Indexing & ACID Properties",
                    syllabusText = "B+ Trees indexing, clustered vs non-clustered, transactions, isolation levels, concurrency control, write-ahead logging.",
                    importance = "High",
                    targetConfidence = 90
                )
            )
            database.subjectDao().insertTopic(
                Topic(
                    subjectId = eeSubjectId,
                    title = "Op-Amp Linear & Non-linear Applications",
                    syllabusText = "Inverting and non-inverting configurations, virtual ground concept, differential amplifier, slew rate, active filters.",
                    importance = "High",
                    targetConfidence = 80
                )
            )
            database.subjectDao().insertTopic(
                Topic(
                    subjectId = medSubjectId,
                    title = "Cardiovascular Physiology & Action Potentials",
                    syllabusText = "Cardiac cycle, SA and AV node conduction, refractory periods, Frank-Starling law, blood pressure regulation mechanisms.",
                    importance = "High",
                    targetConfidence = 85
                )
            )
        }
    }
}
