"""Build the repo-native vector launcher icon from real font outlines."""
from pathlib import Path
import argparse
from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen

parser = argparse.ArgumentParser()
parser.add_argument("font")
parser.add_argument("--preview", help="Optional scratch PNG preview (requires cairosvg)")
args = parser.parse_args()
font = TTFont(args.font)
glyphs = font.getGlyphSet()
cmap = font.getBestCmap()
upm = font["head"].unitsPerEm
paths = []
for word, baseline in [("SIMPLE", 47), ("PRINT", 73)]:
    scale = 18 / upm
    names = [cmap[ord(c)] for c in word]
    width = sum(glyphs[n].width for n in names) * scale
    x = (108 - width) / 2
    for name in names:
        pen = SVGPathPen(glyphs)
        glyphs[name].draw(TransformPen(pen, (scale, 0, 0, -scale, x, baseline)))
        paths.append(pen.getCommands())
        x += glyphs[name].width * scale
outline = " ".join(paths)
vector = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFFFF" android:pathData="M0,0 H108 V108 H0 Z" />
    <path android:fillColor="#000000" android:pathData="{outline}" />
</vector>
'''
root = Path(__file__).resolve().parent.parent
(root / "app/src/main/res/drawable/ic_launcher.xml").write_text(vector)
if args.preview:
    import cairosvg
    svg = f'<svg xmlns="http://www.w3.org/2000/svg" width="324" height="324" viewBox="0 0 108 108"><rect width="108" height="108" fill="white"/><path d="{outline}"/></svg>'
    cairosvg.svg2png(bytestring=svg.encode(), write_to=args.preview)
