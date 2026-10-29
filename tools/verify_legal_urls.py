"""Read-only HTTPS/content checks. Does not provision hosting, change DNS or bypass TLS."""
import argparse,html,json,ssl,urllib.request
from urllib.parse import urlparse

def verify(timeout=5):
 results=[]
 for slug,title in [('privacy','Privacy Policy'),('terms','Terms of Service'),('security','Security & Permissions'),('support','Help & Support')]:
  url='https://legal.imrafi.bond/'+slug
  result={'url':url,'verified':False,'tls_certificate_verified':False}
  try:
   request=urllib.request.Request(url,headers={'User-Agent':'AntiReel-Legal-Verification/1.0'})
   with urllib.request.urlopen(request,timeout=timeout,context=ssl.create_default_context()) as response:
    body=response.read(1024*1024).decode('utf-8')
    checks={'status_200':response.status==200,'same_https_host':urlparse(response.url).scheme=='https' and urlparse(response.url).hostname=='legal.imrafi.bond','html':response.headers.get_content_type()=='text/html','correct_document':'<h1>'+html.escape(title)+'</h1>' in body,'canonical_url':'<link rel="canonical" href="'+url+'">' in body,'support_email':'hello@imrafi.com' in body,'csp':"script-src 'none'" in response.headers.get('Content-Security-Policy',''),'no_cookies':not response.headers.get('Set-Cookie'),'no_login_form':'<form' not in body.lower()}
    result.update({'checks':checks,'verified':all(checks.values()),'tls_certificate_verified':checks['same_https_host']})
  except Exception as error:result['error']=type(error).__name__+': '+str(error)
  results.append(result)
 return {'all_verified':all(r['verified'] for r in results),'pages':results,'note':'HTTPS uses default certificate/hostname validation. This does not prove worldwide availability or Google Play approval.'}
def main():
 parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--output',required=True);args=parser.parse_args()
 result=verify()
 with open(args.output,'w') as f:json.dump(result,f,indent=2);f.write('\n')
 print(json.dumps(result,indent=2));return 0 if result['all_verified'] else 1
if __name__=='__main__':raise SystemExit(main())
