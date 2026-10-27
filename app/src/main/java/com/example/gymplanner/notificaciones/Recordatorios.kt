package com.example.gymplanner.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.gymplanner.MainActivity
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object Recordatorios {

    fun programar(
        context: Context,
        sesionId: Long,
        nombre: String,
        dia: Int,
        minutoDelDia: Int,
        aviso: Int
    ) {
        val ahora = LocalDateTime.now()
        var sesion = ahora.toLocalDate()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dia)))
            .atTime(minutoDelDia / 60, minutoDelDia % 60)
        if (!sesion.minusMinutes(aviso.toLong()).isAfter(ahora)) {
            sesion = sesion.plusWeeks(1)
        }
        val retraso = Duration.between(ahora, sesion.minusMinutes(aviso.toLong()))

        val datos = Data.Builder()
            .putLong("sesionId", sesionId)
            .putString("nombre", nombre)
            .putInt("dia", dia)
            .putInt("minutoDelDia", minutoDelDia)
            .putInt("aviso", aviso)
            .build()

        val peticion = OneTimeWorkRequest.Builder(RecordatorioWorker::class.java)
            .setInitialDelay(retraso.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(datos)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("sesion-$sesionId", ExistingWorkPolicy.REPLACE, peticion)
    }

    fun cancelar(context: Context, sesionId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("sesion-$sesionId")
    }
}

class RecordatorioWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val sesionId = inputData.getLong("sesionId", -1)
        val nombre = inputData.getString("nombre") ?: "Entrenamiento"
        val dia = inputData.getInt("dia", 1)
        val minutoDelDia = inputData.getInt("minutoDelDia", 0)
        val aviso = inputData.getInt("aviso", 30)

        mostrarNotificacion(applicationContext, sesionId, nombre, aviso)
        Recordatorios.programar(applicationContext, sesionId, nombre, dia, minutoDelDia, aviso)
        return Result.success()
    }
}

private fun mostrarNotificacion(context: Context, sesionId: Long, nombre: String, aviso: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) return

    val canal = "recordatorios"
    val gestor = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        gestor.createNotificationChannel(
            NotificationChannel(canal, "Recordatorios de entrenamiento", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    val abrirApp = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
    )
    val notificacion = NotificationCompat.Builder(context, canal)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle("Hora de entrenar")
        .setContentText("$nombre empieza en $aviso min")
        .setContentIntent(abrirApp)
        .setAutoCancel(true)
        .build()

    gestor.notify(sesionId.toInt(), notificacion)
}