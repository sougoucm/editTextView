package com.xiaodao.edittextlabel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by dsj
 * 包含了一个EditText和一个TextView作为标签 ,获取焦点是标签向上移动
 */
public class EditTextLabel extends FrameLayout {

    private Bundle mSavedState;
    private static final String SAVE_STATE_KEY_EDIT_TEXT = "saveStateEditText";
    private static final String SAVE_STATE_KEY_LABEL = "saveStateLabel";
    private static final String SAVE_STATE_PARENT = "saveStateParent";
    private static final String SAVE_STATE_TAG = "saveStateTag";
    private static final String SAVE_STATE_KEY_FOCUS = "saveStateFocus";

    /**
     * 聚焦时的颜色
     */
    private int onFocusColor = Color.parseColor("#000000");
    /**
     * 失去焦点时的颜色
     */
    private int onUnFocusColor = Color.parseColor("#999999");
    private int mTextSize = dpToPx(12);
    /**
     * TextView标签的引用
     */
    private TextView mLabel;
    /**
     * EditText的引用
     */
    private EditText mEditText;
    /**
     * 标签是否正在顶部
     */
    private boolean isTopLabel;
    /**
     * 默认动画
     */
    private LabelAnimator mLabelAnimator = new DefaultLabelAnimator();
    private ImageView mDel;

    // 设置聚焦时的颜色
    public void setOnFocusColor(int onFocusColor) {
        this.onFocusColor = onFocusColor;
    }

    // 设置失去聚焦时的颜色
    public void setOnUnFocusColor(int onUnFocusColor) {
        this.onUnFocusColor = onUnFocusColor;
    }

    //EditText的getter
    public EditText getEditText() {
        return mEditText;
    }

    //TextView标签的getter
    public TextView getLabelTextView() {
        return mLabel;
    }

    //可以设置自定义动画
    public void setLabelAnimator(LabelAnimator labelAnimator) {
        if (labelAnimator == null) {
            mLabelAnimator = new DefaultLabelAnimator();
        } else {
            mLabelAnimator = labelAnimator;
        }
    }

    public EditTextLabel(Context context) {
        this(context, null);
    }

    public EditTextLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditTextLabel);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.EditTextLabel_etl_text_size, mTextSize);
        onUnFocusColor = typedArray.getColor(R.styleable.EditTextLabel_etl_text_color_normal, onUnFocusColor);
        onFocusColor = typedArray.getColor(R.styleable.EditTextLabel_etl_text_color_selected, onFocusColor);
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {

        View rootView = inflate(context, R.layout.libui_edittextlabel, this);
        mEditText = (EditText) findViewById(R.id.libui_edittext_edittext);
        mLabel = (TextView) findViewById(R.id.libui_edittext_label);
        mDel = (ImageView) findViewById(R.id.libui_edittext_del);

        mEditText.setTextSize(mTextSize + 4);
        mLabel.setTextSize(mTextSize);

        // Check current state of EditText
        if (mEditText.getText().length() == 0) {
            isTopLabel = false;
        } else {
            isTopLabel = true;
        }
        mLabel.setTextColor(onUnFocusColor);
        mLabel.setY(mLabel.getTextSize());
        mDel.setY(mLabel.getTextSize() + mLabel.getTextSize() / 2 - dpToPx(10) / 2);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mEditText.getLayoutParams();
        params.setMargins(0, (int) (-mLabel.getTextSize() * 0.45), 0, 0);//2是paddingTop的距离

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mLabel.setTextColor(onFocusColor);
                    if (!isTopLabel) {
                        mLabelAnimator.onTopLabel(mLabel);
                        isTopLabel = true;
                    }
                } else {
                    mLabel.setTextColor(onUnFocusColor);
                    if (isTopLabel) {
                        if (mEditText.getText().length() == 0) {
                            mLabelAnimator.onResetLabel(mLabel);
                            isTopLabel = false;
                        }
                    }
                }
            }
        });

        mEditText.addTextChangedListener(new EditTextWatcher());
        mDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("");
                mEditText.requestFocus();
            }
        });
    }

    /**
     * 定义标签显示和消失的动画接口
     */
    public interface LabelAnimator {
        void onTopLabel(View label);

        void onResetLabel(View label);
    }

    /**
     * 标签显示和消失的默认动画实现
     */
    private class DefaultLabelAnimator implements LabelAnimator {

        @Override
        public void onTopLabel(View label) {
            final float offset = ((TextView) label).getTextSize();
            final float currentY = label.getY();
            if (currentY != offset) {
                label.setY(offset);
            }
            label.animate().y(0).scaleX(0.8f).scaleY(0.8f);//2是paddingTop的距离
        }

        @Override
        public void onResetLabel(View label) {
            final float offset = ((TextView) label).getTextSize();
            final float currentY = label.getY();
            if (currentY != 0) {
                label.setY(0);//2是paddingTop的距离
            }
            label.animate().y(offset).scaleX(1).scaleY(1);
        }
    }

    private class EditTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() != 0) {
                if (!isTopLabel) {
                    mLabel.setY(0);
                    mLabel.setScaleX(0.8f);
                    mLabel.setScaleY(0.8f);
                    isTopLabel = true;
                }
                mDel.setVisibility(VISIBLE);
            }else {
                mDel.setVisibility(GONE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Ignored
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Ignored
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(mEditText, widthMeasureSpec, heightMeasureSpec);
        measureChild(mLabel, widthMeasureSpec, heightMeasureSpec);
        measureChild(mDel, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureHeight(int heightMeasureSpec) {
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        int result = 0;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = mEditText.getMeasuredHeight() + mLabel.getMeasuredHeight();
            result += getPaddingTop() + getPaddingBottom();
            result = Math.max(result, getSuggestedMinimumHeight());

            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    private int measureWidth(int widthMeasureSpec) {
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        int result = 0;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = Math.max(mEditText.getMeasuredWidth(), mLabel.getMeasuredWidth());
            result = Math.max(mDel.getMeasuredWidth(), result);
            result = Math.max(result, getSuggestedMinimumWidth());
            result += getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int childLeft = getPaddingLeft();
        final int childRight = right - left - getPaddingRight();

        int childTop = getPaddingTop();
        final int childBottom = bottom - top - getPaddingBottom();

        layoutChild(mLabel, childLeft, childTop, childRight, childBottom);
        layoutChild(mEditText, childLeft, childTop + mLabel.getMeasuredHeight(), childRight, childBottom);
        layoutChild(mDel, childLeft, childTop, childRight, childBottom);
    }

    private void layoutChild(View child, int parentLeft, int parentTop, int parentRight, int parentBottom) {
        if (child.getVisibility() != GONE) {
            final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            int childLeft;
            final int childTop = parentTop + lp.topMargin;


            if (child.getId() == mDel.getId()) {
                childLeft = parentLeft + lp.leftMargin + mEditText.getMeasuredWidth() - width - 10;
            } else {
                childLeft = parentLeft + lp.leftMargin;
            }
            Log.d("layoutChild", child.getId() + "位置：" + childLeft + ";" + childTop + ";" + (childLeft + width) + ";" + (childTop + height));
            child.layout(childLeft, childTop, childLeft + width, childTop + height);
        }
    }


    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle savedState = (Bundle) state;
            if (savedState.getBoolean(SAVE_STATE_TAG, false)) {
                mSavedState = savedState;
                String inputValue = savedState.getString(SAVE_STATE_KEY_EDIT_TEXT);
                if(!TextUtils.isEmpty(inputValue)){
                    mEditText.setText(inputValue);
                }
                super.onRestoreInstanceState(savedState.getParcelable(SAVE_STATE_PARENT));
                return;
            }
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final Bundle saveState = new Bundle();
        saveState.putString(SAVE_STATE_KEY_EDIT_TEXT, mEditText.getText().toString());
        saveState.putParcelable(SAVE_STATE_KEY_LABEL, mLabel.onSaveInstanceState());
        saveState.putBoolean(SAVE_STATE_KEY_FOCUS, mEditText.isFocused());
        saveState.putBoolean(SAVE_STATE_TAG, true);
        saveState.putParcelable(SAVE_STATE_PARENT, superState);

        return saveState;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}
