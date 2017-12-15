# 三个点（或小球）缓冲控件示例
### 前言
&emsp;&emsp;之前看别人app上缓冲框的实现，觉得挺好的，就想实现下。本文实现的是三个动态点的缓冲框。

### 最终实现效果

![](https://github.com/yizhanzjz/ImageRepo/raw/master/dotsthree.gif)

&emsp;&emsp;左边是三个动态的点，右边是一段简单的说明文字。三个点的大小及透明度依次变化，且有规律性。

### 思路及实现
&emsp;&emsp;第一次看到这个效果就知道可以通过自定义控件实现：点通过画圆填充的方式实现，点大小的变化通过属性动画不断地修改圆半径实现，透明度通过属性动画不断地给画笔设置新的alpha值实现。总共需要画3个点和一个文本，现在先来看怎么画出一个点，一个大小及透明度不断变化的点。
#### &emsp;&emsp;怎么画出一个大小及透明度不断变化的点
&emsp;&emsp;android中画一个点，其实很简单
```
//初始化点的画笔
mPaint = new Paint();
//画笔模式这里设置成填充，因为画的是点，不是圆形
mPaint.setStyle(Paint.Style.FILL);
mPaint.setAntiAlias(true);//抗锯齿
mPaint.setColor(mDotColor);//设置画笔颜色，也就是点的颜色

//cx、cy为圆心坐标，radius为半径
canvas.drawCircle(cx, cy, radius, mPaint);
```
&emsp;&emsp;初始化一个画笔，画笔模式设置为填充，再调用一下canvas的drawCircle方法，指定圆心坐标及圆半径，圆就出来了。

&emsp;&emsp;点好画，那大小不断变化的点怎么画呢？大小不断变化就需要我们不断调整点的半径，而属性动画可以做到在一个值范围内（比如0~1）不断变化并且我们可以拿到这个变化的值
```
//初始化一个点的动画
mValueAnimator = ValueAnimator.ofFloat(0, 1);
//设置动画值变化的监听
mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        //此处的mCurrentValue就是我们拿到的变化值
        mCurrentValue = (float) animation.getAnimatedValue();
        invalidate();
    }
});
mValueAnimator.setInterpolator(null);
mValueAnimator.setDuration(mDuration);
//无限循环的动画才会不停地变化
valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
valueAnimator.setRepeatMode(ValueAnimator.RESTART);
```
&emsp;&emsp;ofFloat(0, 1)指定属性动画的值在0-1范围内变化，setInterpolator传入null意思是在0-1范围内线性变化，对属性动画进行监听得到的mCurrentValue就是值0-1线性变化时某一时刻的值。假设我们的点最小时的半径为minRadius，最大时的半径为maxRadius，那点半径的变化范围就是maxRadius - minRadius，据此我们计算出某一时刻点的半径：minRadius + (maxRadius - minRadius) * mCurrentValue。注意，此处的mCurrentValue范围为0-1。那上述对drawCircle的调用就可以修改成下面这样：
```
canvas.drawCircle(cx, cy, minRadius + (maxRadius - minRadius) * mCurrentValue, mPaint);
```
&emsp;&emsp;随着mCurrentValue在0-1之间线性变化，点的半径在minRadius-maxRadius之间线性变化，这样就画出了大小不断变化的点了。那怎么画出在大小线性变化的点的同时使此点的透明度也线性变化呢？只需在上述drawCircle上方给画笔mPaint设置透明度即可。如下：
```
mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * mCurrentValue + 0.5));
canvas.drawCircle(cx, cy, minRadius + (maxRadius - minRadius) * mCurrentValue, mPaint);
```
&emsp;&emsp;mPaint透明度的取值范围为0~255，这里0就是全透明了，我们没有必要让点最终变透明，所以这里给了一个点透明度的最小值mDotAlpha（详细代码里给的mDotAlpha为128）。255是完全不透明，上述的setAlpha方法可使点的透明度在mDotAlpha-255之间线性变化。

&emsp;&emsp;上面只是说了怎么完成点大小由小到大的变化，那怎么做到点大小由大到小的变化呢？其实有多种方式可以做到这一点，比如我可以把属性动画的ofFloat设置成
```
ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1, 0);
```
&emsp;&emsp;也可以维持ofFloat不变，在每执行第偶数次动画时执行
```
mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * (1 - mCurrentValue) + 0.5));
canvas.drawCircle(cx, cy, minRadius + (maxRadius - minRadius) * (1 - mCurrentValue), mPaint);
```
&emsp;&emsp;除了上面这两种方式，还可以像下面这样做
```
//定义动画的时候这么写
ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 2);

//画点的时候这么做
//当mCurrentValue在0-1之间变化时
if (mCurrentValue >= 0 && mCurrentValue < 1) {
    //0-1之间，不透明度逐渐地增大
    mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * mCurrentValue + 0.5));
    //0-1之间，点半径逐渐增大
    canvas.drawCircle(cx, cy, minRadius + (maxRadius - minRadius) * mCurrentValue, mPaint);
} else if (mCurrentValue >= 1 && mCurrentValue <= 2) {//当mCurrentValue在1-2之间变化时
    //1-2之间，不透明度逐渐变小
    mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * (1 - 0.5 * mCurrentValue) + 0.5));
    //1-2之间，点半径逐渐变小
    canvas.drawCircle(cx, cy, minRadius + (maxRadius - minRadius) * (1 - 0.5 * mCurrentValue), mPaint);
} else {
    //其他情况下的透明度就直接设置成点最小时的透明度和
    mPaint.setAlpha(mDotAlpha);
    canvas.drawCircle(cx, cy, minRadius, mPaint);
}
```
&emsp;&emsp;如上述代码所示，在mCurrentValue范围在0-1之间逐渐变大时，不透明度和点半径逐渐增大，在1-2范围内，不透明度和点半径逐渐减小。其实上述代码，还可以进一步简化，因为setAlpha和drawCircle调用的太多了：
```
float currentValue = 0;
if (mCurrentValue >= 0 && mCurrentValue < 1) {
    currentValue = mCurrentValue;
} else if (mCurrentValue >= 1 && mCurrentValue <= 2) {//当mCurrentValue在1-2之间变化时
    currentValue = 1 - 0.5 * mCurrentValue;
} else {
    currentValue = 0;
}

mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * currentValue + 0.5));
canvas.drawCircle(cx, cy, minRadius + (maxRadius - minRadius) * currentValue, mPaint);
```

&emsp;&emsp;根据上述思路编写代码实现的效果如下：

![](https://github.com/yizhanzjz/ImageRepo/raw/master/dotsone.gif)

#### &emsp;&emsp;怎么画出三个大小及透明度不断变化的点
&emsp;&emsp;上面已经画出了一个大小及透明度不断变化的点，那怎么画出3个呢？首先我们需要研究一下，3个点一起运动时的规律。如最终效果图所示，

- 初始时3个点均为半径最小且透明度最低的状态；
- 之后第一个点开始变化（半径逐渐变大，透明度逐渐加深），其余两点仍旧为半径最小且透明度最低状态；
- 当第一个点半径和透明度变化到最大变化范围的一半时，第二个点开始变化；
- 当第二个点半径和透明度变化到最大变化范围的一半时，第三个点开始变化；
- 第一个点半径和透明度变化到最大变化范围时，随即折返，半径和透明度逐渐变小，变化到最小范围时不再变化；
- 第二个点和第三个点的变化规律和第一个点的变化规律是一致的，只是靠前的点比其后紧跟的点早开始变化一半的时间。当第三个点变化到最小范围时，三个点均为半径最小且透明度最低的状态；
- 回到第一步，上述过程再走一遍

&emsp;&emsp;上述情况，说起来好像有点儿复杂，其实落实到具体的坐标图上，就是3个偏移的“^”，如下所示

![](https://github.com/yizhanzjz/ImageRepo/raw/master/dotsX.jpeg)

&emsp;&emsp;解释一下，上述图片中的横轴为mCurrentValue，纵轴表示点半径radius（透明度类似的）。最靠近纵轴的折线是第一个点的变化规律，中间那个折线是第二个点的变化规律，最右边的折线是第三个点的变化规律。可据此变化规律列出每条折线的方程：
```
//第一条折线的方程
raduis = minRadius + (maxRadius - minRadius) * mCurrentValue,(mCurrentValue >= 0 && mCurrentValue < 1);
raduis = minRadius + (maxRadius - minRadius) * (1 - 0.5f * mCurrentValue),(mCurrentValue >= 1 && mCurrentValue <= 2);

//第二条折线的方程
raduis = minRadius + (maxRadius - minRadius) * (mCurrentValue - 0.5),(mCurrentValue >= 0.5 && mCurrentValue < 1.5);
raduis = minRadius + (maxRadius - minRadius) * (1 - 0.5f * (mCurrentValue - 0.5f)),(mCurrentValue >= 1.5 && mCurrentValue <= 2);

//第三条折线
raduis = minRadius + (maxRadius - minRadius) * (mCurrentValue - 1f)),(mCurrentValue >= 1 && mCurrentValue < 2);
raduis = minRadius + (maxRadius - minRadius) * (1 - 0.5f * (mCurrentValue - 0.5f))),(mCurrentValue >= 2 && mCurrentValue <= 3);

```
&emsp;&emsp;列出这些方程，需要一定的数学知识。但也只是直线而已，列出方程也只会是一阶方程而已。结合上述方程，设置一个0-3范围的属性动画，就可以使用一个动画画出三个大小及透明度不断变化的点。在给出画出三个动态点代码之前，我们还需要解决另一个小问题，三个点应该在控件的中心位置。首先计算出三个点的总长度：
```
float dotsLength = 3 * (mMaxRadius * 2 + mSpace) - mSpace;
```
&emsp;&emsp;其中mSpace指的是两个点之间的间距。有了此总长度就可以计算出三个点的圆心位置为：
```
// i: 0、1、 2，分别代表最左边、中间、左右边的点的坐标
(dotsLength / 2 + (2 * i + 1) * mMaxRadius + i * mSpace,0)
```
&emsp;&emsp;画出3个变化的点的代码如下：
```
//属性动画设置成0-3
ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 3);

//将此view的坐标系原点放到控件的中心
canvas.translate(mWidth / 2, mHeight / 2);

//依次画出三个点
for (int i = 0; i < 3; i++) {

float currentValue = 0f;

switch (i) {
    case 0://画第一个点
        if (mCurrentValue >= 0 && mCurrentValue < 1) {
            currentValue = mCurrentValue;
        } else if (mCurrentValue >= 1 && mCurrentValue <= 2) {
            currentValue = 1 - 0.5f * mCurrentValue;
        } else {
            currentValue = 0;
        }

        mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * currentValue + 0.5));
        canvas.drawCircle(-dotsLength / 2 + (2 * i + 1) * mMaxRadius + i * mSpace, 0,
                mMinRadius + (mMaxRadius - mMinRadius) * currentValue, mPaint);

        break;
    case 1://画第二个点
        if (mCurrentValue >= 0.5 && mCurrentValue < 1.5) {
            currentValue = mCurrentValue - 0.5f;
        } else if (mCurrentValue >= 1.5 && mCurrentValue <= 2.5) {
            currentValue = 1 - 0.5f * (mCurrentValue - 0.5f);
        } else {
            currentValue = 0;
        }

        mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * currentValue + 0.5));
        canvas.drawCircle(-dotsLength / 2 + (2 * i + 1) * mMaxRadius + i * mSpace, 0,
                mMinRadius + (mMaxRadius - mMinRadius) * currentValue, mPaint);

        break;
    case 2://画第三个点
        if (mCurrentValue >= 1 && mCurrentValue < 2) {
            currentValue = mCurrentValue - 1f;
        } else if (mCurrentValue >= 2 && mCurrentValue <= 3) {
            currentValue = 1 - 0.5f * (mCurrentValue - 1f);
        } else {
            currentValue = 0;
        }

        mPaint.setAlpha((int) (mDotAlpha + (255 - mDotAlpha) * currentValue + 0.5));
        canvas.drawCircle(-dotsLength / 2 + (2 * i + 1) * mMaxRadius + i * mSpace, 0,
                mMinRadius + (mMaxRadius - mMinRadius) * currentValue, mPaint);

        break;
}
```
&emsp;&emsp;根据上述代码运行的结果如下：

![](https://github.com/yizhanzjz/ImageRepo/raw/master/dotstwo.gif)

#### &emsp;&emsp;画出3个变化的点右侧的文本
&emsp;&emsp;画文本的代码其实很简单
```
canvas.drawText(mText, x, y, mTextPaint);
```
&emsp;&emsp;但为了要把三个点以及文本看做一个整体然后居中放置，需要调整一下上述三个点圆心坐标的计算方法：
```
//3个点及其间距的总长度
float dotsLength = 3 * (mMaxRadius * 2 + mSpace) - mSpace;
//文本的长度
Rect rect = new Rect();
mTextPaint.getTextBounds(mText, 0, mText.length(), rect);
float rectLength = rect.right - rect.left;
//3个点及文本的总长度，mDivider为3个点与文本之间的间距
float length = dotsLength + rectLength + mDivider;

//i：0，1，2
圆心坐标：(-length / 2 + (2 * i + 1) * mMaxRadius + i * mSpace,0)
```
&emsp;&emsp;然后画出垂直居中的文本：
```
Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
canvas.drawText(mText, -length / 2 + dotsLength + mDivider, (-fontMetrics.top - fontMetrics.bottom) / 2, mTextPaint);
```
&emsp;&emsp;之前知道使一个文本在一个矩形范围内垂直居中的基线纵坐标为：baseY = (rectTop + rectBottom -fontMetrics.top - fontMetrics.bottom) / 2，因为此控件平移了坐标系，整个控件坐标系的原点位置移到了控件的中心，(rectTop + rectBottom) / 2其实等于0，所以这里直接写(-fontMetrics.top - fontMetrics.bottom) / 2就可以了。

### 总结
&emsp;&emsp;一开始刚写这个代码时，总觉得这3个点应该是三个动画。而真正使用3个动画分别控制这3个点时，却遇到了动画同时开启某个点动画出现延迟的问题。最终还是决定使用一个动画来完成，一个动画的值在变化期间使用不同的计算方法计算出各个点此刻的大小和不透明度，然后画出，最终解决问题。

