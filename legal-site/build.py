"""Build dependency-free legal pages and bundled Android copies from reviewed JSON."""
import html,json,shutil
from datetime import datetime
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
SITE=ROOT/'legal-site'
OUT=SITE/'dist'
SLUGS=('privacy','terms','security','support')
def escape(value):return html.escape(value,quote=True)
def build():
 OUT.mkdir(exist_ok=True)
 shutil.copy2(SITE/'styles.css',OUT/'styles.css')
 brand=ROOT/'app/src/main/res/drawable-nodpi/miso_brand_png.png'
 shutil.copy2(brand,OUT/'miso.png')
 for slug in SLUGS:
  doc=json.loads((SITE/'content'/f'{slug}.json').read_text())
  # Keep native and public wording identical, including retention limitations.
  asset=ROOT/'app/src/main/assets/legal'/f'{slug}.json'
  asset.parent.mkdir(parents=True,exist_ok=True)
  asset.write_text(json.dumps(doc,ensure_ascii=False,indent=2)+'\n')
  updated_iso=datetime.strptime(doc['updated'],'%d %B %Y').date().isoformat()
  nav=''.join(f'<a href="/{s}"'+(' aria-current="page"' if s==slug else '')+f'>{label}</a>' for s,label in zip(SLUGS,('Privacy','Terms','Security','Support')))
  toc=''.join(f'<li><a href="#section-{i}">{escape(s["title"])}</a></li>' for i,s in enumerate(doc['sections']))
  sections=''.join(f'<section id="section-{i}"><h2>{escape(s["title"])}</h2>'+''.join(f'<p>{escape(p)}</p>' for p in s['paragraphs'])+'</section>' for i,s in enumerate(doc['sections']))
  markup=f'''<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="color-scheme" content="dark light"><meta name="description" content="{escape(doc['summary'])}"><title>{escape(doc['title'])} — AntiReel AI</title><link rel="canonical" href="https://legal.imrafi.bond/{slug}"><link rel="icon" href="/miso.png" type="image/png"><link rel="stylesheet" href="/styles.css"></head>
<body><a class="skip" href="#content">Skip to content</a><header><div class="header-inner"><a class="brand" href="/privacy"><img src="/miso.png" width="42" height="42" alt=""><span>antireel<span class="brand-note">Legal & support</span></span></a><nav aria-label="Legal pages">{nav}</nav></div></header>
<div class="page"><aside aria-label="On this page"><span class="eyebrow">On this page</span><ol>{toc}</ol><a class="contact" href="mailto:hello@imrafi.com">hello@imrafi.com ↗</a></aside><main id="content"><div class="intro"><span class="eyebrow">AntiReel AI · Android</span><h1>{escape(doc['title'])}</h1><p class="summary">{escape(doc['summary'])}</p><p class="updated">Updated <time datetime="{updated_iso}">{escape(doc['updated'])}</time></p></div><article>{sections}</article><div class="support-note"><h2>Need a hand?</h2><p>Contact the AntiReel AI developer at <a href="mailto:hello@imrafi.com">hello@imrafi.com</a>. Share only what is needed to help with your request.</p></div></main></div><footer><span>AntiReel AI</span><span>Local boundaries. Your choice.</span><a href="mailto:hello@imrafi.com">Contact support</a></footer></body></html>'''
  (OUT/f'{slug}.html').write_text(markup)
 (OUT/'index.html').write_text((OUT/'privacy.html').read_text())
 (OUT/'404.html').write_text('<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Page not found — AntiReel AI</title><link rel="stylesheet" href="/styles.css"><main class="not-found"><h1>Page not found</h1><p><a href="/privacy">Privacy</a> · <a href="/terms">Terms</a> · <a href="/security">Security</a> · <a href="/support">Support</a></p></main></html>')
 # Explicit rewrite routes work on Pages without a client router or login.
 (OUT/'_redirects').write_text(''.join(f'/{s} /{s}.html 200\n' for s in SLUGS))
 shutil.copy2(SITE/'_headers',OUT/'_headers')
 (OUT/'robots.txt').write_text('User-agent: *\nAllow: /\nSitemap: https://legal.imrafi.bond/sitemap.xml\n')
 (OUT/'sitemap.xml').write_text('<?xml version="1.0" encoding="UTF-8"?><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">'+''.join(f'<url><loc>https://legal.imrafi.bond/{s}</loc></url>' for s in SLUGS)+'</urlset>')
 print(f'Built {len(SLUGS)} legal pages in {OUT}')
if __name__=='__main__':build()
