import contextlib,io,json,sys,tempfile,unittest,zipfile
from pathlib import Path
from unittest.mock import patch
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
import install_checked_apk as installer

class InstallationSafetyTests(unittest.TestCase):
    def check(self,old_signer='release',old_version=5,connected=True):
        with tempfile.TemporaryDirectory() as folder:
            root=Path(folder);(root/'build-tools/37.0.0').mkdir(parents=True)
            apk=root/'candidate.apk'
            with zipfile.ZipFile(apk,'w'):pass
            apk=apk.resolve()
            nodes=[{'tag':'manifest','attrs':{'package':'com.rafi.antireelai','versionName':'1.0.5','versionCode':'6'}},
                   {'tag':'application','attrs':{}},{'tag':'uses-sdk','attrs':{'minSdkVersion':'30'}}]
            calls=[];state={'installed':False}
            def run(command):
                parts=[str(x) for x in command];calls.append(parts)
                if parts[-1]=='devices':return 'List of devices attached\nserial\tdevice' if connected else 'List of devices attached\n'
                if 'install' in parts:state['installed']=True;return 'Success'
                if parts[-2:]==['getprop','ro.build.version.sdk']:return '37'
                if parts[-3:]==['pm','path','com.rafi.antireelai']:return 'package:/data/app/example/base.apk'
                if 'dumpsys' in parts:return 'versionCode='+str(6 if state['installed'] else old_version)
                if 'pull' in parts:return ''
                if 'settings' in parts:return 'null'
                if 'am' in parts:return 'Status: ok'
                raise AssertionError('Unexpected device operation: '+repr(parts))
            def signature(tool,path):return 'release' if Path(path)==apk else old_signer
            output=io.StringIO()
            args=['install_checked_apk.py','--sdk',str(root),'--apk',str(apk),'--install','--launch']
            with patch.object(sys,'argv',args),patch.object(installer,'xmltree',return_value=nodes),patch.object(installer,'signature',side_effect=signature),patch.object(installer,'run',side_effect=run),contextlib.redirect_stdout(output):
                status=installer.main()
            return status,json.loads(output.getvalue()),calls
    def test_signing_conflict_stops_without_touching_installed_data(self):
        status,result,calls=self.check(old_signer='debug')
        self.assertEqual(2,status);self.assertIn('signing_certificate_conflict_do_not_uninstall',result['blockers'])
        self.assertTrue(result['existing_installation_found']);self.assertFalse(result['installation_attempted'])
        self.assertFalse(any('install' in call or 'uninstall' in call for call in calls))
    def test_downgrade_is_not_forced(self):
        status,result,calls=self.check(old_version=99)
        self.assertEqual(2,status);self.assertIn('version_downgrade',result['blockers'])
        self.assertFalse(any('install' in call or 'uninstall' in call for call in calls))
    def test_no_device_cannot_be_reported_installed(self):
        status,result,calls=self.check(connected=False)
        self.assertEqual(2,status);self.assertFalse(result['installed']);self.assertFalse(result['live_tracking_verified'])
        self.assertIn('no_connected_device',result['blockers'])
    def test_compatible_update_preserves_data_without_granting_access(self):
        status,result,calls=self.check()
        self.assertEqual(0,status);self.assertTrue(result['installed']);self.assertTrue(result['launched'])
        self.assertFalse(result['accessibility_enabled']);self.assertFalse(result['live_tracking_verified'])
        install=next(call for call in calls if 'install' in call)
        self.assertIn('-r',install)
        self.assertNotIn('-g',install);self.assertNotIn('-d',install)
        self.assertFalse(any('uninstall' in call or 'appops' in call for call in calls))

if __name__=='__main__':unittest.main()
