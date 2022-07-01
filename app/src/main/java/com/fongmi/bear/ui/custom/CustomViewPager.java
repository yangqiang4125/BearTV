package com.fongmi.bear.ui.custom;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.fongmi.bear.R;

public class CustomViewPager extends ViewPager {

    private Animation shake;
    private Rect rect;

    public CustomViewPager(@NonNull Context context) {
        super(context);
    }

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rect = new Rect();
        shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
    }

    public boolean executeKeyEvent(@NonNull KeyEvent event) {
        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    handled = arrowScroll(FOCUS_LEFT);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    handled = arrowScroll(FOCUS_RIGHT);
                    break;
            }
        }
        return handled;
    }

    public boolean arrowScroll(int direction) {
        boolean handled = false;
        View currentFocused = findFocus();
        if (currentFocused == this) {
            currentFocused = null;
        } else if (currentFocused != null) {
            boolean isChild = false;
            for (ViewParent parent = currentFocused.getParent(); parent instanceof ViewGroup;
                 parent = parent.getParent()) {
                if (parent == this) {
                    isChild = true;
                    break;
                }
            }
            if (!isChild) {
                currentFocused = null;
            }
        }
        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
        if (nextFocused != null && nextFocused != currentFocused) {
            if (direction == View.FOCUS_LEFT) {
                final int nextLeft = getChildRectInPagerCoordinates(rect, nextFocused).left;
                final int currLeft = getChildRectInPagerCoordinates(rect, currentFocused).left;
                if (currentFocused != null && nextLeft >= currLeft) {
                    handled = pageLeft();
                } else {
                    handled = nextFocused.requestFocus();
                }
            } else if (direction == View.FOCUS_RIGHT) {
                final int nextLeft = getChildRectInPagerCoordinates(rect, nextFocused).left;
                final int currLeft = getChildRectInPagerCoordinates(rect, currentFocused).left;
                if (currentFocused != null && nextLeft <= currLeft) {
                    handled = pageRight();
                } else {
                    handled = nextFocused.requestFocus();
                }
            }
        } else if (direction == FOCUS_LEFT) {
            if (getCurrentItem() == 0) {
                shakeX(currentFocused);
                handled = true;
            } else {
                handled = pageLeft();
            }
        } else if (direction == FOCUS_RIGHT) {
            if (getAdapter() != null && getCurrentItem() == getAdapter().getCount() - 1) {
                shakeX(currentFocused);
                handled = true;
            } else {
                handled = pageRight();
            }
        }
        if (handled) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
        }
        return handled;
    }

    private Rect getChildRectInPagerCoordinates(Rect outRect, View child) {
        if (outRect == null) {
            outRect = new Rect();
        }
        if (child == null) {
            outRect.set(0, 0, 0, 0);
            return outRect;
        }
        outRect.left = child.getLeft();
        outRect.right = child.getRight();
        outRect.top = child.getTop();
        outRect.bottom = child.getBottom();
        ViewParent parent = child.getParent();
        while (parent instanceof ViewGroup && parent != this) {
            final ViewGroup group = (ViewGroup) parent;
            outRect.left += group.getLeft();
            outRect.right += group.getRight();
            outRect.top += group.getTop();
            outRect.bottom += group.getBottom();
            parent = group.getParent();
        }
        return outRect;
    }

    boolean pageLeft() {
        if (getCurrentItem() > 0) {
            setCurrentItem(getCurrentItem() - 1, true);
            return true;
        }
        return false;
    }

    boolean pageRight() {
        if (getAdapter() != null && getCurrentItem() < (getAdapter().getCount() - 1)) {
            setCurrentItem(getCurrentItem() + 1, true);
            return true;
        }
        return false;
    }

    private void shakeX(View currentFocused) {
        if (currentFocused != null) {
            currentFocused.clearAnimation();
            currentFocused.startAnimation(shake);
        }
    }
}
