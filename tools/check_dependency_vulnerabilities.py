"""Query OSV using public Maven coordinates only. Never uploads APKs or user data."""
import datetime,json,urllib.request
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
def main():
    packages=json.loads((ROOT/'docs/release-dependencies.json').read_text())
    queries=[{'package':{'ecosystem':'Maven','name':p['name']},'version':p['version']} for p in packages]
    result={'checked_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'source':'https://api.osv.dev/v1/querybatch','packages_queried':len(packages),'status':'unavailable','findings':[],
        'limits':'Public Maven coordinates only; no APK/user data sent. Coverage is incomplete. Zero matches are not malware-free certification.'}
    try:
        request=urllib.request.Request(result['source'],data=json.dumps({'queries':queries}).encode(),headers={'Content-Type':'application/json'},method='POST')
        with urllib.request.urlopen(request,timeout=25) as response:body=json.load(response)
        results=body.get('results',[])
        if len(results)!=len(packages):raise ValueError('Incomplete OSV result set')
        result['status']='completed'
        for package,response in zip(packages,results):
            if response.get('vulns'):result['findings'].append({**package,'vulnerabilities':response['vulns']})
            if response.get('next_page_token'):
                result['status']='partial';result.setdefault('pagination_required',[]).append(package)
    except Exception as error:result['error']=str(error)
    (ROOT/'docs/dependency-vulnerability-audit.json').write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps({'status':result['status'],'packages':len(packages),'packages_with_matches':len(result['findings'])},indent=2))
    return 0 if result['status']=='completed' and not result['findings'] else 1
if __name__=='__main__':raise SystemExit(main())
