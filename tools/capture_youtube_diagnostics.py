"""Capture only AntiReel's numeric Shorts diagnostics during a MANUAL device test.

Does not swipe, open apps, grant permissions, change goals, clear logs or read screen
content. Enable the diagnostic tag with adb shell setprop log.tag.AntiReelShorts DEBUG.
Restore it with adb shell setprop log.tag.AntiReelShorts INFO after testing.
"""
import argparse
from collections import Counter
import json
import os
from pathlib import Path
import re
import select
import subprocess
import time
import uuid


def summarize(messages):
    states = Counter()
    generations = []
    scroll_events = 0
    for message in messages:
        if message.startswith('event='):
            scroll_events += 1
        state = re.search(r'\bstate=([a-z_]+):([a-z_]+)', message)
        if state:
            states[state[1]] += 1
        count = re.search(r'\bcount=true\b.*', message)
        generation = re.search(r'\bgeneration=(\d+)\b', message)
        if count and generation:
            generations.append(int(generation[1]))
    return {'count_increments': len(generations),
            'duplicate_commit_generations': len(generations) - len(set(generations)),
            'player_scroll_events': scroll_events, 'states': dict(states),
            'limitation': 'Diagnostic increments are not visual ground truth. Compare manually observed new Shorts with increments; exclude ads and revisits.'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--adb', default=str(Path.home() / 'Library/Android/sdk/platform-tools/adb'))
    parser.add_argument('--serial')
    parser.add_argument('--seconds', type=int, default=240)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    if not 1 <= args.seconds <= 1800:
        parser.error('--seconds must be between 1 and 1800')
    adb = [args.adb] + (['-s', args.serial] if args.serial else [])
    subprocess.run(adb + ['get-state'], check=True, capture_output=True)
    marker = 'capture-start=' + uuid.uuid4().hex
    process = subprocess.Popen(adb + ['logcat', '-v', 'brief', '-s', 'AntiReelShorts:D', '*:S'],
                               stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    messages = []
    started = False
    buffer = b''
    try:
        subprocess.run(adb + ['shell', 'log', '-p', 'd', '-t', 'AntiReelShorts', marker], check=True)
        print('Recording numeric diagnostics. Manually view Shorts; Ctrl+C ends capture. Goals and permissions are unchanged.', flush=True)
        deadline = time.monotonic() + args.seconds
        while time.monotonic() < deadline and process.poll() is None:
            ready, _, _ = select.select([process.stdout], [], [], min(.25, max(0, deadline-time.monotonic())))
            if not ready:
                continue
            data = os.read(process.stdout.fileno(), 65536)
            if not data:
                break
            buffer += data
            while b'\n' in buffer:
                line, buffer = buffer.split(b'\n', 1)
                line = line.decode('utf-8', errors='replace')
                if marker in line:
                    started = True
                    continue
                if not started or 'AntiReelShorts' not in line:
                    continue
                # Retain only the documented, label-free state/event record shape.
                match = re.search(r'\b(?:state=[a-z_]+:[a-z_]+|event=\d+)\s+generation=\d+[^\r\n]*$', line)
                if match:
                    messages.append(match[0])
    except KeyboardInterrupt:
        pass
    finally:
        process.terminate()
        try:
            process.wait(timeout=3)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()
    report = summarize(messages)
    report.update({'capture_started': started, 'messages': messages,
                   'physical_accuracy_verified': False,
                   'note': 'A human must annotate and compare the physical 30-Short test before claiming accuracy.'})
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps({k: v for k, v in report.items() if k != 'messages'}, indent=2))


if __name__ == '__main__':
    main()
