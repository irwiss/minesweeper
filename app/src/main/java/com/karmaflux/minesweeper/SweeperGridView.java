package com.karmaflux.minesweeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class SweeperGridView extends View implements View.OnLongClickListener, View.OnClickListener {
    public interface IGameOverHandler {
        void Win();

        void Lose();
    }

    public static final int[] nearbyBombColors = new int[]{
            Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.MAGENTA,
            Color.YELLOW, Color.BLACK, Color.DKGRAY, Color.RED,
    };

    private final Paint[] bombPaints = new Paint[nearbyBombColors.length];
    private final Map<CellState, Bitmap> bitmaps = new HashMap<>(8);
    private final Paint blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private IGameOverHandler gameOverHandler;
    private MainActivity mainActivity;
    private MineBoard cells;

    private final RectF destBuffer = new RectF();
    private final Rect srcBuffer = new Rect();

    private int adjBombTextHeight = 10;
    private float cellWidth;
    private float cellHeight;
    private float touchX;
    private float touchY;

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putSerializable("cells", cells);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            cells = (MineBoard) bundle.getSerializable("cells");

            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    public SweeperGridView(Context context) {
        this(context, null);
    }

    public SweeperGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        bgPaint.setColor(Color.GRAY);
        bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        Resources res = getResources();
        bitmaps.put(CellState.COVERED, BitmapFactory.decodeResource(res, R.drawable.tile));
        bitmaps.put(CellState.COVERED_BOMB, BitmapFactory.decodeResource(res, R.drawable.tile));
        bitmaps.put(CellState.UNCOVERED, BitmapFactory.decodeResource(res, R.drawable.tile_empty));
        bitmaps.put(CellState.BOMB, BitmapFactory.decodeResource(res, R.drawable.bomb));
        bitmaps.put(CellState.FLAGGED, BitmapFactory.decodeResource(res, R.drawable.flag));
        bitmaps.put(CellState.FLAGGED_BOMB, BitmapFactory.decodeResource(res, R.drawable.flag));

        for (int i = 0; i < nearbyBombColors.length; i++) {
            bombPaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            bombPaints[i].setColor(nearbyBombColors[i]);
        }

        this.setLongClickable(true);
        this.setClickable(true);
        this.setOnLongClickListener(this);
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (isOutOfBounds(touchX, touchY)) {
            return;
        }
        int column = (int) (touchX / cellWidth);
        int row = (int) (touchY / cellHeight);
        dig(column, row);
    }

    public void setup(MainActivity mainActivity, IGameOverHandler gameOverHandler) {
        this.mainActivity = mainActivity;
        this.gameOverHandler = gameOverHandler;
        Preferences preferences = mainActivity.getPreferences();
        cells = new MineBoard(preferences.getSizeX(), preferences.getSizeY(), preferences.getDifficulty());
        calculateDimensions();
        if (preferences.isFreeDig()) {
            cells.freeDig();
            checkVictoryCondition(); // edgecase with very low difficulty, first dig wins the game
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (isOutOfBounds(touchX, touchY)) {
            return false;
        }
        int x = (int) (touchX / cellWidth);
        int y = (int) (touchY / cellHeight);
        cells.flag(x, y);
        invalidate();
        return true;
    }

    boolean isOutOfBounds(float touchX, float touchY) {
        int x = (int) (touchX / cellWidth);
        int y = (int) (touchY / cellHeight);
        return (x < 0 || y < 0 && x >= cells.getSizeX() && y >= cells.getSizeY());
    }

    public void dig(int x, int y) {
        if (cells.dig(x, y)) { // dig returns true if hit a bomb
            mainActivity.playExplosionSound();
            gameOverHandler.Lose();
        } else {
            mainActivity.playShovelSound();
            checkVictoryCondition();
        }

        invalidate();
    }

    private void checkVictoryCondition() {
        if (cells.checkWin()) {
            mainActivity.playWinSound();
            gameOverHandler.Win();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions() {
        cellWidth = (float) getWidth() / cells.getSizeX();
        cellHeight = (float) getHeight() / cells.getSizeY();

        for (Paint bombPaint : bombPaints) {
            adjBombTextHeight = setTextSizeForWidth(bombPaint, Math.min(cellWidth, cellHeight) / 2);
        }

        invalidate();
    }

    //https://stackoverflow.com/questions/12166476/android-canvas-drawtext-set-font-size-from-width
    private static int setTextSizeForWidth(Paint paint, float desiredWidth) {
        String str = "9";
        int size = 0;

        do {
            paint.setTextSize(++size);
        } while (paint.measureText(str) < desiredWidth);

        paint.setTextSize(size);
        paint.setTextAlign(Paint.Align.CENTER);
        Rect bounds = new Rect();
        paint.getTextBounds(str, 0, str.length(), bounds);
        return bounds.height();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        for (int x = 0; x < cells.getSizeX(); x++) {
            for (int y = 0; y < cells.getSizeY(); y++) {
                destBuffer.set(x * cellWidth, y * cellHeight,
                        (x + 1) * cellWidth, (y + 1) * cellHeight);

                canvas.drawRect(destBuffer, bgPaint);

                if (cells.getCellState(x, y) == CellState.UNCOVERED) {
                    final int adjacentBombs = cells.getAdjacentBombs(x, y);
                    if (adjacentBombs != 0) {
                        canvas.drawText(String.valueOf(adjacentBombs),
                                destBuffer.left + cellWidth / 2f,
                                destBuffer.top + cellHeight / 2f + adjBombTextHeight / 2f,
                                bombPaints[adjacentBombs]);
                    }
                } else {
                    final CellState cellState = cells.getCellState(x, y);
                    final Bitmap d = bitmaps.get(cellState);
                    if (d != null) {
                        srcBuffer.set(0, 0, d.getWidth(), d.getHeight());

                        canvas.drawBitmap(d, srcBuffer, destBuffer, null);
                    }
                }
            }
        }

        for (int x = 1; x < cells.getSizeX(); x++)
            canvas.drawLine(x * cellWidth, 0f, x * cellWidth, getHeight(), blackPaint);
        for (int y = 1; y < cells.getSizeY(); y++)
            canvas.drawLine(0f, y * cellHeight, getWidth(), y * cellHeight, blackPaint);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int h = this.getMeasuredHeight();
        int w = this.getMeasuredWidth();
        int squareDim = Math.min(w, h);
        // Inside a view holder or other grid element,
        // with dynamically added content that is not in the XML,
        // height may be 0. In that case, use the other dimension.
        if (squareDim == 0)
            squareDim = Math.max(w, h);

        setMeasuredDimension(squareDim, squareDim);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.touchX = event.getX();
        this.touchY = event.getY();

        return super.onTouchEvent(event);
    }
}