"""Build caption-free, offline spoken Miso guidance from the supplied Android screens.

Only the original Android UI contains text. The video never interacts with Settings.
Audio is exported using MisoNarrationExportTest and muxed to the same timeline as
its visual step, so pauses, replay, seeking and full screen keep speech in sync.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import argparse
import json
import math
import subprocess
import tempfile

ROOT=Path(__file__).resolve().parents[1]
FPS=24
WIDTH,HEIGHT=720,1280

def duration(path):
    return float(subprocess.check_output(['ffprobe','-v','error','-show_entries','format=duration','-of','default=nw=1:nk=1',str(path)],text=True).strip())

def draw_finger(draw,x,y,pressed):
    # A drawn index finger and palm, with a contact shadow and tap ripple.
    scale=.88 if pressed else 1.0
    def box(a,b,c,d):return (x+a*scale,y+b*scale,x+c*scale,y+d*scale)
    skin='#F7D6CA';edge='#9A6F76'
    draw.ellipse(box(-22,-9,24,19),fill='#55343A')
    draw.rounded_rectangle(box(-14,-8,14,91),radius=14,fill=skin,outline=edge,width=3)
    draw.rounded_rectangle(box(-16,62,84,154),radius=23,fill=skin,outline=edge,width=3)
    draw.rounded_rectangle(box(6,52,31,108),radius=12,fill=skin,outline=edge,width=2)
    draw.rounded_rectangle(box(31,61,56,113),radius=12,fill=skin,outline=edge,width=2)
    draw.rounded_rectangle(box(56,74,80,119),radius=11,fill=skin,outline=edge,width=2)
    draw.line([box(-32,78,-9,120)[:2],box(-32,78,-9,120)[2:]],fill=skin,width=23)
    draw.arc(box(-9,7,9,30),0,180,fill='#E8AEB0',width=2)

def render(scene,t,length,screens):
    image=Image.new('RGB',(WIDTH,HEIGHT),'#10131B')
    shot=screens[scene]
    x=(WIDTH-shot.width)//2;y=(HEIGHT-shot.height)//2
    image.paste(shot,(x,y))
    draw=ImageDraw.Draw(image)
    # One approach and one tap per screen. There is no modulo/repeated gesture.
    targets=[(.45,.25),(.50,.765),(.79,.78)]
    tx,ty=targets[scene]
    dest=(x+tx*shot.width,y+ty*shot.height)
    phase=max(0,min(1,(t-.35)/.8));ease=phase*phase*(3-2*phase)
    px=690+(dest[0]-690)*ease;py=1090+(dest[1]-1090)*ease
    if .35<t<min(length,2.35):
        contact=1.2<t<1.55
        if 1.2<t<1.9:
            r=24+int((t-1.2)*38)
            draw.ellipse((px-r,py-r,px+r,py+r),outline='#8DE0C6',width=4)
        draw_finger(draw,px,py,contact)
    return image

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--output',type=Path,default=ROOT/'app/src/main/res/raw/miso_setup.mp4')
    args=parser.parse_args()
    source=ROOT/'app/src/main/res/drawable-nodpi'
    screens=[Image.open(source/f'miso_setup_{name}.png').convert('RGB') for name in ['find','allow','shortcut']]
    # Remove the floating shortcut mascot from the video composition only.
    # This patch contains no permission text; the dialog wording stays untouched.
    from PIL import ImageOps
    shortcut=screens[2].copy()
    patch=shortcut.crop((0,880,162,1045))
    shortcut.paste(ImageOps.mirror(patch),(940,880))
    screens[2]=shortcut
    # Fit every source to the identical 720 × 1280 canvas without stretching or crop.
    fitted=[]
    for shot in screens:
        scale=min(WIDTH/shot.width,HEIGHT/shot.height)
        fitted.append(shot.resize((round(shot.width*scale),round(shot.height*scale)),Image.Resampling.LANCZOS))
    screens=fitted
    voices=[ROOT/f'artwork/narration/miso-guide-{i}.wav' for i in range(3)]
    lengths=[math.ceil((duration(path)+.25)*FPS)/FPS for path in voices]
    with tempfile.TemporaryDirectory(prefix='antireel-spoken-guide-') as temporary:
        silent=Path(temporary)/'visual.mp4'
        process=subprocess.Popen(['ffmpeg','-y','-f','rawvideo','-pixel_format','rgb24','-video_size',f'{WIDTH}x{HEIGHT}','-framerate',str(FPS),'-i','pipe:0','-an','-c:v','libx264','-pix_fmt','yuv420p','-crf','25',str(silent)],stdin=subprocess.PIPE,stdout=subprocess.DEVNULL,stderr=subprocess.PIPE)
        for scene,length in enumerate(lengths):
            for frame in range(round(length*FPS)):
                process.stdin.write(render(scene,frame/FPS,length,screens).tobytes())
        process.stdin.close();error=process.stderr.read()
        if process.wait():raise RuntimeError(error.decode())
        inputs=[]
        for voice in voices:inputs+=['-i',str(voice)]
        filters=';'.join(f'[{i+1}:a]apad,atrim=duration={length},asetpts=PTS-STARTPTS[a{i}]' for i,length in enumerate(lengths))+';[a0][a1][a2]concat=n=3:v=0:a=1[narration]'
        subprocess.run(['ffmpeg','-y','-i',str(silent)]+inputs+['-filter_complex',filters,'-map','0:v','-map','[narration]','-c:v','copy','-c:a','aac','-b:a','96k','-movflags','+faststart',str(args.output)],check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
    print(json.dumps({'file':args.output.name,'duration':sum(lengths),'dimensions':[WIDTH,HEIGHT],'fps':FPS,'spoken_segments':len(voices),'added_captions':False,'gesture_count':3,'added_mascots':0}))
if __name__=='__main__':main()
