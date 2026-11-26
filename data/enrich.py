import json, re, math, unicodedata
from collections import Counter, defaultdict
import requests

FIX={'Synagja':'Syangja','Kabherepalanchok':'Kavrepalanchok','Chitawan':'Chitwan','Nawalapur':'Nawalparasi East',
     'Nawalparasi':'Nawalparasi West','Tanahu':'Tanahun','Sindhupalchok':'Sindhupalchowk','Dadeidhura':'Dadeldhura',
     'Baijura':'Bajura','Kapilbastu':'Kapilvastu','Rukum_E':'Eastern Rukum','Rukum_W':'Western Rukum',
     'Tehrathum':'Terhathum','Terhathum':'Terhathum'}
recs=json.load(open('data/stage1.json'))
for r in recs:
    r['district']=FIX.get(r['district'],r['district'])

GENERIC={'hotel','hotels','guest','guesthouse','gh','house','lodge','resort','inn','the','and','pvt',
 'ltd','private','limited','nepal','restaurant','bar','home','stay','homestay','apartment','suites',
 'hostel','by','a','of','at','on','co','company','group','banquet','international','intl'}
def norm(s):
    s=unicodedata.normalize('NFKD',s or '').encode('ascii','ignore').decode().lower()
    return re.sub(r'[^a-z0-9 ]',' ',s).split()
def core(s): return [t for t in norm(s) if t not in GENERIC and len(t)>1]

def dist_km(a,b,c,d):
    R=6371; p=math.radians
    dl=p(c-a); dn=p(d-b)
    x=math.sin(dl/2)**2+math.cos(p(a))*math.cos(p(c))*math.sin(dn/2)**2
    return 2*R*math.asin(math.sqrt(x))

# --- Wikidata ---
wd=json.load(open('data/wikidata_hotels.json'))
wdh=defaultdict(lambda:{'img':set(),'site':set(),'phone':set(),'coord':None,'addr':set(),'stars':set(),'label':'','email':set()})
for b in wd:
    q=b['h']['value'].split('/')[-1]; a=wdh[q]
    a['label']=b.get('hLabel',{}).get('value','')
    for f in ('img','site','phone','addr','stars'):
        if f in b: a[f].add(b[f]['value'])
    if 'coord' in b and not a['coord']:
        v=b['coord']['value'].replace('Point(','').replace(')','').split(); a['coord']=(float(v[1]),float(v[0]))

# --- Commons images: group by inferred hotel from categories/title ---
ci=json.load(open('data/commons_hotel_images.json'))
HOTELCAT=re.compile(r'(hotels?|resorts?|guest\s?houses?|lodges?|hostels?|inn)\b',re.I)
SCENERY=re.compile(r'(street|road|view|panorama|landscape|mountain|trek|trail|municipal|town|village|river|sunset|temple|stupa|square|bazaar|signs in)',re.I)
imgs=defaultdict(list)
for x in ci:
    if not x['url']: continue
    cand=None
    for c in x['cats']:
        n=c.replace('Category:','')
        if HOTELCAT.search(n) or re.search(r'^Category:[A-Z][\w\'\.\s&-]{2,40}(,|$)',n) and re.search(r'(Hotel|Resort|Guest|Lodge|Inn|Hostel)',n,re.I):
            if not SCENERY.search(n) or re.search(r'(Hotel|Resort|Guest House|Lodge|Inn|Hostel)',n,re.I):
                cand=n; break
    key=cand or re.sub(r'^File:','',x['title'])
    imgs[key].append(x)

def best_photo(lst):
    lst=sorted(lst,key=lambda x:-(x.get('w') or 0))
    return lst[0] if lst else None

# index commons keys by core tokens
cindex=[]
for k,v in imgs.items():
    cindex.append((set(core(k)),k,v))
# index wikidata
windex=[]
for q,a in wdh.items():
    windex.append((set(core(a['label'])),q,a))

from rapidfuzz import fuzz
def match(name,lat,lon):
    ct=set(core(name))
    if not ct: return (0,None,None),(0,None,None)
    bestw=(0,None,None)
    for wt,q,a in windex:
        if not wt: continue
        overlap=len(ct&wt)/max(1,min(len(ct),len(wt)))
        sc=overlap*0.6+fuzz.token_set_ratio(' '.join(sorted(ct)),' '.join(sorted(wt)))/100*0.4
        if a['coord']:
            dd=dist_km(lat,lon,*a['coord'])
            if dd>15: continue
            sc+= 0.25 if dd<0.6 else 0.1
        elif overlap<1.0: continue
        if sc>bestw[0]: bestw=(sc,q,a)
    bestc=(0,None,None)
    for ctk,k,v in cindex:
        if not ctk: continue
        overlap=len(ct&ctk)/max(1,min(len(ct),len(ctk)))
        sc=overlap*0.6+fuzz.token_set_ratio(' '.join(sorted(ct)),' '.join(sorted(ctk)))/100*0.4
        geo=[x for x in v if x.get('lat')]
        if geo: 
            dd=min(dist_km(lat,lon,x['lat'],x['lon']) for x in geo)
            if dd>25: continue
            sc+=0.25 if dd<1 else 0.1
        elif overlap<1.0: continue
        if sc>bestc[0]: bestc=(sc,k,v)
    return bestw,bestc

ph_count=0; wd_count=0
for r in recs:
    r['photo_url']=''; r['photo_page']=''; r['photo_author']=''; r['photo_license']=''
    r['photo_source']=''; r['wikidata_qid']=r.get('wikidata','')
    if not r['name'] or len(r['name'])<3: continue
    bw,bc=match(r['name'],r['lat'],r['lon'])
    # photo: prefer commons, else wikidata
    if bc and (bc[0]>=1.05 or (bc[0]>=0.9 and bc[1])):
        p=best_photo(bc[2])
        if p and p.get('thumb') or (p and p.get('url')):
            r['photo_url']=p.get('thumb') or p['url']; r['photo_page']=p.get('url','')
            r['photo_author']=re.sub('<[^>]+>','',p.get('author','')).strip()
            r['photo_license']=p.get('license',''); r['photo_source']='Wikimedia Commons'; ph_count+=1
    if bw and (bw[0]>=1.05 or (bw[0]>=0.9 and bw[1])):
        a=bw[2]
        if not r['photo_url'] and a['img']:
            fn=sorted(a['img'])[0].split('/')[-1].replace('_',' ')
            url='https://commons.wikimedia.org/wiki/Special:FilePath/'+requests.utils.quote(fn)+'?width=1200'
            r['photo_url']=url; r['photo_page']='https://commons.wikimedia.org/wiki/File:'+requests.utils.quote(fn)
            r['photo_source']='Wikidata/Wikimedia'; r['photo_license']='see Commons page'; ph_count+=1
        if not r['wikidata_qid']: r['wikidata_qid']=bw[1]
        if not r['website'] and a['site']: r['website']=sorted(a['site'])[0]
        if not r['phone'] and a['phone']: r['phone']=sorted(a['phone'])[0]
        if not r['stars'] and a['stars']: r['stars']=sorted(a['stars'])[0]
        wd_count+=1
print('photos assigned:',ph_count,'| wikidata matched:',wd_count)
json.dump(recs,open('data/stage2.json','w'),ensure_ascii=False)
named=[r for r in recs if r['name']]
print('named hotels:',len(named))
print('with phone:',sum(1 for r in recs if r['phone']),'| mobile:',sum(1 for r in recs if r['mobile']))
print('with email:',sum(1 for r in recs if r['email']),'| website:',sum(1 for r in recs if r['website']),'| fb:',sum(1 for r in recs if r['facebook']))
print('with photo:',sum(1 for r in recs if r['photo_url']))
for r in recs[:5]: print(r['name'],'|',r['district'],'|',r['photo_url'][:80])
