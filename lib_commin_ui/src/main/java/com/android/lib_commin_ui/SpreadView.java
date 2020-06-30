package com.android.lib_commin_ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


//自定义view
public class SpreadView extends View {

    private Paint centerPaint; //中心圆paint
    private int radius = 100; //中心圆半径
    private Paint spreadPaint; //扩散圆paint
    private float centerX;//圆心x
    private float centerY;//圆心y
    private int distance = 5; //每次圆递增间距
    private int maxRadius = 80; //最大圆半径
    private int delayMilliseconds = 33;//扩散延迟间隔，越大扩散越慢
    private List<Integer> spreadRadius = new ArrayList<>();//扩散圆层级数，元素为扩散的距离
    private List<Integer> alphas = new ArrayList<>();//对应每层圆的透明度

    public SpreadView(Context context) {
        this(context, null, 0);
    }

    public SpreadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    //使用自定义属性初始化SpreadView
    public SpreadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //从attrs文件中获取与spreadView相关的属性数组
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpreadView, defStyleAttr, 0);
        //获取中心圆半径 默认值为radius
        radius = a.getInt(R.styleable.SpreadView_spread_radius, radius);
        //扩散最大半径
        maxRadius = a.getInt(R.styleable.SpreadView_spread_max_radius, maxRadius);
        //中心圆颜色 默认红色
        int centerColor = a.getColor(R.styleable.SpreadView_spread_center_color,
                ContextCompat.getColor(context, android.R.color.holo_red_dark));
        //扩散圆颜色 默认红色
        int spreadColor = a.getColor(R.styleable.SpreadView_spread_spread_color,
                ContextCompat.getColor(context, R.color.color_F71816));
        //扩散间距
        distance = a.getInt(R.styleable.SpreadView_spread_distance, distance);
        //a数组获得值 并回收
        a.recycle();

        //初始化圆画笔
        centerPaint = new Paint();
        centerPaint.setColor(centerColor);
        //画圆打开抗锯齿（使得边界模糊）
        centerPaint.setAntiAlias(true);
        //最开始不透明且扩散距离为0
        alphas.add(255);
        spreadRadius.add(0);

        //初始化扩散圆画笔
        spreadPaint = new Paint();
        spreadPaint.setAntiAlias(true);
        spreadPaint.setStyle(Paint.Style.STROKE);//空心圆
        spreadPaint.setAlpha(255);//最开始不透明
        spreadPaint.setColor(spreadColor);
    }

    //这个方法回调时View的宽高已经获取
    //确定圆形位置
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //圆心位置 view的中心
        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制所有扩散圆
        for (int i = 0; i < spreadRadius.size(); i++) {
            int alpha = alphas.get(i); //取出对应透明度
            spreadPaint.setAlpha(alpha); //更新画笔透明度
            int width = spreadRadius.get(i); //获取对应的圆扩散半径
            //绘制扩散的圆 canvas画布
            canvas.drawCircle(centerX, centerY, radius + width, spreadPaint);

            //每次扩散圆半径递增，圆透明度递减 在范围内 两个list加入新增扩散圆属性
            if (alpha > 0 && width < 300) {
                //透明度减小
                alpha = alpha - distance > 0 ? alpha - distance : 1;
                alphas.set(i, alpha); //设置透明度数组
                spreadRadius.set(i, width + distance); //设置扩散半径数组
            }
        }
        //当最内层扩散圆半径达到最大半径时添加新扩散圆
        if (spreadRadius.get(spreadRadius.size() - 1) > maxRadius) {
            //重置 加入初始属性
            spreadRadius.add(0);
            alphas.add(255);
        }
        //超过8个扩散圆，删除最先绘制的圆，即最外层的圆 此时扩散到最外 透明度最高
        if (spreadRadius.size() >= 8) {
            alphas.remove(0);
            spreadRadius.remove(0);
        }
        //中间的圆 不参与扩散
        canvas.drawCircle(centerX, centerY, radius, centerPaint);

        //延迟更新，达到扩散视觉差效果 不停刷新
        postInvalidateDelayed(delayMilliseconds);
    }
}

