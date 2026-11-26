import json, re, math, unicodedata
from collections import Counter, defaultdict
from shapely.geometry import shape, Point
from shapely.strtree import STRtree

raw=json.load(open('data/osm_raw.json'))
old=json.load(open('data/stage6.json'))
oldmap={(r['osm_id'],r['osm_type']):r for r in old}

adm2=json.load(open('data/npl_adm/npl_admin2.geojson'))['features']
g=[shape(f['geometry']) for f in adm2]
dn=[f['properties']['adm2_name'] for f in adm2]
dp=[f['properties']['adm1_name'] for f in adm2]
dpc=[f['properties']['adm2_pcode'] for f in adm2]
t=STRtree(g)
PROVFIX={'Bagmati':'Bagmati','Gandaki':'Gandaki','Lumbini':'Lumbini','Karnali':'Karnali',
 'Sudurpashchim':'Sudurpashchim','Sudurpaschim':'Sudurpashchim','Madhesh':'Madhesh','Koshi':'Koshi'}
def locate(lat,lon):
    p=Point(lon,lat)
    for i in t.query(p):
        if g[i].contains(p): return dn[i], PROVFIX.get(dp[i],dp[i]), dpc[i]
    # nearest fallback
    best=None;bd=1e9
    for i in range(len(g)):
        d=g[i].distance(p)
        if d<bd: bd=d;best=i
    return (dn[best],PROVFIX.get(dp[best],dp[best]),dpc[best]) if bd<0.6 else ('','','')

out=[]
unloc=0
for oid,otype,lat,lon,tg in raw:
    d,prov,pc=locate(lat,lon)
    if not d: unloc+=1
    o=oldmap.get((oid,otype),{})
    r=dict(o)
    r.update(osm_id=oid,osm_type=otype,lat=round(lat,6),lon=round(lon,6),
             latitude=round(lat,6),longitude=round(lon,6),
             district=d,province=prov,district_pcode=pc,
             category=tg.get('tourism',''),name=tg.get('name') or tg.get('name:en',''))
    out.append(r)
print('relocated:',len(out),'| unlocated:',unloc)
print('Rupandehi:',sum(1 for r in out if r['district']=='Rupandehi'),'| Kaski Pokhara-area:',sum(1 for r in out if r['district']=='Kaski'))
print(Counter(r['province'] for r in out).most_common())
json.dump(out,open('data/rebuilt.json','w'),ensure_ascii=False)
