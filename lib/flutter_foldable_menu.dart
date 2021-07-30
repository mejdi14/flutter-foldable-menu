library flutter_foldable_menu;

import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'dart:math' as math;
import 'model/cell.dart';
import 'enum/manu_slide.dart';

class FoldableMenu extends StatefulWidget {
  FoldableMenu(
      {required this.myCards,
      this.side = MenuSide.right,
      this.textStyle,
      this.duration,
      this.backgroundOpacity,
      this.onCardSelect})
      : super();
  var myCards;
  MenuSide side;
  TextStyle? textStyle;
  double? backgroundOpacity;
  Duration? duration;
  Function(String label, int counter)? onCardSelect;

  @override
  _FoldableMenuState createState() => _FoldableMenuState();
}

class _FoldableMenuState extends State<FoldableMenu>
    with SingleTickerProviderStateMixin {
  late StreamController<List<Widget>> _streamList;
  late AnimationController _animationController;
  bool isClicked = false;
  final ValueNotifier<double> rotationX = ValueNotifier<double>(math.pi);

  List<Widget> cardsList = [];
  late Animation sizeAnimation;
  var oneTween = Tween<double>(
    begin: 0.0,
    end: 100.0,
  );

  @override
  void initState() {
    super.initState();
    widget.myCards = widget.myCards.reversed.toList();
    _streamList = StreamController<List<Widget>>();
    _animationController = AnimationController(
        vsync: this, duration: widget.duration ?? Duration(seconds: 2));
    createListItems();
    WidgetsBinding.instance!.addPostFrameCallback((_) {
      afterBuild();
    });
    sizeAnimation = TweenSequence(
      [
        TweenSequenceItem(
          tween: Tween(begin: math.pi, end: 0.0),
          weight: 1,
        ),
        ...initiateListTween()
      ],
    ).animate(_animationController)
      ..addListener(() {
        animationEngine();
      })
      ..addStatusListener((status) {
        if (status == AnimationStatus.completed) {
        } else if (status == AnimationStatus.dismissed) {
          Navigator.pop(context);
        }
      });
  }

  void animationEngine() {
    if (_animationController.value <= (1 / widget.myCards.length)) {
      widget.myCards[widget.myCards.length - 1].isVisible = true;
      widget.myCards[widget.myCards.length - 1].textOpacity = 1.0;
      rotationX.value = sizeAnimation.value;
    }
    for (var i = 1; i < widget.myCards.length - 1; i++) {
      if (_animationController.value > (i / widget.myCards.length) &&
          _animationController.value <=
              (1 -
                  (((widget.myCards.length - 1) - i) /
                      widget.myCards.length))) {
        widget.myCards[((widget.myCards.length - 1) - i)].isVisible = true;
        widget.myCards[((widget.myCards.length - 2) - i)].isVisible = false;
        widget.myCards[((widget.myCards.length - 1) - i)].rotationX =
            sizeAnimation.value;
        if(i > 1)
        widget.myCards[((widget.myCards.length) - i)].rotationX = math.pi;
        widget.myCards[((widget.myCards.length - 1) - i)].textOpacity =
            sizeAnimation.value / math.pi;
      }
    }
    if (_animationController.value > (1 - (1 / widget.myCards.length))) {
      widget.myCards[1].rotationX = math.pi;
      widget.myCards[0].isVisible = true;
      widget.myCards[0].textOpacity = 1.0;
      widget.myCards[0].rotationX = sizeAnimation.value;
    }
    createListItems();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        backgroundColor:
            Colors.white.withOpacity(widget.backgroundOpacity ?? 0.85),
        body: SafeArea(
            top: true,
            child: GestureDetector(
              behavior: HitTestBehavior.opaque,
              onTap: () {
                _animationController.reverse();
              },
              child: Align(
                  alignment: widget.side.index == MenuSide.left.index
                      ? Alignment.centerLeft
                      : Alignment.centerRight,
                  child: Container(
                    height: MediaQuery.of(context).size.height,
                    child: ValueListenableBuilder<double>(
                        valueListenable: rotationX,
                        builder: (BuildContext context, double value,
                            Widget? child) {
                          return (Transform(
                            transform: Matrix4.identity()
                              ..setEntry(2, 2, -0.001)
                              ..rotateY(rotationX.value)
                              ..right
                              ..dimension,
                            alignment: widget.side.index == MenuSide.left.index
                                ? Alignment.centerLeft
                                : Alignment.centerRight,
                            child: StreamBuilder(
                              stream: _streamList.stream,
                              builder: (context, AsyncSnapshot<List<Widget>>? snapshot) {
                                return Stack(
                                  children: [...((snapshot != null && snapshot.data != null)
                                    ? ((snapshot.data?.isNotEmpty ?? false)
                                    ? (snapshot.data) : []) : [])!],
                                );
                              }
                            ),
                          ));
                        }),
                  )),
            )));
  }

  createListItems() {
    cardsList = [];
    var counter = widget.myCards.length - 1; // 5
    for (var cell in widget.myCards) {
      cardsList.add(Positioned(
        top: (counter >= (2)) // if 5 > 4
            ? ((cell.height ?? 70) * (counter - 1))
            : cell.topPosition,
        child: GestureDetector(
          behavior: HitTestBehavior.opaque,
          onTap: () {
            widget.onCardSelect!(cell.label ?? '', counter);
          },
          child: Container(
            width: ((cell.width ?? 70) + 210),
            child: Padding(
              padding:
                  EdgeInsets.only(bottom: counter > 0 ? (cell.width ?? 0) : 0),
              child: Row(
                crossAxisAlignment: isItOnTheRightSide()
                    ? CrossAxisAlignment.end
                    : CrossAxisAlignment.start,
                mainAxisAlignment: isItOnTheRightSide()
                    ? MainAxisAlignment.end
                    : MainAxisAlignment.start,
                children: [
                  if (isItOnTheRightSide())
                    ...labelGenerator(cell, counter, true),
                  Visibility(
                    visible: cell.isVisible ?? false,
                    child: Transform(
                      alignment: Alignment.bottomCenter,
                      transform: Matrix4.identity()
                        ..setEntry(2, 1, 0.001)
                        ..rotateX(cell.rotationX ?? 0),
                      child: Container(
                          decoration: BoxDecoration(
                              border: Border.all(color: Colors.black),
                              color: Colors.white),
                          //color: cell.color,
                          height: cell.height,
                          width: cell.width,
                          child: Transform(
                              alignment: Alignment.center,
                              transform: (counter == 0)
                                  ? Matrix4.rotationX(0)
                                  : Matrix4.rotationX(math.pi),
                              child: cell.icon)),
                    ),
                  ),
                  if (!(isItOnTheRightSide()))
                    ...labelGenerator(cell, counter, false),
                ],
              ),
            ),
          ),
        ),
      ));
      counter--;
    }
    _streamList.sink.add(cardsList);
  }

  bool isItOnTheRightSide() => widget.side.index == MenuSide.right.index;

  List<Widget> labelGenerator(FoldableCell cell, int counter, bool isRight) {
    return [
      if (!isRight)
        SizedBox(
          width: 10,
        ),
      Container(
          width: 200,
          height: cell.height ?? 70,
          child: Visibility(
              visible: cell.isVisible ?? false,
              child: Transform(
                alignment: Alignment.bottomCenter,
                transform: Matrix4.identity()
                  ..setEntry(3, 2, 0.001)
                  ..rotateX(cell.rotationX ?? 0),
                child: Opacity(
                    opacity: cell.textOpacity ?? 0,
                    child: Center(
                      child: Transform(
                        alignment: Alignment.center,
                        transform: (counter == 0)
                            ? Matrix4.rotationX(0)
                            : Matrix4.rotationX(math.pi),
                        child: Align(
                          alignment: isItOnTheRightSide()
                              ? Alignment.centerRight
                              : Alignment.centerLeft,
                          child: Text(
                            cell.label ?? '',
                            style: widget.textStyle ??
                                TextStyle(color: Colors.white),
                          ),
                        ),
                      ),
                    )),
              ))),
      if (isRight)
        SizedBox(
          width: 10,
        ),
    ];
  }

  @override
  void dispose() {
    // TODO: implement dispose
    _animationController.dispose();
    _streamList.close();
    rotationX.dispose();
    super.dispose();
  }

  List<TweenSequenceItem> initiateListTween() {
    List<TweenSequenceItem> list = [];
    for (var i = 0; i < widget.myCards.length - 1; i++) {
      list.add(
        TweenSequenceItem(tween: Tween(begin: 0.0, end: math.pi), weight: 1),
      );
    }
    return list;
  }

  afterBuild() {
    _animationController.forward();
  }
}
