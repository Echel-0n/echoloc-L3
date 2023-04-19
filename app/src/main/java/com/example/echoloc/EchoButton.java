package com.example.echoloc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Bouton personnalisé
 */
public class EchoButton extends ConstraintLayout {
    // Attributes
    private final RelativeLayout background;
    private final LinearLayout text_layout;
    private final TextView text;
    private final ImageView click_effect_view;
    private Drawable click_effect;

    private int background_color_selected,
            background_color_unselected,
            stroke_color_selected,
            stroke_color_unselected,
            text_color_selected,
            text_color_unselected,
            padding_top,
            padding_bottom,
            padding_left,
            padding_right,
            stroke_width,
            animation_speed;
    private String text_inner;
    private float radius,
            text_size;
    private boolean selected,
            selectable;

    private ViewPropertyAnimator text_animation;
    private Runnable onSelectedChanged;

    // Constructors

    /**
     * Constructeur si non-instancier par le fichier layout
     * @param context Activité dans laquelle le bouton sera utilisé
     */
    public EchoButton(Context context){
        super(context);

        View.inflate(getContext(), R.layout.echo_button_type,this);

        // Element composant la vue
        background = findViewById(R.id.echoButtonLayout);
        text_layout = findViewById(R.id.echoButtonTextLayout);
        text = findViewById(R.id.echoButtonText);
        click_effect_view = findViewById(R.id.echoButtonClickEffect);

        // Default values
        background_color_selected = Color.TRANSPARENT;
        background_color_unselected = Color.TRANSPARENT;
        stroke_color_selected = Color.TRANSPARENT;
        stroke_color_unselected = Color.TRANSPARENT;
        text_color_selected = Color.TRANSPARENT;
        text_color_unselected = Color.TRANSPARENT;
        radius = 0;
        stroke_width = 0;
        text_inner = "";
        text_size = 0;
        padding_top = 0;
        padding_bottom = 0;
        padding_left = 0;
        padding_right = 0;
        animation_speed = 150;
        selected = false;
        selectable = true;
        setClickable(true);

        // Application des variables
        apply();
        this.post(this::verifyIfAnimationNeeded);
    }

    /**
     * Constructeur si instancier par le fichier layout
     * @param context Activité dans laquelle le bouton sera utilisé
     */
    public EchoButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        setClickable(true);

        View.inflate(getContext(), R.layout.echo_button_type,this);

        // Element composant la vue
        background = findViewById(R.id.echoButtonLayout);
        text_layout = findViewById(R.id.echoButtonTextLayout);
        text = findViewById(R.id.echoButtonText);
        click_effect_view = findViewById(R.id.echoButtonClickEffect);

        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.EchoButton, 0, 0);
        try {
            // Récupération des variables
            int background_color = a
                    .getColor(R.styleable.EchoButton_android_colorBackground, Color.TRANSPARENT);
            int bcs = a
                    .getColor(R.styleable.EchoButton_colorBackgroundSelected, 1);
            int bcu = a
                    .getColor(R.styleable.EchoButton_colorBackgroundUnselected, 1);
            if (bcs != 1){
                background_color_selected = bcs;
            } else {
                background_color_selected = background_color;
            }
            if (bcu != 1){
                background_color_unselected = bcu;
            } else {
                background_color_unselected = background_color;
            }

            int stroke_color = a
                    .getColor(R.styleable.EchoButton_android_strokeColor, Color.TRANSPARENT);
            int scs = a
                    .getColor(R.styleable.EchoButton_strokeColorSelected, 1);
            int scu = a
                    .getColor(R.styleable.EchoButton_strokeColorUnselected, 1);
            if (scs != 1){
                stroke_color_selected = scs;
            } else {
                stroke_color_selected = stroke_color;
            }
            if (scu != 1){
                stroke_color_unselected = scu;
            } else {
                stroke_color_unselected = stroke_color;
            }

            int text_color = a
                    .getColor(R.styleable.EchoButton_android_textColor, Color.TRANSPARENT);
            int tcs = a
                    .getColor(R.styleable.EchoButton_textColorSelected, 1);
            int tcu = a
                    .getColor(R.styleable.EchoButton_textColorUnselected, 1);
            if (tcs != 1){
                text_color_selected = tcs;
            } else {
                text_color_selected = text_color;
            }
            if (tcu != 1){
                text_color_unselected = tcu;
            } else {
                text_color_unselected = text_color;
            }

            radius = a
                    .getDimensionPixelSize(R.styleable.EchoButton_android_radius, 0);
            stroke_width = a
                    .getDimensionPixelSize(R.styleable.EchoButton_strokeWidth, 0);
            CharSequence t = a
                    .getText(R.styleable.EchoButton_android_text);
            if (t != null){
                text_inner = t.toString();
            } else {
                text_inner = "";
            }
            text_size = a
                    .getDimensionPixelSize(R.styleable.EchoButton_android_textSize, 0);

            int p = a
                    .getDimensionPixelSize(R.styleable.EchoButton_paddingInner, 0);
            padding_top = p;
            padding_bottom = p;
            padding_left = p;
            padding_right = p;
            int pt, pb, pl, pr;
            pt = a.getDimensionPixelSize(R.styleable.EchoButton_paddingTopInner, -1);
            pb = a.getDimensionPixelSize(R.styleable.EchoButton_paddingBottomInner, -1);
            pl = a.getDimensionPixelSize(R.styleable.EchoButton_paddingLeftInner, -1);
            pr = a.getDimensionPixelSize(R.styleable.EchoButton_paddingRightInner, -1);
            if (pt != -1) { padding_top = pt; }
            if (pb != -1) { padding_bottom = pb; }
            if (pl != -1) { padding_left = pl; }
            if (pr != -1) { padding_right = pr; }

            animation_speed = a
                    .getDimensionPixelSize(R.styleable.EchoButton_textAnimationPerSecond, 150);

            selected = a.getBoolean(R.styleable.EchoButton_android_state_selected, false);
            selectable = a.getBoolean(R.styleable.EchoButton_android_selectable, true);
            setClickable(a.getBoolean(R.styleable.EchoButton_android_clickable, true));

            // Application des variables
            apply();
            this.post(this::verifyIfAnimationNeeded);

        } finally {
            a.recycle();
        }
    }

    // Event Listeners

    private boolean is_actually_touched;

    @Override @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                clickEffect(e.getX(), e.getY());
                is_actually_touched = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (e.getX() < 0 || e.getX() > background.getWidth()
                || e.getY() < 0 || e.getY() > background.getHeight()){ // Si en dehors du boutton
                    stopClickEffect();
                    is_actually_touched = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                stopClickEffect();
                is_actually_touched = false;
                break;
            case MotionEvent.ACTION_UP:
                stopClickEffect();
                if (is_actually_touched) {
                    if (isClickable()) {
                        performClick();
                        onSelectedChange();
                    }
                }
                is_actually_touched = false;
                break;
        }
        return true;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        apply();
        this.post(this::verifyIfAnimationNeeded);
    }

    // Methods

    /**
     * Changement du style en fonction de si le bouton est selectionner
     */
    private void apply(){
        background.setBackground(getBackgroundDrawable(selected));
        int border = stroke_width;
        background.setPadding( border, border, border, border);
        text.setText(text_inner);
        text.setTextSize(text_size);
        text.setTextColor(getTextColor(selected));
        text_layout.setPadding(
                padding_left, padding_top,
                padding_right, padding_bottom);
        click_effect = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                Paint p = new Paint();
                if (selected) {
                    p.setColor(background_color_unselected);
                } else {
                    p.setColor(background_color_selected);
                }
                p.setAlpha(255/3);
                canvas.drawCircle((float)getBounds().width() / 2 , (float)getBounds().height() / 2, (float)getBounds().width()/2, p);
            }
            @Override public void setAlpha(int alpha) {}
            @Override public void setColorFilter(@Nullable ColorFilter colorFilter) {}
            @Override public int getOpacity() {return PixelFormat.OPAQUE;}
        };
        click_effect_view.setImageDrawable(click_effect);
    }

    private void onSelectedChange(){
        if (this.selectable){
            selected = !selected;
            apply();
            if (onSelectedChanged != null){
                onSelectedChanged.run();
            }
        }
    }

    // Effet de click

    private ViewPropertyAnimator click_effect_animation;

    private void stopClickEffect(){
        click_effect_view.setScaleX(0);
        click_effect_view.setScaleY(0);
        if (click_effect_animation != null) {
            click_effect_animation.cancel();
        }
    }
    private void clickEffect(float X, float Y){
        if (click_effect_animation != null) {
            click_effect_animation.cancel();
        }

        click_effect_view.setScaleX(1);
        click_effect_view.setScaleY(1);
        float bW = background.getWidth();
        float bH = background.getHeight();
        click_effect_view.setImageDrawable(click_effect);
        click_effect_view.setTranslationX(X);
        click_effect_view.setTranslationY(Y);

        float[] max = {X, Y, bW-X, bH-Y};
        float scale = X;
        for (float f : max){
            if (f > scale){
                scale = f;
            }
        }

        click_effect_animation =
                click_effect_view.animate()
                    .scaleX(scale*3)
                    .scaleY(scale*3)
                    .setDuration(250);
    }

    // Fonction diverse

    public int getTextColor(boolean sel) {
        if (sel) {
            return text_color_selected;
        } else {
            return text_color_unselected;
        }
    }

    private void verifyIfAnimationNeeded(){
        int lw = text_layout.getMeasuredWidth()
                -text_layout.getPaddingLeft()
                -text_layout.getPaddingRight();
        int tw = text.getMeasuredWidth();

        if (text_animation != null){
            text_animation.cancel();
        }
        text.setTranslationX(0);

        if (tw <= lw) { // Si texte plus petit que sont container, le centrer
            text_layout.setGravity(Gravity.CENTER);
        } else { // Sinon, lancer en boucle l'animation pour le voir
            text_layout.setGravity(Gravity.START);
            viewAnimateLeftToRightLoop();
        }
    }
    private void viewAnimateLeftToRightLoop(){ // Pour le texte trop grand
        int pwp = text_layout.getMeasuredWidth()-text_layout.getPaddingLeft()-text_layout.getPaddingRight();
        int vw = text.getMeasuredWidth();
        int anim_pause_duration = 400;
        if (vw-pwp > 0) {
            text_animation = text.animate()
                    .setStartDelay(anim_pause_duration)
                    .translationX(-(vw - pwp))
                    .setDuration((vw - pwp) * 1000L / animation_speed) // ... px/sec
                    .setInterpolator(v -> v)
                    .withEndAction(() -> text.animate().setDuration(anim_pause_duration) // Don't move during ...
                            .translationX(-(vw - pwp))
                            .withEndAction(() -> { // And restart
                                text.setTranslationX(0);
                                verifyIfAnimationNeeded();
                            }));
        }
    }

    private Drawable getBackgroundDrawable(boolean sel){
        return new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                float cH = getBounds().height();
                float cW = getBounds().width();

                int bw2 = stroke_width/2;
                RectF r = new RectF(bw2, bw2,cW-bw2,cH-bw2);
                Paint pStroke = new Paint();
                Paint pFill = new Paint();
                if (sel) {
                    pStroke.setStyle(Paint.Style.STROKE);
                    pStroke.setAntiAlias(true);
                    pStroke.setStrokeWidth(stroke_width);
                    pStroke.setColor(stroke_color_selected);

                    pFill.setStyle(Paint.Style.FILL);
                    pFill.setColor(background_color_selected);
                } else {
                    pStroke.setStyle(Paint.Style.STROKE);
                    pStroke.setAntiAlias(true);
                    pStroke.setStrokeWidth(stroke_width);
                    pStroke.setColor(stroke_color_unselected);

                    pFill.setStyle(Paint.Style.FILL);
                    pFill.setColor(background_color_unselected);
                }

                canvas.drawRoundRect(r, radius, radius, pFill); // Ne pas inverser
                canvas.drawRoundRect(r, radius, radius, pStroke);
            }

            @Override
            public void setAlpha(int alpha) { }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) { }

            @Override
            public int getOpacity() {
                return PixelFormat.UNKNOWN;
            }
        };
    }

    // Getters - Setters

    public int getBackgroundColorSelected() {
        return background_color_selected;
    }

    public void setBackgroundColorSelected(int background_color_selected) {
        this.background_color_selected = background_color_selected;
        apply();
    }

    public int getBackgroundColorUnselected() {
        return background_color_unselected;
    }

    public void setBackgroundColorUnselected(int background_color_unselected) {
        this.background_color_unselected = background_color_unselected;
        apply();
    }

    public int getStokeColorSelected() {
        return stroke_color_selected;
    }

    public void setStokeColorSelected(int stroke_color_selected) {
        this.stroke_color_selected = stroke_color_selected;
        apply();
    }

    public int getStokeColorUnselected() {
        return stroke_color_unselected;
    }

    public void setStokeColorUnselected(int stroke_color_unselected) {
        this.stroke_color_unselected = stroke_color_unselected;
        apply();
    }

    public int getTextColorSelected() {
        return text_color_selected;
    }

    public void setTextColorSelected(int text_color_selected) {
        this.text_color_selected = text_color_selected;
        apply();
    }

    public int getTextColorUnselected() {
        return text_color_unselected;
    }

    public void setTextColorUnselected(int text_color_unselected) {
        this.text_color_unselected = text_color_unselected;
        apply();
    }

    public String getText() {
        return text_inner;
    }

    public void setText(String text_inner) {
        this.text_inner = text_inner;
        apply();
    }

    public float getTextSize(){
        return text_size;
    }

    public void setTextSize(float text_size){
        this.text_size = text_size;
        apply();
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        apply();
    }

    public void setInnerPadding(int padding_left, int padding_top, int padding_right, int padding_bottom) {
        this.padding_left = padding_left;
        this.padding_top = padding_top;
        this.padding_right = padding_right;
        this.padding_bottom = padding_bottom;
        apply();
    }
    public void setInnerPadding(int padding) {
        setInnerPadding(padding, padding, padding, padding);
        apply();
    }

    public int getInnerPaddingTop() {
        return padding_top;
    }

    public void setInnerPaddingTop(int padding_top) {
        this.padding_top = padding_top;
        apply();
    }

    public int getInnerPaddingBottom() {
        return padding_bottom;
    }

    public void setInnerPaddingBottom(int padding_bottom) {
        this.padding_bottom = padding_bottom;
        apply();
    }

    public int getInnerPaddingLeft() {
        return padding_left;
    }

    public void setInnerPaddingLeft(int padding_left) {
        this.padding_left = padding_left;
        apply();
    }

    public int getInnerPaddingRight() {
        return padding_right;
    }

    public void setInnerPaddingRight(int padding_right) {
        this.padding_right = padding_right;
        apply();
    }

    public float getStrokeWidth() {
        return stroke_width;
    }

    public void setStrokeWidth(int stroke_width) {
        this.stroke_width = stroke_width;
        apply();
    }

    public boolean isButtonSelected() {
        return selected;
    }

    public void setButtonSelected(boolean selected) {
        this.selected = selected;
        apply();
    }

    public boolean isButtonSelectable() {
        return this.selectable;
    }

    public void setButtonSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public int getAnimationSpeed() {
        return this.animation_speed;
    }

    public void setAnimationSpeed(int pxPerSec){
        this.animation_speed = pxPerSec;
    }

    public void setOnSelectedChanged(Runnable runnable){
        this.onSelectedChanged = runnable;
    }

    public Runnable getOnSelectedChanged(){
        return this.onSelectedChanged;
    }
    public void clearOnSelectedChanged(){
        this.onSelectedChanged = null;
    }
}
