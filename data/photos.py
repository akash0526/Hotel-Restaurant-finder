import json, re, math, unicodedata
from collections import defaultdict
recs=json.load(open('data/stage2.json'))
ci=json.load(open('data/commons_hotel_images.json'))

GENERIC={'hotel','hotels','guest','guesthouse','gh','house','lodge','resort','inn','the','and','pvt',
 'ltd','private','limited','nepal','restaurant','bar','home','stay','homestay','apartment','suites',
 'hostel','by','a','of','at','on','co','company','group','banquet','international','intl','building','buildings'}
LOCWORDS={'kathmandu','pokhara','lalitpur','bhaktapur','nepal','thamel','lazimpat','durbar','marg','chitwan',
 'solukhumbu','kaski','mustang','manang','bandipur','nagarkot','patan','boudha','swayambhu','lumbini','jhapa',
 'rupandehi','banke','nepalgunj','biratnagar','dharan','dhulabari','birtamode','simara','bara','chitawan','east','west'}
def norm(s):
    s=unicodedata.normalize('NFKD',s or '').encode('ascii','ignore').decode().lower()
    return re.sub(r'[^a-z0-9 ]',' ',s).split()
def sig(s, drop_loc=True):
    out=[]
    for t in norm(s):
        if t in GENERIC: continue
        if drop_loc and t in LOCWORDS: continue
        if len(t)<2: continue
        out.append(t)
    return out

BADNAME=re.compile(r'(street|road|panorama|landscape|mountain views?|trek|trail|town|village|river|sunset|sunrise|temple|stupa|square|bazaar|signs? in|buildings? in|airport|interior of the|garden of$|^category:hotels? in|^category:resorts? in|^category:guesthouses? in|^category:hotels? on)',re.I)
HOTELISH=re.compile(r'(hotel|resort|guest\s?house|lodge|inn|hostel|homestay|retreat|suites|villa)',re.I)

def dist_km(a,b,c,d):
    R=6371; p=math.radians
    dl=p(c-a); dn=p(d-b)
    x=math.sin(dl/2)**2+math.cos(p(a))*math.cos(p(c))*math.sin(dn/2)**2
    return 2*R*math.asin(math.sqrt(x))

# Build strict pools: specific hotel categories + clearly-named files
pool=defaultdict(list)
for x in ci:
    if not (x.get('thumb') or x.get('url')): continue
    if BADNAME.search(x['title'].replace('File:','')): continue
    cat=None
    for c in x['cats']:
        n=c.replace('Category:','')
        if HOTELISH.search(n) and not BADNAME.search(n) and len(sig(n,False))>=1:
            cat=n; break
    key=None
    if cat: key=re.sub(r'\s*\(.*?\)\s*$','',cat)
    else:
        t=re.sub(r'^File:','',x['title']); t=re.sub(r'[-_]\d+\.(jpg|jpeg|png|JPG)$','',t)
        if HOTELISH.search(t) and len(sig(t))>=1: key=t.replace('_',' ')
    if key: pool[key].append(x)

entries=[]
for k,v in pool.items():
    name=re.sub(r'\s*,\s*',', ',k)
    base=name.split(',')[0]
    entries.append({'key':k,'name':name,'base':base,'tokens':set(sig(base)),'all':set(sig(name)),'files':v})

def match(r):
    rt=set(sig(r['name']))
    if len(rt)==0: return None,None
    best=None; bestsc=0
    for e in entries:
        if not e['tokens']: continue
        inter=rt & e['all']
        if not inter: continue
        # strict: all significant tokens of the commons hotel name must appear in OSM name (or vice versa)
        strict = e['tokens'] <= rt or rt <= e['all']
        if not strict: continue
        sc=len(inter)/max(len(rt),len(e['tokens']))
        geo=[f for f in e['files'] if f.get('lat')]
        dmin=None
        if geo: dmin=min(dist_km(r['lat'],r['lon'],f['lat'],f['lon']) for f in geo)
        if dmin is not None:
            if dmin>12: continue
            sc += 0.5 if dmin<2 else 0.2
        else:
            # no geo: require exact set equality to be safe
            if not (e['tokens']==rt and len(rt)>=2): continue
        if sc>bestsc: bestsc=sc; best=e
    if not best: return None,None
    files=[f for f in best['files'] if not BADNAME.search(f['title'])]
    if not files: return None,None
    files.sort(key=lambda f:(-(f.get('w') or 0)))
    return best, files[0]

n=0
for r in recs:
    r['photo_url']=''; r['photo_page']=''; r['photo_author']=''; r['photo_license']=''; r['photo_source']=''
    if not r.get('name'): continue
    e,f=match(r)
    if f:
        r['photo_url']=f.get('thumb') or f['url']; r['photo_page']=f['url'].split('?')[0]
        r['photo_author']=re.sub('<[^>]+>','',f.get('author','')).strip()[:200]
        r['photo_license']=f.get('license',''); r['photo_source']='Wikimedia Commons'
        r['photo_verified']=True; n+=1
json.dump(recs,open('data/stage3.json','w'),ensure_ascii=False)
print('strict photo matches:',n)
for r in recs:
    if r.get('photo_url'): print(f"  {r['name'][:30]:32s}|{r['district'][:12]:13s}|{r['photo_page'].split('/')[-1][:60]}")
