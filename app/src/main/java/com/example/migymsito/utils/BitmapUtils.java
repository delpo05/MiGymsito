package com.example.migymsito.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;
import java.io.InputStream;

public class BitmapUtils {

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) {
        try {
            // 1. Obtener la rotación de la imagen original usando EXIF
            int rotation = getRotationFromExif(context, uri);

            // 2. Decodificar solo los límites para obtener las dimensiones
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(input, null, options);
            if (input != null) input.close();

            // 3. Calcular el factor de reducción (inSampleSize)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // 4. Decodificar el bitmap con el inSampleSize calculado
            options.inJustDecodeBounds = false;
            // Usar una configuración de color más ligera (RGB_565 ocupa la mitad que ARGB_8888)
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            if (input != null) input.close();
            
            if (bitmap == null) return null;

            // 5. Aplicar la rotación si es necesaria
            if (rotation != 0) {
                Bitmap rotatedBitmap = rotateBitmap(bitmap, rotation);
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle(); // Liberar memoria del bitmap original
                }
                return rotatedBitmap;
            }
            
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getRotationFromExif(Context context, Uri uri) {
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) return 0;
            ExifInterface exifInterface = new ExifInterface(input);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
