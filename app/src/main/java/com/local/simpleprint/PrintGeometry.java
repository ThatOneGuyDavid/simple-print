package com.local.simpleprint;

/** Printer-pixel geometry, independent of Android so all four modes can be tested. */
final class PrintGeometry {
	static final int PAPER_WIDTH = 384;
	static final int EDGE = 8;
	static final int BORDER_PADDING = 12;
	// Memory guard, not an automatic wrap or rescale threshold.
	static final int MAX_LENGTH = 16384;
	final int padding, blockWidth, blockHeight, outputHeight, left, top;
	final boolean rotated;

	PrintGeometry(int textWidth, int textHeight, boolean border, boolean rotated) {
		if (textWidth < 1 || textHeight < 1) throw new IllegalArgumentException("Empty text layout");
		this.rotated = rotated;
		padding = border ? BORDER_PADDING : 0;
		blockWidth = textWidth + padding * 2;
		blockHeight = textHeight + padding * 2;
		int across = rotated ? blockHeight : blockWidth;
		int along = rotated ? blockWidth : blockHeight;
		if (across > PAPER_WIDTH - EDGE * 2) {
			throw new IllegalArgumentException(rotated
				? "Too many lines for 90° mode. Remove a line or use smaller text."
				: "Text is wider than the paper.");
		}
		if (along > MAX_LENGTH - EDGE * 2)
			throw new IllegalArgumentException("Print too long for one job. Split it into shorter prints.");
		left = border ? (PAPER_WIDTH - across) / 2 : EDGE;
		top = EDGE;
		outputHeight = along + EDGE * 2;
	}

	static int normalTextWidth(boolean border) {
		return PAPER_WIDTH - EDGE * 2 - (border ? BORDER_PADDING * 2 : 0);
	}
}
