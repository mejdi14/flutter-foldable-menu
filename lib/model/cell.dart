import 'package:flutter/material.dart';

class FoldableCell {
  double? rotationX = 0.0;
  double? topPosition = 0.0;
  double? textOpacity = 0.0;
  double? height = 80.0;
  bool? isVisible = false;
  double? width = 80.0;
  String? label;
  Color? color;
  Color? textColor;
  Icon? icon;

  FoldableCell(
      {this.rotationX,
      this.topPosition,
      this.textOpacity,
      this.label,
      this.color,
      this.textColor,
      this.icon,
      this.isVisible});

  @override
  String toString() {
    return 'Cell{rotationX: $rotationX, topPosition: $topPosition, height: $height, isVisible: $isVisible, width: $width, label: $label, color: $color, icon: $icon}';
  }
}
