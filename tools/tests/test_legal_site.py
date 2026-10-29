import hashlib,json,re,unittest,html
from pathlib import Path
from html.parser import HTMLParser
ROOT=Path(__file__).resolve().parents[2]
class Tags(HTMLParser):
 def __init__(self):super().__init__();self.tags=[]
 def handle_starttag(self,tag,attrs):self.tags.append((tag,dict(attrs)))
class LegalSiteTest(unittest.TestCase):
 def test_all_documents_match_android_and_have_working_static_routes(self):
  redirects=(ROOT/'legal-site/dist/_redirects').read_text().splitlines()
  for slug in ('privacy','terms','security','support'):
   a=(ROOT/f'legal-site/content/{slug}.json').read_bytes();b=(ROOT/f'app/src/main/assets/legal/{slug}.json').read_bytes()
   self.assertEqual(a,b)
   doc=json.loads(a);self.assertEqual('hello@imrafi.com',doc['contact'])
   self.assertIn(f'/{slug} /{slug}.html 200',redirects)
   page=(ROOT/f'legal-site/dist/{slug}.html').read_text()
   self.assertIn(doc['title'],html.unescape(page));self.assertIn('hello@imrafi.com',page)
   self.assertNotIn('contenteditable',page);self.assertNotIn('user-select:none',page)
 def test_no_javascript_trackers_forms_external_assets_or_authentication(self):
  for file in (ROOT/'legal-site/dist').glob('*.html'):
   parser=Tags();parser.feed(file.read_text())
   self.assertFalse(any(t in ('script','iframe','form','input') for t,_ in parser.tags),file.name)
   for tag,attrs in parser.tags:
    for key in ('src','href'):
     value=attrs.get(key,'')
     if key=='src' or tag=='link' and attrs.get('rel')!='canonical':
      self.assertFalse(value.startswith(('https:','http:','//')),value)
      if value.startswith('/'):
       self.assertTrue((ROOT/'legal-site/dist'/value[1:]).is_file(),value)
 def test_security_headers_and_keyboard_navigation_are_present(self):
  headers=(ROOT/'legal-site/dist/_headers').read_text()
  for token in ("script-src 'none'","connect-src 'none'","frame-ancestors 'none'",'Referrer-Policy: no-referrer','X-Content-Type-Options: nosniff'):
   self.assertIn(token,headers)
  for slug in ('privacy','terms','security','support'):
   page=(ROOT/f'legal-site/dist/{slug}.html').read_text()
   self.assertIn('href="#content"',page);self.assertIn('aria-current="page"',page)
   self.assertIn('name="viewport"',page);self.assertIn('lang="en"',page)
 def test_privacy_retention_and_external_service_limits_are_not_hidden(self):
  content=(ROOT/'legal-site/content/privacy.json').read_text()
  for text in ('subsequent tracking writes','text-to-speech','email','no Internet permission','no fixed provider-log retention','not saved as content'):
   self.assertIn(text,content)
if __name__=='__main__':unittest.main()
