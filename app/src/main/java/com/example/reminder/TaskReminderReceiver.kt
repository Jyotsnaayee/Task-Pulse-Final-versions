package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AlarmSoundType

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
        val durationSeconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 30)
        val soundTypeName = intent.getStringExtra(EXTRA_SOUND_TYPE) ?: AlarmSoundType.SYSTEM_ALARM.name
        val spotifyUri = intent.getStringExtra(EXTRA_SPOTIFY_URI) ?: "spotify:genre:focus"
        val soundType = try {
            AlarmSoundType.valueOf(soundTypeName)
        } catch (e: Exception) {
            AlarmSoundType.SYSTEM_ALARM
        }

        // Action when user clicks "Dismiss Alarm" action button
        if (ACTION_DISMISS_ALARM == intent.action) {
            stopActiveAlarm(context, taskId.toInt())
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId.toInt())
            return
        }

        // Trigger alarm sound and vibration for specified duration
        playAlarmAudioAndVibration(context, soundType, durationSeconds, taskId.toInt())

        // If Spotify sound was chosen, trigger intent to open Spotify with focus playlist / music
        if (soundType == AlarmSoundType.SPOTIFY) {
            launchSpotifyForTask(context, taskTitle, spotifyUri)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android O+
        val channelId = if (soundType == AlarmSoundType.SPOTIFY) {
            CHANNEL_ID_SPOTIFY
        } else {
            CHANNEL_ID_SYSTEM_ALARM
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                channelId,
                if (soundType == AlarmSoundType.SPOTIFY) "TaskPulse Spotify Reminders" else "TaskPulse System Alarm Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task alarm reminders with ringtones and duration control"
                enableVibration(true)
                setSound(alarmSoundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action intent
        val dismissIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = ACTION_DISMISS_ALARM
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt() + 100000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundBadge = if (soundType == AlarmSoundType.SPOTIFY) "🎵 Spotify Alarm" else "🔔 System Alarm Tone"
        val durationLabel = if (durationSeconds < 60) "${durationSeconds}s" else "${durationSeconds / 60}m"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ $taskTitle")
            .setContentText("Alarm sounding ($durationLabel) • $soundBadge")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Time to work on: $taskTitle\nPlaying $soundBadge for $durationLabel. Tap Dismiss to stop ringtone.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Dismiss Alarm",
                dismissPendingIntent
            )
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    private fun playAlarmAudioAndVibration(
        context: Context,
        soundType: AlarmSoundType,
        durationSeconds: Int,
        notificationId: Int
    ) {
        try {
            // Get system default ALARM tone (or ringtone fallback)
            val alarmToneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            activeMediaPlayer?.stop()
            activeMediaPlayer?.release()

            val mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmToneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            activeMediaPlayer = mediaPlayer

            // Trigger pattern vibration
            triggerVibration(context, durationSeconds)

            // Stop playback after duration
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                stopActiveAlarm(context, notificationId)
            }, durationSeconds * 1000L)

        } catch (e: Exception) {
            Log.e("TaskReminderReceiver", "Error playing alarm tone: ${e.message}", e)
        }
    }

    private fun triggerVibration(context: Context, durationSeconds: Int) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 800, 400, 800, 400)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(pattern, 0) // Repeat from index 0
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, 0)
                }

                handler.postDelayed({
                    vibrator.cancel()
                }, durationSeconds * 1000L)
            }
        } catch (e: Exception) {
            Log.e("TaskReminderReceiver", "Vibration error: ${e.message}")
        }
    }

    private fun launchSpotifyForTask(context: Context, taskTitle: String, spotifyUri: String) {
        try {
            // First attempt to open Spotify App via URI
            val spotifyIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(spotifyUri)
                putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.packageName))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.spotify.music")
            }
            context.startActivity(spotifyIntent)
        } catch (e: Exception) {
            // If Spotify app is not installed, open web Spotify search / playlist
            try {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://open.spotify.com/search/" + Uri.encode(taskTitle + " focus music"))
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                Log.e("TaskReminderReceiver", "Could not open Spotify web fallback: ${e2.message}")
            }
        }
    }

    companion object {
        const val CHANNEL_ID_SYSTEM_ALARM = "taskpulse_system_alarm_channel"
        const val CHANNEL_ID_SPOTIFY = "taskpulse_spotify_alarm_channel"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
        const val EXTRA_SOUND_TYPE = "extra_sound_type"
        const val EXTRA_SPOTIFY_URI = "extra_spotify_uri"
        const val ACTION_DISMISS_ALARM = "com.example.reminder.ACTION_DISMISS_ALARM"

        private var activeMediaPlayer: MediaPlayer? = null
        private val handler = Handler(Looper.getMainLooper())

        fun stopActiveAlarm(context: Context, notificationId: Int? = null) {
            try {
                activeMediaPlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                }
                activeMediaPlayer = null

                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                vibrator?.cancel()
            } catch (e: Exception) {
                Log.e("TaskReminderReceiver", "Error stopping active alarm: ${e.message}")
            }
        }
    }
}
