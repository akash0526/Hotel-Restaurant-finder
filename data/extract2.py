import osmium, json, csv
from collections import Counter
TYPES={'hotel','guest_house','hostel','motel','resort','apartment','chalet','homestay','bnb','love_hotel'}
KEYS=['tourism','name','name:en','addr:full','addr:street','addr:housenumber','addr:city','addr:district','addr:province','addr:postcode','addr:suburb','addr:ward','phone','contact:phone','mobile','contact:mobile','email','contact:email','website','contact:website','contact:facebook','facebook','contact:instagram','contact:twitter','stars','rooms','beds','opening_hours','operator','brand','wikidata','wikipedia','image','description','internet_access','wheelchair','check_date','note']
rows=[]
class H(osmium.SimpleHandler):
    def node(self,n):
        t=n.tags
        if t.get('tourism') in TYPES:
            rows.append((n.id,'node',n.location.lat,n.location.lon,{k:t[k] for k in KEYS if k in t}))
    def way(self,w):
        t=w.tags
        if t.get('tourism') in TYPES:
            try:
                pts=[(nd.location.lat,nd.location.lon) for nd in w.nodes if nd.location.valid()]
                if pts: rows.append((w.id,'way',sum(p[0] for p in pts)/len(pts),sum(p[1] for p in pts)/len(pts),{k:t[k] for k in KEYS if k in t}))
            except Exception as e: pass
h=H()
h.apply_file('data/nepal-latest.osm.pbf', locations=True, idx='sparse_file_array,/home/user/.cache/npl.loc.cache')
print("POIs:",len(rows))
json.dump(rows, open('data/osm_raw.json','w'), ensure_ascii=False)
c=Counter(r[4].get('tourism') for r in rows); print(c)
KR=['phone','contact:phone','mobile','email','contact:email','website','contact:website','contact:facebook','stars','rooms','beds','addr:street','addr:city','opening_hours','image','wikidata','description']
for k in KR:
    print(f"  {k:18s} {sum(1 for r in rows if k in r[4])}")
