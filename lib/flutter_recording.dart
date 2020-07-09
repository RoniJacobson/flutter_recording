import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class AndroidOutputFormat {
  static const AAC_ADTS = 6;
  static const AMR_NB = 3;
  static const AMR_WB = 4;
  static const DEFAULT = 0;
  static const MPEG_2_TS = 8;
  static const MPEG_4 = 2; // H.264/AAC data encapsulated in MPEG2/TS
  static const OGG = 11;
  static const THREE_GPP = 1;
  static const WEBM = 9; // VP8/VORBIS data in a WEBM container
  static const MP3 = 20;
}

class AndroidAudioEncoder {
  static const AAC = 3; // AAC Low Complexity (AAC-LC) audio codec
  static const AAC_ELD = 5; // Enhanced Low Delay AAC (AAC-ELD) audio codec
  static const AMR_NB = 1; // AMR (Narrowband) audio codec
  static const AMR_WB = 2; // AMR (Wideband) audio codec
  static const DEFAULT = 0;
  static const HE_AAC = 4; // High Efficiency AAC (HE-AAC) audio codec
  static const OPUS = 7; // Opus audio codec
  static const VORBIS = 6; // Ogg Vorbis audio codec (Support is optional)
}

enum RecorderState {
  RECORDING,
  PAUSED,
  STOPPED,
}

class RecorderError extends Error {
  final String error;
  final RecorderState state;
  RecorderError(this.error, this.state);

  @override
  String toString() {
    return "$error; State: $state";
  }
}

class Timestamp {
  final Duration time;
  final double volume;
  Timestamp._(int time, this.volume) : this.time = Duration(milliseconds: time);
}

class FlutterRecording {
  static const MethodChannel _channel =
      const MethodChannel('flutter_recording');
  static const EventChannel _eventChannel =
      const EventChannel('flutter_recording/updates');

  Stream<List<Timestamp>> _onTimestampUpdate;

  RecorderState _state = RecorderState.STOPPED;
  String fileName;
  int sampleRate;
  int channels;
  int bitRate;
  int androidOutputFormat = AndroidOutputFormat.MP3;
  int androidAudioEncoder = AndroidAudioEncoder.DEFAULT;
  int callbackRate;
  int timestampBufferLength;
  String mes;

  RecorderState get state => _state;
  Stream<List<Timestamp>> get onTimestampUpdate {
    if (_onTimestampUpdate == null) {
      _onTimestampUpdate =
          _eventChannel.receiveBroadcastStream().map(_toTimestamps);
    }
    return _onTimestampUpdate;
  }

  List<Timestamp> _toTimestamps(timestamps) {
    return timestamps
        .map<Timestamp>((timestamp) => Timestamp._(timestamp[0], timestamp[1]))
        .toList();
  }

  FlutterRecording() {}

  Future<void> init() async {
    var state = await _channel.invokeMethod('getServiceStatus');
    switch (state) {
      case "Recording":
        _state = RecorderState.RECORDING;
        break;
      case "Stopped":
        _state = RecorderState.STOPPED;
        break;
      default:
    }
  }

  /// will start recording if we are stopped, and throw if we are already recording
  Future<void> start(
      {@required fileName,
      sampleRate = 44100,
      bitRate = 32000,
      channels = 1,
      callbackRate = 200,
      timestampBufferLength = 10}) async {
    if (state == RecorderState.STOPPED) {
      this.fileName = fileName;
      this.sampleRate = sampleRate;
      this.bitRate = bitRate;
      this.channels = channels;
      var args = {
        'fileName': fileName,
        'sampleRate': sampleRate,
        'bitRate': bitRate,
        'channels': channels,
        'androidAudioEncoder': androidAudioEncoder,
        'androidOutputFormat': androidOutputFormat,
        'callbackRate': callbackRate,
        'timestampBufferLength': timestampBufferLength,
      };
      await _channel.invokeMethod('startRecorder', args);
      _state = RecorderState.RECORDING;
    } else {
      throw RecorderError('Already Recording, can\'t start new', state);
    }
  }

  /// will pause the recording if it is playing, do nothing if it is already paused, and throw otherwise
  Future<void> pause() async {
    if (state == RecorderState.RECORDING) {
      await _channel.invokeMethod('pauseRecorder');
      _state = RecorderState.PAUSED;
    } else if (state != RecorderState.PAUSED) {
      throw RecorderError('Not Recording, can\'t pause', state);
    }
  }
  
  /// will resume recording if it is paused, do nothing if we are already recording, and throw otherwise
  Future<void> resume() async {
    if (state == RecorderState.PAUSED) {
      await _channel.invokeMethod('resumeRecorder');
      _state = RecorderState.RECORDING;
    } else if (state != RecorderState.RECORDING) {
      throw RecorderError('Not Paused, can\'t resume', state);
    }
  }
  
  /// will stop recording if we are recording, and throw if we are not recording
  Future<void> stop() async {
    if (state != RecorderState.STOPPED) {
      await _channel.invokeMethod('stopRecorder');
      _state = RecorderState.STOPPED;
    } else {
      throw RecorderError('Not Recording, can\'t stop', state);
    }
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
