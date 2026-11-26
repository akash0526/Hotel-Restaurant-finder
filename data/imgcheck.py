import json, re, concurrent.futures as cf, requests, urllib.parse
requests.packages.urllib3.disable_warnings()
recs=json.load(open('data/stage5.json'))
UA={'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36'}
BAD=re.compile(r'(flag|logo|icon|sprite|favicon|placeholder|loader|default|noimage|no-image|blank|avatar|social|facebook|instagram|twitter|whatsapp|pixel|spacer|1x1|thumb-|button|banner-?ad)',re.I)
def check(r):
    u=r.get('photo_url','')
    if not u: return None
    if not re.search(r'\.(jpe?g|png|webp|avif)(\?|$)',u,re.I) and 'wikimedia' not in u and 'hotellinksolutions' not in u:
        return None
    if BAD.search(urllib.parse.urlparse(u).path.split('/')[-1]): return None
    try:
        h=requests.get(u,headers=UA,timeout=12,stream=True,verify=False)
        ct=h.headers.get('content-type','')
        if h.status_code>=400 or 'image' not in ct: return None
        size=int(h.headers.get('content-length') or 0)
        if size and size<12000: return None
        return u
    except Exception: return None
targets=[r for r in recs if r.get('photo_url')]
with cf.ThreadPoolExecutor(max_workers=24) as ex:
    res=list(ex.map(check,targets))
kept=0
for r,ok in zip(targets,res):
    if not ok:
        r['photo_url']=''; r['photo_page']=''; r['photo_source']=''; r['photo_license']=''; r['photo_author']=''
        r.pop('photo_gps_delta_km',None)
    else: kept+=1
json.dump(recs,open('data/stage6.json','w'),ensure_ascii=False)
print('validated live photos:',kept)
print('by source:', {s:sum(1 for r in recs if r.get('photo_source')==s) for s in set(r.get('photo_source','') for r in recs) if s})
