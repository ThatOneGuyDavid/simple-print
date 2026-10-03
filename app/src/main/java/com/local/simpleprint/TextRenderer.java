package com.local.simpleprint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.List;

final class TextRenderer {
	static final int WIDTH = 384;
	private TextRenderer() {}

	static Bitmap renderBitmap(String content, int textSize, boolean bold, boolean drawBorder) {
		Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
		text.setColor(Color.BLACK);
		text.setTextSize(textSize);
		text.setTypeface(bold ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT);
		List<String> lines = wrap(content.isEmpty() ? " " : content, text, WIDTH - 48);
		Paint.FontMetrics metrics = text.getFontMetrics();
		int lineHeight = (int) Math.ceil(metrics.descent - metrics.ascent + textSize * 0.25f);
		int height = Math.max(80, 40 + lines.size() * lineHeight);
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
		float baseline = 20 - metrics.ascent;
		for (String line : lines) {
			canvas.drawText(line, 24, baseline, text);
			baseline += lineHeight;
		}
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

	private static List<String> wrap(String content, Paint paint, int maxWidth) {
		List<String> lines = new ArrayList<>();
		String[] paragraphs = content.replace("\r", "").split("\n", -1);
		for (String paragraph : paragraphs) {
			if (paragraph.isEmpty()) { lines.add(""); continue; }
			String remaining = paragraph;
			while (!remaining.isEmpty()) {
				int count = paint.breakText(remaining, true, maxWidth, null);
				if (count >= remaining.length()) { lines.add(remaining); break; }
				int split = remaining.lastIndexOf(' ', count);
				if (split <= 0) split = count;
				lines.add(remaining.substring(0, split).trim());
				remaining = remaining.substring(split).trim();
			}
		}
		return lines;
	}
}
