import 'package:flutter/material.dart';
import 'package:get/get.dart';

import 'controller/ota_server.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter GAIA Control Demo'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  void initState() {
    super.initState();
    Get.put<OtaServer>(OtaServer());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Column(
        children: [
          MaterialButton(
              color: Colors.blue,
              onPressed: () {
                OtaServer.to.startScan();
              },
              child: Text('扫描蓝牙')),
          Expanded(child: Obx(() {
            return ListView.builder(
              itemBuilder: (context, index) {
                var device = OtaServer.to.devices[index];
                return GestureDetector(
                  onTap: () {
                    OtaServer.to.connectDevice(device.id);
                  },
                  child: Container(
                    margin:
                        const EdgeInsets.only(left: 10, right: 10, bottom: 5),
                    padding: const EdgeInsets.only(top: 8, bottom: 8, left: 20),
                    decoration: const BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.all(Radius.circular(5)),
                    ),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(device.name,
                            style: const TextStyle(
                                color: Color(0xff373F50),
                                fontSize: 20,
                                fontWeight: FontWeight.bold)),
                        Text(
                          device.id,
                          style: const TextStyle(
                              color: Color(0xff373F50), fontSize: 12),
                        )
                      ],
                    ),
                  ),
                );
              },
              itemCount: OtaServer.to.devices.length,
            );
          }))
        ],
      ),
    );
  }
}
