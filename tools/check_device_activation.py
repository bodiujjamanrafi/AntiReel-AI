"""Read-only AntiReel activation check; never grants permissions or changes app-ops."""
import argparse,json,os,subprocess
from pathlib import Path

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--adb',default=str(Path(os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk')))/'platform-tools/adb'))
a=parser.parse_args()
def shell(*args):
 return subprocess.check_output([a.adb,'shell',*args],text=True,stderr=subprocess.STDOUT).strip()
package='com.rafi.antireelai'
component=package+'/'+package+'.tracking.ReelAccessibilityService'
ops=shell('cmd','appops','get',package,'ACCESS_RESTRICTED_SETTINGS')
services=shell('settings','get','secure','enabled_accessibility_services').split(':')
enabled=component in services
restricted=next((line.split(';')[0].strip() for line in ops.splitlines() if line.startswith('ACCESS_RESTRICTED_SETTINGS:')),'Not available from this Android build')
info=shell('dumpsys','package',package)
version=next((line.strip() for line in info.splitlines() if 'versionName=' in line),'Unknown')
flags=next((line.strip() for line in info.splitlines() if 'flags=[' in line and 'HAS_CODE' in line),'Unknown')
print(json.dumps({'package':package,'version':version,'restricted_access':restricted,'accessibility_enabled':enabled,'test_only_build':'TEST_ONLY' in flags,'status':'Android accessibility is enabled; verify service connection in AntiReel Settings > Accessibility.' if enabled else 'Not activated. Android user approval is still required.','note':'Read-only check. No permissions, security settings, app data or installer identity were changed.'},indent=2))
