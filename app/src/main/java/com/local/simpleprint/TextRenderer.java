package com.local.simpleprint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;

final class TextRenderer {
	static final int WIDTH = 384;
	private TextRenderer() {}

	static Bitmap renderBitmap(CharSequence formattedText, boolean drawBorder) {
		SpannableString content = new SpannableString(formattedText.length() == 0 ? " " : formattedText);
		TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		paint.setTextSize(32f);
		int contentWidth = WIDTH - 48;
		StaticLayout layout = StaticLayout.Builder.obtain(content, 0, content.length(), paint, contentWidth)
			.setAlignment(Layout.Alignment.ALIGN_NORMAL)
			.setIncludePad(false)
			.setLineSpacing(4f, 1f)
			.build();
		int height = Math.max(80, layout.getHeight() + 40);
		Bitmap bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		if (drawBorder) {
			Paint border = new Paint();
			border.setColor(Color.BLACK);
			border.setStyle(Paint.Style.STROKE);
			border.setStrokeWidth(3f);
			canvas.drawRect(12, 12, WIDTH - 13, height - 13, border);
		}
		canvas.save();
		canvas.translate(24, 20);
		layout.draw(canvas);
		canvas.restore();
		return bitmap;
	}

	static byte[] rasterize(Bitmap bitmap) {
		byte[] pixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
		for (int y = 0; y < bitmap.getHeight(); y++) {
			for (int x = 0; x < bitmap.getWidth(); x++) {
				int color = bitmap.getPixel(x, y);
				int luminance = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000;
				pixels[y * bitmap.getWidth() + x] = (byte) (luminance < 160 ? 1 : 0);
			}
		}
		return pixels;
	}
}
