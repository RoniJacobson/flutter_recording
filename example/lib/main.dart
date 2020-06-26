import 'package:flutter/material.dart';
import 'package:flutter_recording/flutter_recording.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  FlutterRecording flutterRecording = FlutterRecording();
  Duration currentTime = Duration.zero;
  double currentVolume = 0;

  String getTime() {
      int milliseconds = currentTime.inMilliseconds;
      int seconds = milliseconds ~/ 1000;
      int minutes = seconds % 3600 ~/ 60;
      int hours = seconds ~/ 3600;
      return '$hours:$minutes:$seconds';
    }

  @override
  void initState() {
    flutterRecording.init().whenComplete(() => setState(() {}));
    super.initState();
    flutterRecording.onTimestampUpdate.listen((event) {
      setState(() {
        currentTime = event.time;
        currentVolume = event.volume;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Plugin example app'),
          ),
          body: Center(
            child: Column(
              children: <Widget>[
                RaisedButton(
                  onPressed: () async {
                    await flutterRecording.start(fileName: 'bob');
                    setState(() {});
                  },
                  child: Text('Start'),
                ),
                RaisedButton(
                  onPressed: () async {
                    await flutterRecording.stop();
                    setState(() {});
                  },
                  child: Text('Stop'),
                ),
                RaisedButton(
                  onPressed: () async {
                    await flutterRecording.pause();
                    setState(() {});
                  },
                  child: Text('Pause'),
                ),
                RaisedButton(
                  onPressed: () async {
                    await flutterRecording.resume();
                    setState(() {});
                  },
                  child: Text('Resume'),
                ),
                Text('${flutterRecording.state}'),
                Text('${getTime()}'),
                Text('${this.currentVolume}'),
              ],
            ),
          )),
    );
  }
}
