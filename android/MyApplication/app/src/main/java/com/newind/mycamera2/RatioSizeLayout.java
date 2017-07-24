package com.newind.mycamera2;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class RatioSizeLayout extends FrameLayout {
	float mWHRatio = -1.0f;
	boolean mStaticWidth = true;

	public RatioSizeLayout(Context context) {
		super(context);
		init(context, null);
	}

	public RatioSizeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public RatioSizeLayout(Context context, AttributeSet attrs,
                                int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public RatioSizeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.RatioSize);
		mStaticWidth = a.getBoolean(R.styleable.RatioSize_rsFixWidth, true);
		mWHRatio = a.getFloat(R.styleable.RatioSize_rsScaleRatio, -1);
		a.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mWHRatio < 0) {
			return;
		}
		int wRes = getMeasuredWidth();
		int hRes = getMeasuredHeight();
		if (mStaticWidth) {
			hRes = (int) (wRes / mWHRatio);
		} else {
			wRes = (int) (hRes * mWHRatio);
		}

		int newSpecWidth = MeasureSpec.makeMeasureSpec(wRes,
				MeasureSpec.EXACTLY);
		int newSpecHeight = MeasureSpec.makeMeasureSpec(hRes,
				MeasureSpec.EXACTLY);
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			measureChild(getChildAt(i), newSpecWidth, newSpecHeight);
		}

		setMeasuredDimension(newSpecWidth, newSpecHeight);
	}

	public void resetRatio(float w2hRatio){
		//这里预留了一点误差,避免过于频繁的切换宽高比.
		if (Math.abs(mWHRatio - w2hRatio) < 0.05) {
            return;
        }
		mWHRatio = w2hRatio;
		requestLayout();
	}
}
