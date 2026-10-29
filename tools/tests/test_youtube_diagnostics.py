import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('youtube_diagnostics', Path(__file__).parents[1] / 'capture_youtube_diagnostics.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class DiagnosticSummaryTest(unittest.TestCase):
    def test_event_bursts_are_not_reported_as_video_counts(self):
        events = ['event=4096 generation=2 from=-1 to=-1 current=-1 dy=0 indexed=false'] * 20
        events += ['state=counted:youtube_metadata generation=2 count=true indexed=false direction=0',
                   'state=seen_video:youtube_metadata generation=3 count=false indexed=false direction=0']
        report = module.summarize(events)
        self.assertEqual(1, report['count_increments'])
        self.assertEqual(20, report['player_scroll_events'])
        self.assertEqual(0, report['duplicate_commit_generations'])

    def test_duplicate_commit_generations_are_flagged(self):
        report = module.summarize(['state=counted:youtube_metadata generation=5 count=true'] * 2)
        self.assertEqual(2, report['count_increments'])
        self.assertEqual(1, report['duplicate_commit_generations'])


if __name__ == '__main__':
    unittest.main()
