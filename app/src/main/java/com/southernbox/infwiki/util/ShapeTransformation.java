package com.southernbox.infwiki.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * Created by nanquan.lin on 2017/6/27 0027.
 */

public class ShapeTransformation extends BitmapTransformation {

    private static float radius = 0f;

    public ShapeTransformation(BitmapPool bitmapPool) {
        super(bitmapPool);
    }

    public ShapeTransformation(Context context) {
        super(context);
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return roundCrop(pool, toTransform);
    }

    private Bitmap roundCrop(BitmapPool pool, Bitmap toTransform) {
        if (toTransform == null) return null;

        Bitmap result = pool.get(toTransform.getWidth(), (int) (toTransform.getWidth() * 0.7), Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(toTransform.getWidth(), (int) (toTransform.getWidth() * 0.5625), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(toTransform, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0f, 0f, toTransform.getWidth(), (int) (toTransform.getWidth() * 0.5625));
//        canvas.drawRoundRect(rectF, radius, radius, paint);
        canvas.drawRect(rectF, paint);
        return result;
    }


    @Override
    public String getId() {
        return getClass().getName().concat(String.valueOf(radius));
    }
}
