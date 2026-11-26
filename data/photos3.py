import json, re, math, unicodedata
from collections import defaultdict
recs=json.load(open('data/stage3.json'))
ci=json.load(open('data/commons_hotel_images.json'))

GENERIC={'hotel','hotels','guest','guesthouse','gh','house','lodge','lodges','resort','resorts','inn','the','and',
 'pvt','ltd','private','limited','nepal','restaurant','bar','home','stay','homestay','apartment','suites','hostel',
 'by','a','of','at','on','co','company','group','banquet','international','intl','building','buildings','cafe'}
# geographic / brand-agnostic names that must never be the sole basis of a match
GEO={'kathmandu','pokhara','lalitpur','bhaktapur','nepal','thamel','lazimpat','patan','kaski','solukhumbu','mustang',
 'manang','chitwan','bandipur','nagarkot','lumbini','dhulikhel','gorkha','palpa','tansen','janakpur','biratnagar',
 'dharan','butwal','bhairahawa','nepalgunj','kohalpur','birtamode','jhapa','bara','simara','rupandehi','banke',
 'surkhet','lakeside','boudha','swayambhu','durbar','marg','road','annapurna','everest','manaslu','kailash',
 'sagarmatha','lhotse','makalu','fishtail','machhapuchhre','ganesh','langtang','dolpo','tilicho','thorong','yak',
 'khumbu','panchase','sarangkot','dwarika','mountain','himalaya','himalayan','base','camp','view'}
SCENERY=re.compile(r'(way to|trek|trail|street|road|panorama|landscape|mountain|view from|sunset|sunrise|town|village|'
 r'river|temple|stupa|square|bazaar|sign in|signs in|airport|interior|\bof nepal\b$|chandelier|corridor|lobbies)',re.I)
HOTELISH=re.compile(r'(hotel|resort|guest\s?house|lodge|inn|hostel|homestay|retreat|suites|villa|dwarika|shanker|dreamhome)',re.I)
def norm(s):
    s=unicodedata.normalize('NFKD',s or '').encode('ascii','ignore').decode().lower()
    return re.sub(r'[^a-z0-9 ]',' ',s).split()
def sig(s):
    return [t for t in norm(s) if t not in GENERIC and len(t)>=3]
def dist_km(a,b,c,d):
    R=6371;p=math.radians;dl=p(c-a);dn=p(d-b)
    x=math.sin(dl/2)**2+math.cos(p(a))*math.cos(p(c))*math.sin(dn/2)**2
    return 2*R*math.asin(math.sqrt(x))

ded=defaultdict(list)
for x in ci:
    if not (x.get('thumb') or x.get('url')) or not x.get('w') or x['w']<500: continue
    t=re.sub(r'^File:','',x['title'])
    if SCENERY.search(t): continue
    for c in x['cats']:
        n=c.replace('Category:','')
        if not HOTELISH.search(n): continue
        if re.match(r'^(Hotels?|Resorts?|Guest ?houses?|Lodges?|Hostels?|Inns?|Homestays?)\s+(in|of|on)\b',n,re.I): continue
        if re.search(r'(chandelier|corridor|lobbies|interior)',n,re.I): continue
        base=n.split(',')[0]
        toks=set(sig(base))
        strong={t for t in toks if t not in GEO and len(t)>=4}
        if not strong: continue
        ded[re.sub(r'\s*\(.*?\)\s*$','',n)].append(x); break

pool=[]
for k,v in ded.items():
    base=k.split(',')[0]
    toks=set(sig(base)); strong={t for t in toks if t not in GEO and len(t)>=4}
    pool.append({'key':k,'tokens':toks,'strong':strong,'files':sorted(v,key=lambda f:-(f.get('w') or 0))})
print('usable dedicated photo sets:'); [print('  -',p['key'],len(p['files']),'files') for p in pool]

def match(r):
    rt=set(sig(r['name'])); rstrong={t for t in rt if t not in GEO and len(t)>=4}
    if not rstrong: return None
    cands=[]
    for p in pool:
        # the distinctive token must be shared
        if not (p['strong'] & rstrong): continue
        # and it must not be the only thing shared if that token is weakly distinctive
        if len(p['strong'] & rstrong)==1 and len(p['strong'])==1 and len(list(p['strong'])[0])<5: continue
        geo=[f for f in p['files'] if f.get('lat')]
        dmin=min((dist_km(r['lat'],r['lon'],f['lat'],f['lon']) for f in geo),default=None)
        if dmin is not None and dmin>20: continue   # wrong city -> reject
        score=len(p['strong']&rstrong)*2+len(p['tokens']&rt)+(2 if (dmin is not None and dmin<3) else 0)
        cands.append((score,dmin,p))
    if not cands: return None
    cands.sort(key=lambda c:-c[0]); return cands[0]

for r in recs:
    for k in ('photo_url','photo_page','photo_author','photo_license','photo_source'): r[k]=''
    if not r.get('name') or len(r['name'])<5: continue
    m=match(r)
    if not m: continue
    score,dmin,p=m
    f=p['files'][0]
    r['photo_url']=f.get('thumb') or f['url']; r['photo_page']=f['url'].split('?')[0]
    r['photo_author']=re.sub('<[^>]+>','',f.get('author','')).strip()[:200]
    r['photo_license']=f.get('license',''); r['photo_source']='Wikimedia Commons'
    r['photo_gps_delta_km']=round(dmin,1) if dmin is not None else None
json.dump(recs,open('data/stage4.json','w'),ensure_ascii=False)
m=[r for r in recs if r['photo_url']]
print('\nverified photo matches:',len(m))
for r in sorted(m,key=lambda x:x['name']):
    print(f"  {r['name'][:30]:32s}|{r['district'][:12]:13s}|{r['photo_gps_delta_km']}| {r['photo_page'].split('/')[-1][:46]}")
