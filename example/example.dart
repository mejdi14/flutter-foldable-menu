import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_foldable_menu/enum/cell.dart';
import 'package:flutter_foldable_menu/enum/enums.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
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
  List<Cell> myCards = [
    Cell(color: Colors.yellow, label: 'yellow'),
    Cell(color: Colors.orange),
    Cell(color: Colors.green, label: 'green', textColor: Colors.black),
    Cell(color: Colors.purple, label: 'purple'),
    Cell(color: Colors.blue, label: 'blue'),
    Cell(color: Colors.red, label: 'red')
  ];

  @override
  Widget build(BuildContext context) {
    return Center(
      child: GestureDetector(
        onTap: () {
          Navigator.of(context).push(PageRouteBuilder(
              opaque: false,
              pageBuilder: (BuildContext context, _, __) => MyHomePage(
                myCards: myCards,
                side: MenuSide.left,
                textStyle: TextStyle(color: Colors.black),
                onCardSelect: (cell, counter){
                  print('this is my label $counter');
                },
              )));
        },
        child: Container(
          color: Colors.blue,
          child: Text('click me bliz'),
        ),
      ),
    );
  }
}