"""Local APK configuration/signature audit. Not an antivirus or Play Protect verdict."""
import argparse,hashlib,json,os,re,subprocess,zipfile,struct
from pathlib import Path

def xmltree(binary,apk,file):
    dump=subprocess.check_output([str(binary),'dump','xmltree',str(apk),'--file',file],text=True)
    nodes=[];stack=[]
    for line in dump.splitlines():
        depth=len(line)-len(line.lstrip())
        m=re.search(r'E: ([\w-]+)',line)
        if m:
            while stack and stack[-1]['depth']>=depth:stack.pop()
            node={'tag':m[1],'depth':depth,'attrs':{},'parent':stack[-1]['tag'] if stack else None}
            nodes.append(node);stack.append(node)
        elif stack and 'A: ' in line:
            attr=line.split('A: ',1)[1];name=attr.split('=',1)[0].split('(')[0].rsplit(':',1)[-1];value=attr.split('=',1)[1]
            raw=re.search(r'\(Raw: "(.*)"\)',value)
            stack[-1]['attrs'][name]=raw[1] if raw else value.strip('"')
    return nodes

def elf_load_alignments(data):
    """Read ELF64 PT_LOAD alignment from bytes; no native code is executed."""
    if len(data)<64 or data[:4]!=b"\x7fELF" or data[4]!=2 or data[5] not in (1,2):
        raise ValueError("Expected a valid ELF64 library")
    endian="<" if data[5]==1 else ">"
    offset=struct.unpack_from(endian+"Q",data,32)[0]
    size,count=struct.unpack_from(endian+"HH",data,54)
    if size<56 or count==0 or offset+size*count>len(data):
        raise ValueError("Invalid ELF program-header table")
    return [struct.unpack_from(endian+"Q",data,offset+i*size+48)[0]
        for i in range(count) if struct.unpack_from(endian+"I",data,offset+i*size)[0]==1]

BACKUP_DOMAINS={'root','file','database','sharedpref','external','device_root','device_file','device_database','device_sharedpref'}
def excludes_all_app_data(nodes,parent):
    domains={n['attrs'].get('domain') for n in nodes if n['tag']=='exclude' and n['parent']==parent and n['attrs'].get('path')=='.'}
    return BACKUP_DOMAINS<=domains

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('apk',type=Path);parser.add_argument('--sdk',type=Path,default=Path(os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk'))));parser.add_argument('--output',type=Path,default=Path('docs/release-apk-audit.json'));args=parser.parse_args()
    tools=sorted((args.sdk/'build-tools').iterdir(),key=lambda p:tuple(int(x) for x in p.name.split('.') if x.isdigit()))[-1]
    apk=args.apk.resolve();nodes=xmltree(tools/'aapt2',apk,'AndroidManifest.xml')
    app=next(n['attrs'] for n in nodes if n['tag']=='application')
    package=next(n['attrs']['package'] for n in nodes if n['tag']=='manifest')
    true=lambda v:v in ('true','0xffffffff')
    sdk=next(n['attrs'] for n in nodes if n['tag']=='uses-sdk')
    manifest=next(n['attrs'] for n in nodes if n['tag']=='manifest')
    permissions=sorted(n['attrs']['name'] for n in nodes if n['tag']=='uses-permission')
    allowed={'android.permission.VIBRATE','android.permission.POST_NOTIFICATIONS','android.permission.RECEIVE_BOOT_COMPLETED',package+'.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'}
    components=[{'type':n['tag'],**n['attrs']} for n in nodes if n['tag'] in ('activity','activity-alias','service','receiver','provider')]
    exported=[c for c in components if true(c.get('exported','false'))]
    protected={'android.permission.BIND_ACCESSIBILITY_SERVICE','android.permission.DUMP'}
    checks={
        'release_not_debuggable':not true(app.get('debuggable','false')),
        'release_not_test_only':not true(app.get('testOnly','false')),
        'backup_disabled':app.get('allowBackup') in ('false','0x00000000'),
        'cleartext_disabled':app.get('usesCleartextTraffic') in ('false','0x00000000'),
        'only_expected_permissions':set(permissions)<=allowed,
        'no_packaged_instrumentation':not any(n['tag']=='instrumentation' for n in nodes),
        'no_required_hardware_features':all(n['attrs'].get('required','true') in ('false','0x00000000') for n in nodes if n['tag']=='uses-feature'),
        'no_internet_permission':'android.permission.INTERNET' not in permissions,
        'exported_components_guarded':all(c.get('name')==package+'.MainActivity' or c.get('permission') in protected for c in exported),
    }
    resources=subprocess.check_output([str(tools/'aapt2'),'dump','resources',str(apk)],text=True)
    resource=re.search(r'xml/reel_accessibility\s+\(\) \(file\) (\S+)',resources)
    if not resource:raise SystemExit('Accessibility resource missing from release.')
    accessibility=xmltree(tools/'aapt2',apk,resource[1])[0]['attrs']
    checks['accessibility_scoped']=bool(accessibility.get('packageNames'))
    packages=set(accessibility.get('packageNames','').split(','))
    queries={n['attrs'].get('name') for n in nodes if n['tag']=='package' and n['parent']=='queries'}
    checks['accessibility_scope_matches_declared_platforms']=bool(packages) and packages<=queries and '*' not in packages
    extra_capabilities=['canPerformGestures','canRequestFilterKeyEvents','canRequestTouchExplorationMode','canRequestFingerprintGestures','canRequestEnhancedWebAccessibility']
    checks['no_unneeded_accessibility_control_capabilities']=not any(true(accessibility.get(key,'false')) for key in extra_capabilities)
    event_types=int(accessibility.get('accessibilityEventTypes','0'),0)
    checks['no_input_text_accessibility_events']=event_types & (0x10|0x2000)==0
    def resource_nodes(name):
        match=re.search(r'xml/'+name+r'\s+\(\) \(file\) (\S+)',resources)
        if not match:raise SystemExit('Required backup rules resource missing.')
        return xmltree(tools/'aapt2',apk,match[1])
    extraction=resource_nodes('data_extraction_rules')
    checks['explicit_cloud_and_device_backup_exclusions']=excludes_all_app_data(resource_nodes('backup_rules'),'full-backup-content') and excludes_all_app_data(extraction,'cloud-backup') and excludes_all_app_data(extraction,'device-transfer')
    checks['not_misrepresented_as_disability_tool']=accessibility.get('isAccessibilityTool') in ('false','0x00000000')
    certificate=subprocess.check_output([str(tools/'apksigner'),'verify','--verbose','--print-certs',str(apk)],text=True,stderr=subprocess.STDOUT)
    checks['signature_verified']='Verifies' in certificate
    checks['not_debug_certificate']='CN=Android Debug' not in certificate
    checks['zip_aligned_16kb']=subprocess.run([str(tools/'zipalign'),'-c','-P','16','4',str(apk)],capture_output=True).returncode==0
    with zipfile.ZipFile(apk) as z:
        checks['apk_zip_integrity']=z.testzip() is None
        checks['no_packaged_credentials']=not any(re.search(r'(?:\.p12|\.jks|\.keystore|signing\.properties)$',name) for name in z.namelist())
        native=sorted(name for name in z.namelist() if name.startswith('lib/') and name.endswith('.so'))
        abis=sorted({name.split('/')[1] for name in native})
        alignments={name:elf_load_alignments(z.read(name)) for name in native if name.split('/')[1] in ('arm64-v8a','x86_64')}
        checks['elf64_load_alignment_16kb']=all(values and all(n>=16384 and n&(n-1)==0 for n in values) for values in alignments.values())
        checks['native_abis_complete']=not native or set(abis)=={'arm64-v8a','armeabi-v7a','x86','x86_64'}
    result={'apk':str(apk),'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'size_bytes':apk.stat().st_size,'version_name':manifest['versionName'],'version_code':manifest['versionCode'],'min_sdk':sdk['minSdkVersion'],'target_sdk':sdk['targetSdkVersion'],'abis':abis,'elf64_load_alignments':alignments,'checks':checks,'permissions':permissions,'exported_components':exported,'native_libraries':native,'signature_verification':certificate,'limits':'Local configuration and signature review only. Not a malware scan, dependency CVE certification, Google review, or Play Protect approval.'}
    args.output.parent.mkdir(parents=True,exist_ok=True);args.output.write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps({'checks':checks,'sha256':result['sha256'],'report':str(args.output)},indent=2))
    if not all(checks.values()):raise SystemExit('Release security check failed.')
if __name__=='__main__':main()
