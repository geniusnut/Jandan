package com.alensw.shape;

import android.graphics.*;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.Shape;
import android.view.Gravity;

public class Shapes {
//	public static final float[] mRevertMatrix = new float[] { -1,0,0,0,256, 0,-1,0,0,256, 0,0,-1,0,256, 0,0,0,1,0 };
//	public static final ColorMatrixColorFilter mRevertColorFilter = new ColorMatrixColorFilter(mRevertMatrix);

	public static Drawable createShadowBackground(int color, int padding) {
		final float shadow = padding * 3 / 4f;
		final PaintDrawable drawable = new PaintDrawable(color);
		drawable.getPaint().setShadowLayer(shadow, shadow / 4, shadow / 4, Color.BLACK);
		drawable.setCornerRadius(padding / 2f);
		drawable.setPadding(0, 0, 0, 0);
		return new InsetDrawable(drawable, padding);
	}

	public static final int[] STATES_DEFAULT = new int[0];
	public static final int[] STATES_SELECTED = new int[]{android.R.attr.state_enabled, android.R.attr.state_selected};

	public static Drawable createTabBackground(int color) {
		final StateListDrawable sd = new StateListDrawable();
		sd.addState(STATES_SELECTED, new ClipDrawable(new ColorDrawable(color), Gravity.BOTTOM, ClipDrawable.VERTICAL));
		sd.addState(STATES_DEFAULT, new ColorDrawable(Color.TRANSPARENT));
		sd.setLevel(Math.round(10000 * 2 / 48f));  // 2dp in 48dp
		return sd;
	}

	public static final int[] PATTERN_MASK = {0xffffffff, 0xffcccccc, 0xffcccccc, 0xffffffff};

	public static Shader createPatternShader(int scale) {
		final Bitmap bitmap = Bitmap.createBitmap(PATTERN_MASK, 2, 2, Bitmap.Config.RGB_565);
		final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		if (scale != 1) {
			final Matrix matrix = new Matrix();
			matrix.setScale(scale, scale);
			shader.setLocalMatrix(matrix);
		}
		return shader;
	}

	public static ShapeDrawable createShapeImage(Shape shape, int width, int height, int color) {
		final ShapeDrawable drawable = new ShapeDrawable(shape);
		drawable.getPaint().setColor(color);
		drawable.setIntrinsicWidth(width);
		drawable.setIntrinsicHeight(height);
		return drawable;
	}
}

