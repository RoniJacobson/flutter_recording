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
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar


/** FlutterRecordingPlugin */
public class FlutterRecordingPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private var recorder: RecordingInterface? = null
  private lateinit var context: Context
  private lateinit var channel : MethodChannel
  private var activity: Activity? = null
  private var binding: ActivityPluginBinding? = null
  private val permissionRequestCode = 440404
  private val notAvailiblePermission: String = "Getting Permissions"
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding)  {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_recording")
    channel.setMethodCallHandler(this);
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
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    println(call.arguments)
    println(call.method)
    when (call.method) {
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "startRecorder" -> {
        start(call, result)
      }
      "stopRecorder" -> {
        stop(call, result)
      }
      "pauseRecorder" -> result.success("Pausing")
      "resumeRecorder" -> result.success("Playing")
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
    if (call.argument<Int>("androidOutputFormat") == 20) {
//      recorder = MP3Recorder(call.argument<String>("fileName"),
//              call.argument<Int>("bitRate")!!,
//              call.argument<Int>("sampleRate")!!, 5)
      activity?.let {
        val serviceIntent = Intent(context, RecordingForegroundService::class.java)
        ContextCompat.startForegroundService(it, serviceIntent)
      }
//      recorder?.startRecording()
      println('k')
      } else {
      return "not yet supported"
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
//    recorder?.stopRecording()
//    val serviceIntent = Intent(activity?.applicationContext, RecordingForegroundService::class.java)
//    activity?.applicationContext?.stopService(serviceIntent)
    Intent().also { intent ->
      intent.action = "flutter.recorder.stop"
      context.sendBroadcast(intent)
    }
    result.success("Stopped")
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
