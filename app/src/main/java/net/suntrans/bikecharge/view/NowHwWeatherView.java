package net.suntrans.bikecharge.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import net.suntrans.bikecharge.utils.UiUtils;


/**
 * Created by ws on 2017/2/25.
 */

public class NowHwWeatherView extends View {

    private Paint mArcPaint;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mPointPaint;

    private float mWidth;
    private float mHeight;
    private float radius;//半径

    private int startAngle;//圆弧开始角
    private int sweepAngle;//圆弧总角度数
    private int count;//圆弧被分的份数


    private int maxValue;
    private int minValue;
    private Bitmap bitmap;
    private int ocAngle;//0度初始角
    private int fgAngle;//总覆盖的角
    //    private int offset;
    private int lineWidth = UiUtils.dip2px(10);
    private int timeMillis;
    private int arcOffset = UiUtils.dip2px(5);


    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        invalidate();
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        invalidate();
    }



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text = "开始检测";

    public NowHwWeatherView(Context context) {
        this(context, null);
    }

    public NowHwWeatherView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NowHwWeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        initPaint();

        startAngle = 0;
        sweepAngle = 360;
        count = 100;//刻度份数

        maxValue = 25;
        minValue = 0;
//        bitmap= BitmapFactory.decodeResource(context.getResources(), R.drawable.w16);
        ocAngle = 230;
        fgAngle = 90;
//        offset=22;

        timeMillis = 4000;

    }

    private void initPaint() {
        mArcPaint = new Paint();
        mArcPaint.setColor(Color.parseColor("#f1f1f1"));
        mArcPaint.setStrokeWidth(arcOffset+lineWidth);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStrokeWidth(4);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(144);

        mPointPaint = new Paint();
        mPointPaint.setColor(Color.WHITE);
        mPointPaint.setStrokeWidth(2);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setAntiAlias(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int wrap_Len = UiUtils.dip2px(232);
        int width = measureDimension(wrap_Len, widthMeasureSpec);
        int height = measureDimension(wrap_Len, heightMeasureSpec);
        int len = Math.min(width, height);
        //保证是一个正方形
        setMeasuredDimension(len, len);

    }

    public int measureDimension(int defaultSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize;   //UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        mHeight = getHeight();
        radius = (mWidth - getPaddingLeft() - getPaddingRight()-arcOffset*2) / 2;//半径
        canvas.translate(mWidth / 2, mHeight / 2);

//        drawArcView(canvas);//画圆环
        drawLine(canvas);//画短线
        drawTextBitmapView(canvas);//画中间的温度和下边的图片
//        drawTempLineView(canvas);//画动态温度

    }

//    private void drawTempLineView(Canvas canvas) {
//        mTextPaint.setTextSize(24);
//        //canvas.drawText("0°C",getRealCosX(ocAngle,offset,true),getRealSinY(ocAngle,offset,true),mTextPaint);//固定0度的位置
//
//        int startTempAngle=getStartAngle(minValue,maxValue);
//       /* if(startTempAngle<=startAngle){//如果开始角小于startAngle，防止过边界
//            startTempAngle=startAngle+10;
//        }else if((startTempAngle+fgAngle)>=(startAngle+sweepAngle)){//如果结束角大于(startAngle+sweepAngle)
//            startTempAngle =startAngle+sweepAngle-20-fgAngle;
//        }*/
//        canvas.drawText(minValue + "°", getRealCosX(startTempAngle, offset,true), getRealSinY(startTempAngle, offset,true), mTextPaint);
//        canvas.drawText(maxValue + "°", getRealCosX(startTempAngle+fgAngle, offset,true), getRealSinY(startTempAngle+fgAngle, offset,true), mTextPaint);
//
//        int circleAngle = startTempAngle+(currentTemp-minValue)*fgAngle/(maxValue-minValue);
//        mPointPaint.setColor(getRealColor(minValue,maxValue));
//        canvas.drawCircle(getRealCosX(circleAngle,50,false),getRealSinY(circleAngle,50,false),7,mPointPaint);
//    }

    private void drawArcView(Canvas canvas) {
        RectF mRect = new RectF(-radius-arcOffset, -radius-arcOffset, radius+arcOffset, radius+arcOffset);
        //canvas.drawRect(mRect,mArcPaint);
        canvas.drawArc(mRect, 0, 360, false, mArcPaint);

    }


    private void drawLine(Canvas canvas) {
        canvas.save();
        float angle = (float) sweepAngle / count;//刻度间隔
//        canvas.rotate(0);//将起始刻度点旋转到正上方
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        for (int i = 0; i <= count; i++) {
            if (minValue < maxValue) {
                if (i >= minValue && i <= maxValue) {
                    mLinePaint.setStrokeWidth(UiUtils.dip2px(2));
                    if (isStart) {
                        mLinePaint.setColor(Color.parseColor("#8470f6"));
                    } else {
                        mLinePaint.setColor(Color.parseColor("#e3e3e3"));
                    }
                    canvas.drawLine(0, -radius, 0, -radius + lineWidth, mLinePaint);

                } else {
                    mLinePaint.setStrokeWidth(UiUtils.dip2px(2));
                    mLinePaint.setColor(Color.parseColor("#e3e3e3"));
                    canvas.drawLine(0, -radius, 0, -radius + lineWidth, mLinePaint);
                }
            } else if (minValue > maxValue) {
                if (i >= minValue || i <= maxValue) {
                    mLinePaint.setStrokeWidth(UiUtils.dip2px(2));
                    if (isStart)
                        mLinePaint.setColor(Color.parseColor("#8470f6"));
                    else {
                        mLinePaint.setColor(Color.parseColor("#e3e3e3"));
                    }
                    canvas.drawLine(0, -radius, 0, -radius + lineWidth, mLinePaint);

                } else {
                    mLinePaint.setStrokeWidth(UiUtils.dip2px(2));
                    mLinePaint.setColor(Color.parseColor("#e3e3e3"));
                    canvas.drawLine(0, -radius, 0, -radius + lineWidth, mLinePaint);
                }
            }

            canvas.rotate(angle);//逆时针旋转
        }
        canvas.restore();
    }

    private void drawTextBitmapView(Canvas canvas) {
        mTextPaint.setTextSize(50);
        canvas.drawText(text, 0, 0 + getTextPaintOffset(mTextPaint), mTextPaint);
//        canvas.drawBitmap(bitmap,0-bitmap.getWidth()/2,radius-bitmap.getHeight()/2-30,null);

    }


    public float getTextPaintOffset(Paint paint) {
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        return -fontMetrics.descent + (fontMetrics.bottom - fontMetrics.top) / 2;
    }

    //根据当天温度范围获得扇形开始角。
    private int getStartAngle(int minTemp, int maxTemp) {
        int startFgAngle = 0;
        if (minTemp >= maxTemp) {
            Log.e("ws", "getStartAngle---?fail");
            return startFgAngle;
        }
        if (minTemp <= 0) {
            startFgAngle = ocAngle - (0 - minTemp) * fgAngle / (maxTemp - minTemp);
        } else {
            startFgAngle = ocAngle + (minTemp - 0) * fgAngle / (maxTemp - minTemp);
        }
        //边界 start
        if (startFgAngle <= startAngle) {//如果开始角小于startAngle，防止过边界
            startFgAngle = startAngle + 10;
        } else if ((startFgAngle + fgAngle) >= (startAngle + sweepAngle)) {//如果结束角大于(startAngle+sweepAngle)
            startFgAngle = startAngle + sweepAngle - 20 - fgAngle;
        }
        //边界 end
        return startFgAngle;
    }

    //根据当天温度范围获取开始短线的索引
    private int getStartLineIndex(int minTemp, int maxTemp) {
        return (getStartAngle(minTemp, maxTemp) - startAngle) / (sweepAngle / count);
    }

    private int getEndLineIndex(int minTemp, int maxTemp) {
        return (getStartAngle(minTemp, maxTemp) - startAngle) / (sweepAngle / count) + fgAngle / (sweepAngle / count);
    }

    //根据温度返回颜色值
    public int getRealColor(int minTemp, int maxTemp) {
        if (maxTemp <= 0) {
            return Color.parseColor("#00008B");//深海蓝
        } else if (minTemp <= 0 && maxTemp > 0) {
            return Color.parseColor("#4169E1");//黄君兰
        } else if (minTemp > 0 && minTemp < 15) {
            return Color.parseColor("#40E0D0");//宝石绿
        } else if (minTemp >= 15 && minTemp < 25) {
            return Color.parseColor("#00FF00");//酸橙绿
        } else if (minTemp >= 25 && minTemp < 30) {
            return Color.parseColor("#FFD700");//金色
        } else if (minTemp >= 30) {
            return Color.parseColor("#CD5C5C");//印度红
        }

        return Color.parseColor("#00FF00");//酸橙绿;
    }


    private float getIndexY(int index) {
        return (float) Math.sin((2 * Math.PI / count) * index);
    }

    private float getIndexX(int index) {
        return (float) Math.cos((2 * Math.PI / count) * index);
    }

    public void setBitmap(Bitmap mBitmap) {
        this.bitmap = mBitmap;
        invalidate();
    }

    private Runnable progressChangeTask = new Runnable() {


        @Override
        public void run() {
            removeCallbacks(this);
            maxValue += 1;
            minValue += 1;
            if (maxValue >= count) {
                maxValue = 0;
            }
            if (minValue >= count) {
                minValue = 0;
            }
//            Integer.MAX_VALUE
            invalidate();
            postDelayed(progressChangeTask, timeMillis / 100);
        }
    };

    /**
     * 开始。
     */
    public void startScan() {
        if (isStart) {
            stopScan();
        } else {
            isStart = true;
            text = "停止检测";

            post(progressChangeTask);
        }
    }

    /**
     * 停止。
     */
    public void stopScan() {
        if (isStart) {
            text = "开始检测";
            isStart = false;
            removeCallbacks(progressChangeTask);
            invalidate();
        }
    }


    private boolean isStart = false;
}