import 'dart:async';
import 'dart:io';
import 'package:meta/meta.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ImageCropper {
  static const MethodChannel _channel =
      const MethodChannel('plugins.hunghd.vn/image_cropper');

  /// Loads the image from [sourcePath], represents it on an UI that lets users
  /// crop, rotate the image. If the [ratioX] and [ratioY] are set, it will force
  /// users to crop the image in fixed aspect ratio.
  static Future<File> cropImage({
      @required String sourcePath,
      double ratioX,
      double ratioY,
      int maxWidth,
      int maxHeight,
      bool circularCrop,
      ImageCropperAndroidOptions androidOptions
  }) async {
    assert(sourcePath != null);

    if (maxWidth != null && maxWidth < 0) {
      throw new ArgumentError.value(maxWidth, 'maxWidth cannot be negative');
    }

    if (maxHeight != null && maxHeight < 0) {
      throw new ArgumentError.value(maxHeight, 'maxHeight cannot be negative');
    }

    final String resultPath = await _channel.invokeMethod(
        'cropImage',
        <String, dynamic> {
          'source_path': sourcePath,
          'max_width': maxWidth,
          'max_height': maxHeight,
          'ratio_x': ratioX,
          'ratio_y': ratioY,
          'circular_crop': circularCrop,
          // Android Options
          'android_toolbar_title': androidOptions?.toolbarTitle,
          'android_toolbar_color': androidOptions?.toolbarColor?.value,
          'android_hide_crop_grid': androidOptions?.androidHideCropGrid,
          'android_dimmed_layer_color': androidOptions?.androidDimmedLayerColor?.value,
          'android_frame_color': androidOptions?.androidFrameColor?.value
          }
        );
    return resultPath == null ? null : new File(resultPath);
  }

  
}

class ImageCropperAndroidOptions {
  String toolbarTitle;
  Color toolbarColor;
  bool androidHideCropGrid;
  Color androidDimmedLayerColor;
  Color androidFrameColor;

  ImageCropperAndroidOptions({
    this.toolbarTitle,
    this.toolbarColor,
    this.androidHideCropGrid,
    this.androidDimmedLayerColor,
    this.androidFrameColor,
  });

}