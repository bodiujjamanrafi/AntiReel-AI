"""Fail closed on common secret formats in staged files or reachable Git history.
Only report paths and rule names, never matched secret values. Not a certification.
"""
import argparse,json,re,subprocess
from pathlib import Path
PATTERNS={
 'private-key':re.compile(rb'-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----'),
 'github-token':re.compile(rb'\b(?:gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{30,})\b'),
 'aws-access-key':re.compile(rb'\b(?:AKIA|ASIA)[0-9A-Z]{16}\b'),
 'google-api-key':re.compile(rb'\bAIza[0-9A-Za-z_-]{30,}\b'),
 'slack-token':re.compile(rb'\bxox[baprs]-[0-9A-Za-z-]{20,}\b'),
 'credential-url':re.compile(rb'https?://[^\s/\x22\x27]+:[^\s/@\x22\x27]+@'),
 'assigned-secret':re.compile(rb'(?im)^\s*(?:export\s+)?(?:[A-Z_]*(?:API_TOKEN|API_SECRET|PRIVATE_KEY|CLIENT_SECRET|ACCESS_TOKEN|PASSWORD)|storePassword|keyPassword)\s*[=:]\s*[\x22\x27]?([^\s\x22\x27]{8,})')}
PLACEHOLDERS=(b'REPLACE_LOCALLY',b'YOUR_',b'EXAMPLE_',b'CHANGEME',b'process.env.',b'os.environ',b'${',b'getProperty(',b'System.getenv(')
FORBIDDEN_SUFFIXES={'.jks','.keystore','.p12','.pfx','.pem','.key'}
def findings(path,data):
 issues=[]
 p=Path(path)
 if p.suffix.lower() in FORBIDDEN_SUFFIXES or p.name in {'antireel-signing.properties','local.properties','.env'} or (p.name.startswith('.env.') and p.name!='.env.example'):
  issues.append('forbidden-secret-file')
 for name,rule in PATTERNS.items():
  for match in rule.finditer(data):
   if name=='assigned-secret' and any(token in match[0] for token in PLACEHOLDERS):continue
   issues.append(name);break
 return sorted(set(issues))
def git(*args):return subprocess.check_output(['git',*args])
def main():
 parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--history',action='store_true');args=parser.parse_args()
 issues=[];checked=0
 if args.history:
  # Reachable commits only; unreferenced Git objects are not pushed by normal push.
  objects=git('rev-list','--objects','--all').decode().splitlines()
  for entry in objects:
   parts=entry.split(' ',1)
   if len(parts)!=2:continue
   oid,path=parts
   if git('cat-file','-t',oid).strip()!=b'blob':continue
   checked+=1
   for rule in findings(path,git('cat-file','blob',oid)):issues.append({'path':path,'rule':rule})
 else:
  paths=git('diff','--cached','--name-only','--diff-filter=ACMR','-z').decode().split('\0')
  for path in filter(None,paths):
   checked+=1
   for rule in findings(path,git('show',':'+path)):issues.append({'path':path,'rule':rule})
 print(json.dumps({'scope':'reachable-history' if args.history else 'staged-files','checked_blobs':checked,'findings':issues,'limits':'Pattern scan only. Manual review and exclusion rules are also required.'},indent=2))
 return 1 if issues else 0
if __name__=='__main__':raise SystemExit(main())
