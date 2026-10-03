package com.local.simpleprint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import java.util.ArrayList;
import java.util.List;

final class TextRenderer {
	static final int WIDTH = PrintGeometry.PAPER_WIDTH;
	private TextRenderer() {}

	/** Only supported print formatting is copied; IME/selection/clipboard spans never print. */
	static SpannableString snapshot(CharSequence source) {
		SpannableString copy = new SpannableString(source.toString());
		if (source instanceof Spanned) {
			Spanned original = (Spanned) source;
			for (AbsoluteSizeSpan span : original.getSpans(0, source.length(), AbsoluteSizeSpan.class)) {
				int start = original.getSpanStart(span), end = original.getSpanEnd(span);
				if (end > start) copy.setSpan(new AbsoluteSizeSpan(span.getSize(), false),
					start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			for (StyleSpan span : original.getSpans(0, source.length(), StyleSpan.class)) {
				int start = original.getSpanStart(span), end = original.getSpanEnd(span);
				if (end > start) copy.setSpan(new StyleSpan(span.getStyle()),
					start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return copy;
	}

	static final class Document {
		final List<StaticLayout> lines;
		final PrintGeometry geometry;
		Document(List<StaticLayout> lines, PrintGeometry geometry) {
			this.lines = lines;
			this.geometry = geometry;
		}
	}

	private static StaticLayout layout(CharSequence text, TextPaint paint, int width) {
		return StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
			.setAlignment(Layout.Alignment.ALIGN_NORMAL)
			.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE)
			.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
			.setIncludePad(false).setLineSpacing(4f, 1f).build();
	}

	static Document measure(CharSequence source, boolean border, boolean rotated) {
		SpannableString content = snapshot(source.length() == 0 ? " " : source);
		TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		paint.setTextSize(32f);
		List<StaticLayout> layouts = new ArrayList<>();
		int height = 0;
		float width = 1;
		if (rotated) {
			// One layout per explicit newline. No automatic wrapping in this mode.
			int start = 0;
			for (int end = 0; end <= content.length(); end++) {
				if (end != content.length() && content.charAt(end) != '\n') continue;
				CharSequence line = content.subSequence(start, end);
				float desired = Layout.getDesiredWidth(line, paint);
				if (!Float.isFinite(desired) || desired > PrintGeometry.MAX_LENGTH - 64)
					throw new IllegalArgumentException("Print too long for one job. Split it into shorter prints.");
				StaticLayout row = layout(line, paint, Math.max(1, (int) Math.ceil(desired) + 4));
				if (row.getLineCount() != 1)
					throw new IllegalArgumentException("Line cannot be rendered without wrapping.");
				layouts.add(row);
				width = Math.max(width, row.getLineMax(0));
				height += row.getHeight() + (start == 0 ? 0 : 4);
				if (height > WIDTH)
					throw new IllegalArgumentException("Too many lines for 90° mode. Remove a line or use smaller text.");
				start = end + 1;
			}
		} else {
			StaticLayout body = layout(content, paint, PrintGeometry.normalTextWidth(border));
			layouts.add(body);
			height = body.getHeight();
			for (int line = 0; line < body.getLineCount(); line++)
				width = Math.max(width, body.getLineMax(line));
		}
		PrintGeometry geometry = new PrintGeometry((int) Math.ceil(width), height, border, rotated);
		return new Document(layouts, geometry);
	}

	static Bitmap renderBitmap(CharSequence text, boolean border, boolean rotated) {
		Document document = measure(text, border, rotated);
		PrintGeometry g = document.geometry;
		Bitmap bitmap = Bitmap.createBitmap(WIDTH, g.outputHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		canvas.translate(g.left, g.top);
		if (rotated) {
			canvas.translate(g.blockHeight, 0);
			canvas.rotate(90f);
		}
		if (border) {
			Paint pen = new Paint();
			pen.setColor(Color.BLACK);
			pen.setStyle(Paint.Style.STROKE);
			pen.setStrokeWidth(3f);
			canvas.drawRect(1.5f, 1.5f, g.blockWidth - 1.5f, g.blockHeight - 1.5f, pen);
		}
		canvas.translate(g.padding, g.padding);
		for (int i = 0; i < document.lines.size(); i++) {
			StaticLayout row = document.lines.get(i);
			row.draw(canvas);
			canvas.translate(0, row.getHeight() + 4);
		}
		return bitmap;
	}

	static byte[] rasterize(Bitmap bitmap) {
		byte[] pixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
		int[] row = new int[bitmap.getWidth()];
		for (int y = 0; y < bitmap.getHeight(); y++) {
			bitmap.getPixels(row, 0, row.length, 0, y, row.length, 1);
			for (int x = 0; x < row.length; x++) {
				int color = row[x];
				int luminance = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000;
				pixels[y * row.length + x] = (byte) (luminance < 160 ? 1 : 0);
			}
		}
		return pixels;
	}
}
