"""Preflight/install only same-certificate AntiReel updates. Never uninstalls or grants access."""
import argparse,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
from audit_release_apk import xmltree

def run(args):
    return subprocess.check_output([str(x) for x in args],text=True,stderr=subprocess.STDOUT,timeout=90).strip()

def signature(apksigner,path):
    text=run([apksigner,'verify','--print-certs',path])
    match=re.search(r'certificate SHA-256 digest: ([0-9a-f]+)',text)
    if not match:raise ValueError('No verified signing certificate.')
    return match[1]

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk',type=Path,default=Path(__file__).resolve().parents[1]/'app/build/outputs/apk/release/app-release.apk')
    parser.add_argument('--sdk',type=Path,default=Path(os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk'))))
    parser.add_argument('--serial');parser.add_argument('--install',action='store_true');parser.add_argument('--launch',action='store_true')
    args=parser.parse_args();apk=args.apk.resolve()
    sdkTools=sorted((args.sdk/'build-tools').iterdir(),key=lambda p:tuple(int(x) for x in p.name.split('.') if x.isdigit()))[-1]
    adb=args.sdk/'platform-tools/adb';apksigner=sdkTools/'apksigner'
    nodes=xmltree(sdkTools/'aapt2',apk,'AndroidManifest.xml')
    manifest=next(n['attrs'] for n in nodes if n['tag']=='manifest');app=next(n['attrs'] for n in nodes if n['tag']=='application');sdk=next(n['attrs'] for n in nodes if n['tag']=='uses-sdk')
    package=manifest['package']
    if package!='com.rafi.antireelai':raise ValueError('This tool only operates on the AntiReel package.')
    signer=signature(apksigner,apk)
    result={'apk':str(apk),'version':manifest['versionName'],'signer_sha256':signer,'installed':False,'existing_installation_found':None,'installation_attempted':False,'launched':False,'live_tracking_verified':False,'blockers':[]}
    if app.get('testOnly') in ('true','0xffffffff'):result['blockers'].append('test_only_apk')
    devices=[line.split()[0] for line in run([adb,'devices']).splitlines()[1:] if len(line.split())>=2 and line.split()[1]=='device']
    if args.serial:
        if args.serial not in devices:result['blockers'].append('requested_device_not_connected')
        serial=args.serial
    elif len(devices)==1:serial=devices[0]
    else:
        serial=None;result['blockers'].append('no_connected_device' if not devices else 'multiple_devices_choose_serial')
    if result['blockers']:
        print(json.dumps(result,indent=2));return 2
    prefix=[adb,'-s',serial]
    def shell(*command):return run(prefix+['shell',*command])
    api=int(shell('getprop','ro.build.version.sdk'));result['device_api']=api
    if api<int(sdk['minSdkVersion'],0):result['blockers'].append('android_version_too_old')
    with zipfile.ZipFile(apk) as z:abis={n.split('/')[1] for n in z.namelist() if n.startswith('lib/') and n.endswith('.so')}
    if abis and not abis.intersection(shell('getprop','ro.product.cpu.abilist').split(',')):result['blockers'].append('unsupported_cpu_abi')
    def installed_apk():
        try:return next((line.removeprefix('package:') for line in shell('pm','path',package).splitlines() if line.startswith('package:') and line.endswith('/base.apk')),None)
        except subprocess.CalledProcessError:return None
    existing=installed_apk()
    result['existing_installation_found']=bool(existing)
    if existing:
        info=shell('dumpsys','package',package);code=re.search(r'versionCode=(\d+)',info)
        if code and int(code[1])>int(manifest['versionCode'],0):result['blockers'].append('version_downgrade')
        with tempfile.TemporaryDirectory(prefix='antireel-signature-check-') as folder:
            previous=Path(folder)/'installed.apk';run(prefix+['pull',existing,previous])
            result['installed_signer_sha256']=signature(apksigner,previous)
        if result['installed_signer_sha256']!=signer:result['blockers'].append('signing_certificate_conflict_do_not_uninstall')
    if args.install and not result['blockers']:
        result['installation_attempted']=True
        try:
            run(prefix+['install','--no-streaming','-r',apk])
            info=shell('dumpsys','package',package);code=re.search(r'versionCode=(\d+)',info)
            result['installed']=installed_apk() is not None and code is not None and int(code[1])==int(manifest['versionCode'],0)
            if not result['installed']:result['blockers'].append('post_install_verification_failed')
        except subprocess.CalledProcessError as error:
            result['blockers'].append('package_manager_rejected_install')
            result['package_manager_result']=error.output.strip()
    if args.launch and result['installed']:
        status=shell('am','start','-W','-n',package+'/.MainActivity')
        result['launched']='Status: ok' in status
    enabled=shell('settings','get','secure','enabled_accessibility_services').split(':')
    result['accessibility_enabled']=any(x in enabled for x in (package+'/'+package+'.tracking.ReelAccessibilityService',package+'/.tracking.ReelAccessibilityService'))
    result['note']='No uninstall, data reset, downgrade override, installer disguise, permission grant or device-protection change was performed.'
    print(json.dumps(result,indent=2));return 2 if result['blockers'] else 0

if __name__=='__main__':
    try:raise SystemExit(main())
    except (ValueError,OSError,subprocess.SubprocessError) as error:
        print(json.dumps({'status':'verification_failed','error':str(error),'installed':False,'live_tracking_verified':False},indent=2));raise SystemExit(2)
