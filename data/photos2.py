import json, re, math, unicodedata
from collections import defaultdict
recs=json.load(open('data/stage3.json'))
ci=json.load(open('data/commons_hotel_images.json'))

GENERIC={'hotel','hotels','guest','guesthouse','gh','house','lodge','resort','inn','the','and','pvt','ltd',
 'private','limited','nepal','restaurant','bar','home','stay','homestay','apartment','suites','hostel',
 'by','a','of','at','on','co','company','group','banquet','international','intl','building','buildings','cafe','café'}
PLACE={'kathmandu','pokhara','lalitpur','bhaktapur','nepal','thamel','lazimpat','patan','kaski','solukhumbu','mustang','manang','chitwan','bandipur','nagarkot','annapurna','everest','lumbini','bhaktapur','dulikhel','dhulikhel','gorkha','palpa','tansen','janakpur','biratnagar','dharan','butwal','bhairahawa','nepalgunj','kohalpur','birtamode','jhapa','bara','simara','rupandehi','banke','surkhet','damside','lakeside','boudha','swayambhu','durbar','marg','road'}
SCENERY=re.compile(r'(way to|trek|trail|street|road|panorama|landscape|mountain|view from|sunset|sunrise|town|village|river|temple|stupa|square|bazaar|signs? in|airport|interior|\bof nepal\b$|building)',re.I)
HOTELISH=re.compile(r'(hotel|resort|guest\s?house|lodge|inn|hostel|homestay|retreat|suites|villa|dwarika|shanker)',re.I)
def norm(s):
    s=unicodedata.normalize('NFKD',s or '').encode('ascii','ignore').decode().lower()
    return re.sub(r'[^a-z0-9 ]',' ',s).split()
def sig(s, drop_place=True):
    o=[]
    for t in norm(s):
        if t in GENERIC or len(t)<2: continue
        if drop_place and t in PLACE: continue
        o.append(t)
    return o
def dist_km(a,b,c,d):
    R=6371;p=math.radians;dl=p(c-a);dn=p(d-b)
    x=math.sin(dl/2)**2+math.cos(p(a))*math.cos(p(c))*math.sin(dn/2)**2
    return 2*R*math.asin(math.sqrt(x))

# dedicated categories only: "Category:Hotel Shanker, Kathmandu" (not "Hotels in Kathmandu")
ded=defaultdict(list)
for x in ci:
    if not (x.get('thumb') or x.get('url')): continue
    t=re.sub(r'^File:','',x['title'])
    if SCENERY.search(t): continue
    if not x.get('w') or x['w']<450: continue
    for c in x['cats']:
        n=c.replace('Category:','')
        if not HOTELISH.search(n): continue
        if re.match(r'^(Hotels?|Resorts?|Guest ?houses?|Lodges?|Hostels?|Inns?|Homestays?)\s+(in|of|on)\b',n,re.I): continue
        if SCENERY.search(n): continue
        if len(sig(n))<1: continue
        ded[re.sub(r'\s*\(.*?\)\s*$','',n)].append(x); break

pool=[]
for k,v in ded.items():
    base=k.split(',')[0]
    st=set(sig(base))
    if not st: continue
    pool.append({'key':k,'tokens':st,'files':sorted(v,key=lambda f:(-(f.get('w') or 0)))})
print('dedicated hotel photo categories:',len(pool))
print(sorted(p['key'] for p in pool))

def best(r):
    rt=set(sig(r['name']))
    if not rt: return None
    cands=[]
    for p in pool:
        shared=rt & p['tokens']
        exact = p['tokens']==rt
        two = len(shared)>=2
        if not (exact or two): continue
        if len(p['tokens'])==1 and len(next(iter(p['tokens'])))<5: continue
        geo=[f for f in p['files'] if f.get('lat')]
        dmin=min((dist_km(r['lat'],r['lon'],f['lat'],f['lon']) for f in geo),default=None)
        if dmin is not None and dmin>20: continue
        score=(2 if exact else 0)+len(shared)+(1 if (dmin is not None and dmin<3) else 0)
        cands.append((score,p,dmin))
    if not cands: return None
    cands.sort(key=lambda c:-c[0])
    return cands[0][1]

for r in recs:
    r['photo_url']='';r['photo_page']='';r['photo_author']='';r['photo_license']='';r['photo_source']=''
    if not r.get('name') or len(r['name'])<4: continue
    p=best(r)
    if not p: continue
    f=p['files'][0]
    r['photo_url']=f.get('thumb') or f['url']; r['photo_page']=f['url'].split('?')[0]
    r['photo_author']=re.sub('<[^>]+>','',f.get('author','')).strip()[:200]
    r['photo_license']=f.get('license',''); r['photo_source']='Wikimedia Commons'
json.dump(recs,open('data/stage4.json','w'),ensure_ascii=False)
m=[r for r in recs if r['photo_url']]
print('\nmatched POIs:',len(m))
for r in sorted(m,key=lambda x:x['name']):
    print(f"  {r['name'][:32]:34s}|{r['district'][:12]:13s}|{r['photo_page'].split('/')[-1][:52]}")
