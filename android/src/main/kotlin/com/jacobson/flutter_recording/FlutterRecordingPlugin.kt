package com.jacobson.flutter_recording

import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import kotlin.reflect.typeOf


/** FlutterRecordingPlugin */
public class FlutterRecordingPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private var recorder: RecordingInterface? = null
  private lateinit var context: Context
  private lateinit var channel : MethodChannel
  private lateinit var eventChannel: EventChannel
  private var activity: Activity? = null
  private var binding: ActivityPluginBinding? = null
  private val permissionRequestCode = 440404
  private val notAvailiblePermission: String = "Getting Permissions"
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding)  {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "flutter_recording")
    channel.setMethodCallHandler(this)
    eventChannel = EventChannel(flutterPluginBinding.flutterEngine.dartExecutor, "flutter_recording/updates")
    eventChannel.setStreamHandler(TimeDBStream)
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "flutter_recording")
      channel.setMethodCallHandler(FlutterRecordingPlugin())
      val eventChannel = EventChannel(registrar.messenger(), "plugins.flutter.io/connectivity_status")
      eventChannel.setStreamHandler(TimeDBStream)
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    println(call.arguments)
    println(call.method)
    when (call.method) {
      "getServiceStatus" -> {
        result.success(RecordingForegroundService.state.name)
      }
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "startRecorder" -> {
        start(call, result)
      }
      "stopRecorder" -> {
        stop(call, result)
      }
      "pauseRecorder" -> pause(call, result)
      "resumeRecorder" -> resume(call, result)
      else -> result.success("${call.method} not implemented")
    }
  }

  private fun start(call: MethodCall, result: Result) {
      println(checkPermission())
      if (!checkPermission()) {
        requestPermission()
        return result.error("1" , notAvailiblePermission, null)
      }
    return result.success(startRecorder(call))
  }

  private fun startRecorder(call: MethodCall): String {
    val fileName: String? = call.argument<String>("fileName")
    val sampleRate: Int? = call.argument<Int>("sampleRate")
    val bitRate: Int? = call.argument<Int>("bitRate")
    val callbackRate: Int? = call.argument<Int>("callbackRate")
    val timestampBufferLength: Int? = call.argument<Int>("timestampBufferLength")
    activity?.let {
      val serviceIntent = Intent(context, RecordingForegroundService::class.java)
      serviceIntent.putExtra("fileName", fileName)
      serviceIntent.putExtra("sampleRate", sampleRate)
      serviceIntent.putExtra("bitRate", bitRate)
      serviceIntent.putExtra("callbackRate", callbackRate)
      serviceIntent.putExtra("timestampBufferLength", timestampBufferLength)
      ContextCompat.startForegroundService(it, serviceIntent)
    }
    return "Starting"
  }


  private fun requestPermission() {
    println("a: $activity")
    activity?.let { ActivityCompat.requestPermissions(activity!!, arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, RECORD_AUDIO), permissionRequestCode)}
  }

  private fun checkPermission(): Boolean {
    val writeResult = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
    val recordResult = ContextCompat.checkSelfPermission(context, RECORD_AUDIO)
    return writeResult == PackageManager.PERMISSION_GRANTED && recordResult == PackageManager.PERMISSION_GRANTED
  }

  private fun stop(call: MethodCall, result: Result) {
    val launchIntent: Intent? = context.packageManager?.getLaunchIntentForPackage(context.packageName)
    val className = launchIntent?.component?.className
    Intent().also { intent ->
      intent.action = "$className.recorder.stop"
      context.sendBroadcast(intent)
    }
    result.success("Stopped")
  }

  private fun pause(call: MethodCall, result: Result) {
    val launchIntent: Intent? = context.packageManager?.getLaunchIntentForPackage(context.packageName)
    val className = launchIntent?.component?.className
    Intent().also { intent ->
      intent.action = "$className.recorder.pause"
      context.sendBroadcast(intent)
    }
    result.success("Paused")
  }

  private fun resume(call: MethodCall, result: Result) {
    val launchIntent: Intent? = context.packageManager?.getLaunchIntentForPackage(context.packageName)
    val className = launchIntent?.component?.className
    println(className)
    Intent().also { intent ->
      intent.action = "$className.recorder.resume"
      context.sendBroadcast(intent)
    }
    result.success("Resumed")
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onDetachedFromActivity() {
    activity = null
    binding?.removeRequestPermissionsResultListener(this)
    binding = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
    this.binding = binding
    binding.addRequestPermissionsResultListener(this)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
    this.binding = binding
    binding.addRequestPermissionsResultListener(this)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
    binding?.removeRequestPermissionsResultListener(this)
    binding = null
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?): Boolean {
    when (requestCode) {
      permissionRequestCode -> {
        if ( null != grantResults ) {
          println(grantResults.isNotEmpty() &&
                  grantResults.get(0) == PackageManager.PERMISSION_GRANTED)
        }
        // only return true if handling the request code
        return true
      }
    }
    return false
  }
}
