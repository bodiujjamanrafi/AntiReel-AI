"""Verify and package APK/AAB release outputs without publishing or changing a device."""
import base64,hashlib,json,os,re,shutil,subprocess,sys,zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]

def run(args):
    return subprocess.check_output([str(x) for x in args],text=True,stderr=subprocess.STDOUT)

def main():
    apk=ROOT/'app/build/outputs/apk/release/app-release.apk'
    bundle=ROOT/'app/build/outputs/bundle/release/app-release.aab'
    audit=ROOT/'docs/release-apk-audit.json'
    if not apk.is_file() or not bundle.is_file():
        raise SystemExit('Build both assembleRelease and bundleRelease before packaging.')
    subprocess.run([sys.executable,str(ROOT/'tools/audit_release_apk.py'),str(apk),'--output',str(audit)],check=True)
    report=json.loads(audit.read_text())
    expected=(ROOT/'docs/release-signing-certificate.sha256').read_text().strip().lower()
    fingerprint=re.search(r'certificate SHA-256 digest: ([0-9a-f]+)',report['signature_verification'])
    if not fingerprint or fingerprint[1].lower()!=expected:
        raise SystemExit('Release signing certificate changed. Retain the existing key for update continuity.')
    java=Path(os.environ['JAVA_HOME'])/'bin' if os.environ.get('JAVA_HOME') else None
    jarsigner=java/'jarsigner' if java else Path(shutil.which('jarsigner') or '')
    keytool=java/'keytool' if java else Path(shutil.which('keytool') or '')
    verification=run([jarsigner,'-J-Duser.language=en','-J-Duser.country=US','-verify',bundle])
    if 'jar verified.' not in verification.lower():raise SystemExit('App bundle signature not verified.')
    certificate=run([keytool,'-printcert','-jarfile',bundle,'-rfc'])
    match=re.search(r'-----BEGIN CERTIFICATE-----\s*(.*?)\s*-----END CERTIFICATE-----',certificate,re.S)
    if not match or hashlib.sha256(base64.b64decode(match[1])).hexdigest()!=expected:
        raise SystemExit('App bundle and APK must share the expected release signer.')
    version=report['version_name']
    if not re.fullmatch(r'[A-Za-z0-9._-]+',version):raise SystemExit('Invalid distribution version.')
    with zipfile.ZipFile(bundle) as z:
        if z.testzip() is not None:raise SystemExit('Corrupt bundle ZIP.')
        required={'BundleConfig.pb','base/manifest/AndroidManifest.xml','base/dex/classes.dex'}
        if not required<=set(z.namelist()):raise SystemExit('Incomplete app bundle.')
        if version.encode() not in z.read('base/manifest/AndroidManifest.xml'):
            raise SystemExit('APK and app bundle version names differ.')
        if any(re.search(r'(?:\.p12|\.jks|\.keystore|signing\.properties)$',n) for n in z.namelist()):
            raise SystemExit('Private signing material must never be packaged.')
    destination=ROOT/'dist';destination.mkdir(exist_ok=True)
    outputs=[]
    for source,suffix in [(apk,'apk'),(bundle,'aab')]:
        target=destination/f'AntiReel-AI-{version}.{suffix}';shutil.copy2(source,target)
        outputs.append({'name':target.name,'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'size_bytes':target.stat().st_size})
    (destination/f'AntiReel-AI-{version}-checksums.txt').write_text(''.join(f"{x['sha256']}  {x['name']}\n" for x in outputs))
    evidence={'version':version,'artifacts':outputs,'release_signer_sha256':expected,
        'apk_checks':report['checks'],'bundle_signature_verified':True,'bundle_structure_checked':True,
        'min_android':'Android 11 (API 30)','abis':report['abis'],
        'limits':'Local checks only. Not malware certification, universal device support, Play Protect approval or Play Console acceptance. AAB is for Play Console, not direct installation.'}
    (destination/f'AntiReel-AI-{version}-verification.json').write_text(json.dumps(evidence,indent=2)+'\n')
    print(json.dumps(evidence,indent=2))

if __name__=='__main__':main()
