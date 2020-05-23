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

  @override
  void initState() {
    super.initState();
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
                Text('${flutterRecording.state}')
              ],
            ),
          )),
    );
  }
}
