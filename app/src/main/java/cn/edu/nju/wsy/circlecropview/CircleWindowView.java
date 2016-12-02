package cn.edu.nju.wsy.circlecropview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by wangsiyi on 2016/11/29.
 */
public class CircleWindowView extends ImageView {
    private static final String TAG = CircleWindowView.class.getSimpleName();

    private CircleWindowView(Context context) {
        super(context);
    }

    public CircleWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleWindowView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        paint = new Paint();
        paint.setAntiAlias(true);
        radius = MIN_RADIUS;
        mode = STATE.ZERO;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleWindowView);
        OcclusionColor = typedArray.getColor(R.styleable.CircleWindowView_OcclusionColor, 0xe0000000);
        typedArray.recycle();

        Drawable d = getDrawable();
        double imgWidth = d.getIntrinsicWidth();
        double imgHeight = d.getIntrinsicHeight();
        ratio = Math.max(imgWidth / 800, imgHeight / 1000);
        final int aimWidth = (int) (imgWidth / ratio);
        final int aimHeight = (int) (imgHeight / ratio);

        post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(aimWidth, aimHeight);
                setLayoutParams(layoutParams);
            }
        });
    }

    public Bitmap getCrop() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int startX = (int) (x - radius < 0 ? 0 : x - radius);
        int startY = (int) (y - radius < 0 ? 0 : y - radius);
        int cropWidth = (int) (startX + 2 * radius > bitmapWidth ? bitmapWidth - startX : 2 * radius);
        int cropHeight = (int) (startY + 2 * radius > bitmapHeight ? bitmapHeight - startY : 2 * radius);
        startX *= ratio;
        startY *= ratio;
        cropWidth *= ratio;
        cropHeight *= ratio;
        return Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight);
    }

    private double ratio;
    private Paint paint;
    private float x;
    private float y;
    private float radius;
    private static final int MIN_RADIUS = 200;
    private int height;
    private int width;
    private int OcclusionColor = 0xe0000000;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        height = getHeight();
        width = getWidth();

        int saveCount = canvas.saveLayer(0, 0, width, height, paint,
                Canvas.ALL_SAVE_FLAG);
        paint.setColor(OcclusionColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

//        paint.setColor(Color.WHITE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        x = x == 0 ? width / 2 : x;
        x = x < radius ? radius : x;
        x = x > width - radius ? width - radius : x;
        y = y == 0 ? height / 2 : y;
        y = y < radius ? radius : y;
        y = y > height - radius ? height - radius : y;
        radius = x - radius < 0 ? x : radius;
        radius = x + radius > width ? width - x : radius;
        radius = y - radius < 0 ? y : radius;
        radius = y + radius > height ? height - y : radius;
        radius = radius < MIN_RADIUS ? MIN_RADIUS : radius;
        radius = radius > width / 2 ? width / 2 : radius;
        radius = radius > height / 2 ? height / 2 : radius;
        canvas.drawCircle(x, y, radius, paint);
        paint.setXfermode(null);
        canvas.restoreToCount(saveCount);
    }

    private STATE mode;

    private enum STATE {
        ZERO, ONE, TWO;
    }

    private double initDistance;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = STATE.ONE;
                initX = event.getX();
                initY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mode = STATE.ZERO;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == STATE.ONE)
                    changePosition(event);
                if (mode == STATE.TWO) {
                    changeRadius(distance(event) - initDistance);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = STATE.TWO;
                initDistance = distance(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = STATE.ONE;
                break;
            default:
                break;
        }
        return true;
    }

    private void changeRadius(double gap) {
        if (Math.abs(gap) < 10) return;
        radius += gap / 10;
        invalidate();
    }

    private double initX;
    private double initY;

    private void changePosition(MotionEvent event) {
        double gapX = event.getX() - initX;
        double gapY = event.getY() - initY;
        double dis = Math.sqrt(gapX * gapX + gapY * gapY);
        if (dis < 5) return;
        x = event.getX();
        y = event.getY();
        invalidate();
    }

    private double distance(MotionEvent event) {
        float temp_x = event.getX(0) - event.getX(1);
        float temp_y = event.getY(0) - event.getY(1);
        return Math.sqrt(temp_x * temp_x + temp_y * temp_y);
    }
}
