import json, re, unicodedata, math
from collections import Counter, defaultdict
from shapely.geometry import shape, Point
from shapely.strtree import STRtree

rows=json.load(open('data/osm_raw.json'))
adm2=json.load(open('data/adm2.geojson'))['features']
adm1=json.load(open('data/adm1.geojson'))['features']

def geoms(feats):
    g=[];names=[]
    for f in feats:
        try: g.append(shape(f['geometry'])); names.append(f['properties'].get('shapeName',''))
        except Exception: pass
    return g,names,STRtree(g)
g2,n2,t2=geoms(adm2); g1,n1,t1=geoms(adm1)

def locate(lat,lon):
    p=Point(lon,lat); d=prov=''
    for i in t2.query(p):
        if g2[i].contains(p): d=n2[i]; break
    for i in t1.query(p):
        if g1[i].contains(p): prov=n1[i]; break
    return d,prov

# province name normalisation -> official names
PROV={'Biratnagar':'Koshi','Janakpur':'Madhesh','Bagmati':'Bagmati','Gandaki':'Gandaki',
      'Lumbini':'Lumbini','Karnali':'Karnali','Sudurpaschim':'Sudurpaschim'}

GENERIC={'hotel','hotels','guest','guesthouse','gh','house','lodge','resort','inn','the','and','&',
         'pvt','pvt.','ltd','ltd.','private','limited','nepal','restaurant','bar','home','stay','homestay',
         'apartment','apartments','suites','hostel','by','a','of','at','on','co','co.','company','group'}

def norm(s):
    s=unicodedata.normalize('NFKD',s or '').encode('ascii','ignore').decode().lower()
    s=re.sub(r"[^a-z0-9 ]",' ',s)
    return ' '.join(s.split())
def core(s):
    return {t for t in norm(s).split() if t not in GENERIC and len(t)>1}

out=[]
for oid,otype,lat,lon,t in rows:
    d,prov=locate(lat,lon)
    name=t.get('name') or t.get('name:en') or ''
    rec={
      'osm_id':oid,'osm_type':otype,'name':name,'name_en':t.get('name:en',''),
      'category':t.get('tourism',''),
      'lat':round(lat,6),'lon':round(lon,6),'district':d or t.get('addr:district',''),
      'province':PROV.get(prov,prov) or t.get('addr:province',''),
      'city':t.get('addr:city',''),'street':t.get('addr:street',''),
      'housenumber':t.get('addr:housenumber',''),'postcode':t.get('addr:postcode',''),
      'address_full':t.get('addr:full',''),
      'phone':(t.get('phone') or t.get('contact:phone','')),
      'mobile':(t.get('mobile') or t.get('contact:mobile','')),
      'email':(t.get('email') or t.get('contact:email','')),
      'website':(t.get('website') or t.get('contact:website','')),
      'facebook':t.get('contact:facebook','') or t.get('facebook',''),
      'instagram':t.get('contact:instagram',''),'twitter':t.get('contact:twitter',''),
      'stars':t.get('stars',''),'rooms':t.get('rooms',''),'beds':t.get('beds',''),
      'opening_hours':t.get('opening_hours',''),'operator':t.get('operator',''),
      'brand':t.get('brand',''),'description':t.get('description',''),
      'wikidata':t.get('wikidata',''),'wikipedia':t.get('wikipedia',''),
      'internet_access':t.get('internet_access',''),'wheelchair':t.get('wheelchair',''),
      'image_osm':t.get('image',''),'check_date':t.get('check_date',''),
      'osm_url':f"https://www.openstreetmap.org/{otype}/{oid}",
      '_tokens':sorted(core(name)),
    }
    out.append(rec)
print('located:',sum(1 for r in out if r['district']),'/',len(out))
print('no district:',sum(1 for r in out if not r['district']))
print(Counter(r['district'] for r in out).most_common(12))
json.dump(out,open('data/stage1.json','w'),ensure_ascii=False)
