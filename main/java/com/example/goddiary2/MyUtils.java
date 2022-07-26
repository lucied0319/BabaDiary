package com.example.goddiary2;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyUtils {
    public  static Bitmap getImageFromByte(byte[] bytes){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
        int bitmapSize = 1;
        if((options.outHeight * options.outWidth) > 500000) {
            double outSize = (double) (options.outHeight * options.outWidth) / 500000;
            bitmapSize = (int) (Math.sqrt(outSize) + 1);
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = bitmapSize;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
        return  bitmap;
    }
    public static byte[] getByteFromImage(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[] byteArray = stream.toByteArray();
        return  byteArray;
    }

    public static Bitmap getImageFromStream(ContentResolver resolver , Uri uri) throws IOException {
        InputStream in;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        in = resolver.openInputStream(uri);
        BitmapFactory.decodeStream(in,null,options);
        in.close();
        int bitmapSize = 1;
        if((options.outHeight * options.outWidth) > 500000){
            double outSize = (double)(options.outHeight * options.outWidth) / 500000;
            bitmapSize = (int)(Math.sqrt(outSize) + 1);
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = bitmapSize;
        in = resolver.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(in,null,options);
        in.close();
        return  bitmap;
    }
}
