import osmium, json, csv
from collections import Counter

TYPES = {'hotel','guest_house','hostel','motel','resort','apartment','chalet','homestay','bnb','love_hotel'}
rows=[]; tagcount=Counter()

class H(osmium.SimpleHandler):
    def __init__(self):
        super().__init__(); self.n=0
    def _emit(self, o, lat, lon):
        t=o.tags
        typ=t.get('tourism','')
        if typ not in TYPES: return
        rows.append(dict(
            osm_type=o.__class__.__name__.replace('Handler','').lower(),
            osm_id=o.id, category=typ,
            name=t.get('name',''), name_en=t.get('name:en',''),
            lat=round(lat,7), lon=round(lon,7),
            addr_full=t.get('addr:full',''), street=t.get('addr:street',''),
            housenumber=t.get('addr:housenumber',''), city=t.get('addr:city',''),
            district=t.get('addr:district',''), province_tag=t.get('addr:province',''),
            postcode=t.get('addr:postcode',''),
            phone=t.get('phone') or t.get('contact:phone',''),
            mobile=t.get('mobile') or t.get('contact:mobile',''),
            email=t.get('email') or t.get('contact:email',''),
            website=t.get('website') or t.get('contact:website',''),
            facebook=t.get('contact:facebook','') or t.get('facebook',''),
            instagram=t.get('contact:instagram',''),
            stars=t.get('stars',''), rooms=t.get('rooms',''), beds=t.get('beds',''),
            opening_hours=t.get('opening_hours',''),
            operator=t.get('operator',''), brand=t.get('brand',''),
            wikidata=t.get('wikidata',''), wikipedia=t.get('wikipedia',''),
            image=t.get('image',''), description=t.get('description',''),
            internet_access=t.get('internet_access',''), wheelchair=t.get('wheelchair',''),
            email2=t.get('contact:email',''),
        ))
        for k in ('phone','contact:phone','mobile','contact:mobile','email','contact:email','website','contact:website','contact:facebook','contact:instagram','stars','rooms','beds','addr:street','addr:city','opening_hours','image','wikidata','description'):
            if t.get(k): tagcount[k]+=1
    def node(self,n):
        if n.tags.get('tourism') in TYPES:
            self._emit(n, n.location.lat, n.location.lon)
    def area(self,a):
        pass
    def way(self,w):
        if w.tags.get('tourism') in TYPES:
            try:
                lats=[nd.location.lat for nd in w.nodes if nd.location.valid()]
                lons=[nd.location.lon for nd in w.nodes if nd.location.valid()]
                if lats: self._emit(w, sum(lats)/len(lats), sum(lons)/len(lons))
            except Exception: pass

h=H(); h.apply_file('data/nepal-latest.osm.pbf', locations=True)
print("total lodging POIs:", len(rows))
print(Counter(r['category'] for r in rows))
print("tag completeness (count of POIs with tag):")
for k,v in tagcount.most_common(): print(f"  {k:22s} {v}")
json.dump(rows, open('data/osm_lodging.json','w'), ensure_ascii=False)
