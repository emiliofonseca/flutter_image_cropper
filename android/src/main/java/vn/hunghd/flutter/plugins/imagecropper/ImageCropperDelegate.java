package vn.hunghd.flutter.plugins.imagecropper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.ColorInt;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Date;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import static android.app.Activity.RESULT_OK;

public class ImageCropperDelegate implements PluginRegistry.ActivityResultListener {
    private final Activity activity;
    private MethodChannel.Result pendingResult;
    private MethodCall methodCall;
    private FileUtils fileUtils;

    public ImageCropperDelegate(Activity activity) {
        this.activity = activity;
        fileUtils = new FileUtils();
    }

    public void startCrop(MethodCall call, MethodChannel.Result result) {
        String sourcePath = call.argument("source_path");
        Integer maxWidth = call.argument("max_width");
        Integer maxHeight = call.argument("max_height");
        Double ratioX = call.argument("ratio_x");
        Double ratioY = call.argument("ratio_y");
        Boolean circularCrop  = call.argument("circular_crop");
        
        String title = call.argument("android_toolbar_title");
        Integer toolbarColor = getColorArgument(call, "android_toolbar_color");
        Boolean hideCropGrid = call.argument("android_hide_crop_grid");
        Integer dimmedLayerColor = getColorArgument(call, "android_dimmed_layer_color");
        Integer frameColor = getColorArgument(call, "android_frame_color");

        methodCall = call;
        pendingResult = result;

        File outputDir = activity.getCacheDir();
        File outputFile = new File(outputDir, "image_cropper_" + (new Date()).getTime() + ".jpg");

        Uri sourceUri = Uri.fromFile(new File(sourcePath));
        Uri destinationUri = Uri.fromFile(outputFile);
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);

        if(circularCrop != null && circularCrop) {
            options.setCircleDimmedLayer(true);
        }
        if(hideCropGrid != null && hideCropGrid) {
            options.setShowCropGrid(false);
        }
        if(dimmedLayerColor != null) {
            options.setDimmedLayerColor(dimmedLayerColor.intValue());
        }
        if(frameColor != null) {
            options.setCropFrameColor(frameColor.intValue());
        }
        if (title != null) {
            options.setToolbarTitle(title);
        }
        if (toolbarColor != null) {
            int intToolbarColor  = toolbarColor.intValue();
            options.setToolbarColor(intToolbarColor);
            options.setStatusBarColor(darkenColor(intToolbarColor));
        }
        UCrop cropper = UCrop.of(sourceUri, destinationUri).withOptions(options);
        if (maxWidth != null && maxHeight != null) {
            cropper.withMaxResultSize(maxWidth, maxHeight);
        }
        if (ratioX != null && ratioY != null) {
            cropper.withAspectRatio(ratioX.floatValue(), ratioY.floatValue());
        }
        cropper.start(activity);
    }

    private Integer getColorArgument(MethodCall call, String argumentKey) {
        Object object = call.argument(argumentKey);
        if(object instanceof Long) {
            return ((Long) object).intValue();
        } else if(object instanceof Integer) {
            return ((Integer) object).intValue();
        } else {
            return null;    
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                final Uri resultUri = UCrop.getOutput(data);
                finishWithSuccess(fileUtils.getPathFromUri(activity, resultUri));
                return true;
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                finishWithError("crop_error", cropError.getLocalizedMessage(), cropError);
                return true;
            } else {
                pendingResult.success(null);
                clearMethodCallAndResult();
                return true;
            }
        }
        return false;
    }

    private void finishWithSuccess(String imagePath) {
        pendingResult.success(imagePath);
        clearMethodCallAndResult();
    }

    private void finishWithError(String errorCode, String errorMessage, Throwable throwable) {
        pendingResult.error(errorCode, errorMessage, throwable);
        clearMethodCallAndResult();
    }


    private void clearMethodCallAndResult() {
        methodCall = null;
        pendingResult = null;
    }

    @ColorInt
    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
