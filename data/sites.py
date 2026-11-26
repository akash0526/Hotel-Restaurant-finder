import json, re, urllib.parse, concurrent.futures as cf
import requests
from bs4 import BeautifulSoup
recs=json.load(open('data/stage4.json'))
UA={'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36'}
def clean(u):
    u=u.strip()
    if not u: return ''
    if not u.startswith(('http://','https://')): u='http://'+u
    return u
def work(r):
    u=clean(r['website'])
    out={'og_image':'','site_title':'','site_desc':'','site_phone':'','site_email':'','site_addr':'','final_url':'','ok':False}
    if not u: return out
    try:
        resp=requests.get(u,headers=UA,timeout=12,allow_redirects=True,verify=False)
        if resp.status_code>=400: return out
        ctype=resp.headers.get('content-type','')
        if 'html' not in ctype: return out
        html=resp.text[:600000]
        s=BeautifulSoup(html,'html.parser')
        def meta(*names):
            for n in names:
                t=s.find('meta',attrs={'property':n}) or s.find('meta',attrs={'name':n})
                if t and t.get('content'): return t['content'].strip()
            return ''
        img=meta('og:image','og:image:url','twitter:image')
        if not img:
            for im in s.find_all('img'):
                src=im.get('src') or im.get('data-src') or ''
                if re.search(r'\.(jpe?g|png|webp)',src,re.I) and not re.search(r'(logo|icon|sprite|favicon|placeholder|loader)',src,re.I):
                    img=src; break
        if img and not img.startswith('http'):
            img=urllib.parse.urljoin(resp.url,img)
        txt=s.get_text(' ',strip=True)[:20000]
        ph=re.findall(r'(?:\+977[\s\-]?)?(?:0?1[\s\-]?\d{6,7}|9[6-8]\d{8}|\d{2}[\s\-]?\d{6,7})',txt)
        em=re.findall(r'[\w\.\-+]+@[\w\-]+\.[\w\.\-]{2,}',txt)
        out.update(og_image=img or '', site_title=(meta('og:title','twitter:title') or (s.title.get_text(strip=True) if s.title else ''))[:200],
                   site_desc=meta('og:description','description')[:400],
                   site_phone=ph[0].strip() if ph else '', site_email=em[0].strip() if em else '',
                   final_url=resp.url, ok=True)
        ad=s.find(attrs={'itemtype':re.compile('PostalAddress')}) 
        if ad: out['site_addr']=ad.get_text(' ',strip=True)[:200]
    except Exception:
        pass
    return out
targets=[r for r in recs if r['website']]
print('websites to fetch:',len(targets))
with cf.ThreadPoolExecutor(max_workers=24) as ex:
    for r,res in zip(targets, ex.map(work,targets)):
        r['site']=res
        if res['og_image'] and not r['photo_url']:
            r['photo_url']=res['og_image']; r['photo_source']='Hotel official website'; r['photo_page']=res['final_url']
        if not r['email'] and res['site_email']: r['email']=res['site_email']
        if not r['phone'] and res['site_phone']: r['phone']=res['site_phone']
        if not r['street'] and res['site_addr']: r['address_full']=r['address_full'] or res['site_addr']
        if res['site_desc'] and not r['description']: r['description']=res['site_desc']
json.dump(recs,open('data/stage5.json','w'),ensure_ascii=False)
ok=sum(1 for r in recs if r.get('site',{}).get('ok'))
print('sites reachable:',ok,'| with og:image:',sum(1 for r in recs if r.get('site',{}).get('og_image')))
print('total with photo now:',sum(1 for r in recs if r['photo_url']))
print('phone:',sum(1 for r in recs if r['phone']),'email:',sum(1 for r in recs if r['email']))
for r in [x for x in recs if x.get('site',{}).get('og_image')][:6]:
    print(' ',r['name'][:26],'|',r['photo_url'][:95])
