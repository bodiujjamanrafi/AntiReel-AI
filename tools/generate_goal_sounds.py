"""Original near-goal and reached-goal notification tones; no external audio assets."""
from pathlib import Path
import math
import struct
import wave

RATE = 24000
ROOT = Path(__file__).resolve().parents[1] / 'app/src/main/res/raw'
SCORES = {
    'miso_near': ((0.0, 783.99, .28), (.22, 659.25, .42)),
    'miso_limit': ((0.0, 523.25, .32), (.28, 440.0, .35), (.59, 349.23, .55)),
}
for name, notes in SCORES.items():
    duration = max(start + length for start, _, length in notes) + .12
    frames = bytearray()
    for sample in range(int(RATE * duration)):
        t = sample / RATE
        amplitude = 0.0
        for start, frequency, length in notes:
            age = t - start
            if 0 <= age < length:
                envelope = min(age / .016, 1.0) * math.exp(-4 * age / length)
                envelope *= min((length - age) / .08, 1.0)
                tone = math.sin(2 * math.pi * frequency * age)
                tone += .10 * math.sin(4 * math.pi * frequency * age)
                amplitude += .28 * envelope * tone
        frames.extend(struct.pack('<h', round(max(-.9, min(.9, amplitude)) * 32767)))
    with wave.open(str(ROOT / (name + '.wav')), 'wb') as sound:
        sound.setparams((1, 2, RATE, 0, 'NONE', 'not compressed'))
        sound.writeframes(frames)
    print(name)
