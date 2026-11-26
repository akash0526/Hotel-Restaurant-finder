import requests, json, time
S=requests.Session(); S.headers.update({'User-Agent':'NepalHotelData/1.0 (tourism app research; contact: dev@example.com)'})
API='https://commons.wikimedia.org/w/api.php'
def members(cat, t='file|subcat'):
    out=[];cont={}
    while True:
        p={'action':'query','list':'categorymembers','cmtitle':cat,'cmlimit':'500','cmtype':t,'format':'json'}
        p.update(cont)
        r=S.get(API,params=p,timeout=40).json()
        out+=r.get('query',{}).get('categorymembers',[])
        if 'continue' in r: cont=r['continue']
        else: break
    return out
seen_cats=set(); files=set(); queue=['Category:Hotels in Nepal','Category:Hotel buildings in Nepal','Category:Resorts in Nepal','Category:Guesthouses in Nepal','Category:Hotels in Kathmandu','Category:Hotels in Pokhara']
lvl=0
while queue and lvl<4:
    nxt=[]
    for c in queue:
        if c in seen_cats: continue
        seen_cats.add(c)
        try: m=members(c)
        except Exception as e: print('err',c,e); continue
        for x in m:
            if x['ns']==14: nxt.append(x['title'])
            elif x['ns']==6: files.add(x['title'])
    queue=nxt; lvl+=1
    print(f'level {lvl}: cats={len(seen_cats)} files={len(files)}')
info=[]
fl=list(files)
for i in range(0,len(fl),50):
    chunk=fl[i:i+50]
    r=S.get(API,params={'action':'query','titles':'|'.join(chunk),'prop':'imageinfo|coordinates|categories','iiprop':'url|extmetadata|size','iiurlwidth':'1200','cllimit':'max','format':'json'},timeout=60).json()
    for pid,pg in r.get('query',{}).get('pages',{}).items():
        ii=pg.get('imageinfo',[{}])[0]
        em=ii.get('extmetadata',{})
        coord=pg.get('coordinates',[{}])[0] if pg.get('coordinates') else {}
        info.append({'title':pg['title'],'url':ii.get('url',''),'thumb':ii.get('thumburl',''),
            'w':ii.get('width'),'h':ii.get('height'),
            'author':em.get('Artist',{}).get('value','')[:300],'license':em.get('LicenseShortName',{}).get('value',''),
            'desc':em.get('ImageDescription',{}).get('value','')[:400],
            'lat':coord.get('lat'),'lon':coord.get('lon'),
            'cats':[c['title'] for c in pg.get('categories',[])]})
    time.sleep(0.1)
json.dump(info,open('data/commons_hotel_images.json','w'),ensure_ascii=False)
print('files with info:',len(info),'| geocoded:',sum(1 for x in info if x['lat']))
print('sample cats:',sorted(seen_cats)[:15])
