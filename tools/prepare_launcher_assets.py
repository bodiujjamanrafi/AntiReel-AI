"""Package the exact native CompanionView export, retaining adaptive-icon safe zones.

Recreate artwork/miso-native.png with MisoAssetExportTest; no generated lookalike
or signing material is used. This script only prepares standard Android densities.
"""
from pathlib import Path
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'artwork/miso-native.png'
RES=ROOT/'app/src/main/res'
art=Image.open(SOURCE).convert('RGBA')
bounds=art.getbbox()
if not bounds:raise SystemExit('Native Miso export is empty')
art=art.crop(bounds)
side=max(art.size)+24
square=Image.new('RGBA',(side,side))
square.alpha_composite(art,((side-art.width)//2,(side-art.height)//2))
foreground=Image.new('RGBA',(648,648))
foreground.alpha_composite(square.resize((432,432),Image.Resampling.LANCZOS),(108,108))
(RES/'drawable-nodpi').mkdir(exist_ok=True)
foreground.save(RES/'drawable-nodpi/miso_launcher_foreground.png')
(RES/'drawable-nodpi/miso_launcher_foreground.webp').unlink(missing_ok=True)
square.resize((192,192),Image.Resampling.LANCZOS).save(RES/'drawable-nodpi/miso_brand_png.png')
# The themed icon uses the identical native silhouette via its alpha mask.
monochrome=Image.new('RGBA',foreground.size,'white');monochrome.putalpha(foreground.getchannel('A'))
monochrome.save(RES/'drawable-nodpi/miso_native_monochrome.webp',lossless=True)
for density,size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    icon=Image.new('RGBA',(size,size))
    inset=max(2,round(size*.08));scaled=square.resize((size-2*inset,size-2*inset),Image.Resampling.LANCZOS)
    icon.alpha_composite(scaled,(inset,inset))
    for name in ('ic_launcher','ic_launcher_round'):
        icon.save(RES/f'mipmap-{density}/{name}.png')
        (RES/f'mipmap-{density}/{name}.webp').unlink(missing_ok=True)
print('Packaged the exact native Miso: launcher, themed silhouette and transparent header.')
