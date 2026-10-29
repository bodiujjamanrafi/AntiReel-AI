"""Generate AntiReel's original, offline break-complete chime (standard library only)."""
from pathlib import Path
import math
import struct
import wave

RATE = 24000
NOTES = ((0.00, 659.255, .38), (.18, 830.609, .44), (.39, 1108.731, .72))
DURATION = 1.4
output = Path(__file__).resolve().parents[1] / 'app/src/main/res/raw/miso_chime.wav'
output.parent.mkdir(parents=True, exist_ok=True)
frames = bytearray()
for sample in range(int(RATE * DURATION)):
    t = sample / RATE
    amplitude = 0.0
    for start, frequency, length in NOTES:
        age = t - start
        if 0 <= age < length:
            envelope = min(age / .018, 1.0) * math.exp(-5.0 * age / length)
            envelope *= min((length - age) / .07, 1.0)
            tone = math.sin(2 * math.pi * frequency * age)
            tone += .14 * math.sin(2 * math.pi * frequency * 2 * age)
            amplitude += .28 * envelope * tone
    frames.extend(struct.pack('<h', round(max(-.95, min(.95, amplitude)) * 32767)))
with wave.open(str(output), 'wb') as sound:
    sound.setparams((1, 2, RATE, 0, 'NONE', 'not compressed'))
    sound.writeframes(frames)
print(output)
