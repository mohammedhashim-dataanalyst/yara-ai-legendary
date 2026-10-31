package com.yara.ai.reminders
import android.content.Context
import androidx.work.*
import com.yara.ai.data.MemoryStore
import java.util.*
import java.util.concurrent.TimeUnit

data class LocalReminder(val text:String,val at:Long)
object ReminderManager{private const val PREFIX="yara_reminder_";fun parseAndSchedule(ctx:Context,input:String):Boolean{val m=Regex("(?i)remind me to (.+) at (\d{1,2}):(\d{2})").find(input)?:return false;val h=m.groupValues[2].toIntOrNull()?:return false;val min=m.groupValues[3].toIntOrNull()?:return false;if(h !in 0..23||min !in 0..59)return false;val now=Calendar.getInstance();val at=now.clone() as Calendar;at.set(Calendar.HOUR_OF_DAY,h);at.set(Calendar.MINUTE,min);at.set(Calendar.SECOND,0);if(at.timeInMillis<=now.timeInMillis)at.add(Calendar.DAY_OF_YEAR,1);val text=m.groupValues[1].trim();val data=workDataOf("text" to text,"at" to at.timeInMillis);val req=OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(at.timeInMillis-now.timeInMillis,TimeUnit.MILLISECONDS).setInputData(data).build();WorkManager.getInstance(ctx).enqueueUniqueWork(PREFIX+at.timeInMillis,ExistingWorkPolicy.REPLACE,req);return true}}
class ReminderWorker(ctx:Context,p:WorkerParameters):CoroutineWorker(ctx,p){override suspend fun doWork():Result{val t=inputData.getString("text")?:return Result.failure();com.yara.ai.notify.Notifier.notifyYara(applicationContext,"Reminder: $t");android.speech.tts.TextToSpeech(applicationContext){if(it==android.speech.tts.TextToSpeech.SUCCESS){val e=android.speech.tts.TextToSpeech(applicationContext);e.speak("Reminder: $t",android.speech.tts.TextToSpeech.QUEUE_FLUSH,null,"reminder")}};return Result.success()}}
