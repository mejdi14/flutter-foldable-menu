import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_foldable_menu/enum/manu_slide.dart';
import 'package:flutter_foldable_menu/flutter_foldable_menu.dart';
import 'package:flutter_foldable_menu/model/cell.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Example(),
    );
  }
}

class Example extends StatefulWidget {
  const Example({Key? key}) : super(key: key);

  @override
  _ExampleState createState() => _ExampleState();
}

class _ExampleState extends State<Example> {
  List<FoldableCell> myCards = [
    FoldableCell(color: Colors.yellow, label: 'close', icon: Icon(Icons.close)),
    FoldableCell(
        color: Colors.orange,
        label: 'take photo',
        icon: Icon(Icons.camera_alt)),
    FoldableCell(
        color: Colors.green,
        label: 'share',
        textColor: Colors.black,
        icon: Icon(Icons.share)),
    FoldableCell(color: Colors.purple, label: 'settings', icon: Icon(Icons.settings)),
    FoldableCell(
        color: Colors.blue,
        label: 'verification',
        icon: Icon(Icons.verified_user_rounded)),
    FoldableCell(color: Colors.red, label: 'profile', icon: Icon(Icons.person))
  ];

  var listImages = [
    ['assets/img/image1.jpeg', 'assets/img/image2.jpeg'],
    ['assets/img/image3.jpeg', 'assets/img/image4.jpeg'],
    ['assets/img/image5.jpeg', 'assets/img/image6.jpeg']
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        top: true,
        child: SingleChildScrollView(
          child: Column(
            children: [
              Align(
                alignment: Alignment.topRight,
                child: GestureDetector(
                    onTap: () {
                        Navigator.of(context).push(PageRouteBuilder(
                            opaque: false,
                            pageBuilder: (BuildContext context, _, __) =>
                                FoldableMenu(
                                  myCards: myCards,
                                  side: MenuSide.right,
                                  textStyle: TextStyle(
                                      color: Colors.black,
                                      fontSize: 20,
                                      fontWeight: FontWeight.bold),
                                  onCardSelect: (cell, counter) {
                                    print('this is :$counter');
                                  },
                                )));
                    },
                    child: Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Image.asset(
                        'assets/img/menu.png',
                        height: 30,
                        width: 30,
                      ),
                    )),
              ),
              SizedBox(
                height: 14,
              ),
              Align(
                  alignment: Alignment.topLeft,
                  child: Padding(
                    padding: const EdgeInsets.all(18.0),
                    child: Text(
                      'Art pictures',
                      style: TextStyle(
                          fontSize: 27,
                          color: Colors.black,
                          fontWeight: FontWeight.bold),
                    ),
                  )),
              SizedBox(
                height: 14,
              ),
              Container(
                child: ListView.builder(
                    shrinkWrap: true,
                    itemCount: listImages.length,
                    itemBuilder: (context, index) {
                      return Container(
                        width: double.infinity,
                        child: Row(
                          children: [
                             Expanded(
                              flex: 1,
                              child: Container(
                                  padding: EdgeInsets.all(8),
                                  decoration: BoxDecoration(
                                      borderRadius: BorderRadius.all(
                                          Radius.circular(15))),
                                  child: Image.asset(
                                    listImages[index][0],
                                    height: 200,
                                    fit: BoxFit.cover,
                                  )),
                            ),
                            Expanded(
                              flex: 1,
                              child: Container(
                                  padding: EdgeInsets.all(8),
                                  decoration: BoxDecoration(
                                      borderRadius: BorderRadius.circular(15)),
                                  child: Image.asset(
                                    listImages[index][1],
                                    height: 200,
                                    fit: BoxFit.cover,
                                  )),
                            )
                          ],
                        ),
                      );
                    }),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
