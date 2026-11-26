import json, re, math, unicodedata
from collections import Counter, defaultdict
recs=json.load(open('data/stage6.json'))

HUB={
 'Kathmandu':('Kathmandu Valley','Kathmandu'),'Lalitpur':('Kathmandu Valley','Patan/Lalitpur'),
 'Bhaktapur':('Kathmandu Valley','Bhaktapur'),'Kavrepalanchok':('Kathmandu Valley','Dhulikhel'),
 'Nuwakot':('Kathmandu Valley','Nagarkot/Nuwakot'),'Sindhupalchowk':('Kathmandu Valley','Helambu'),
 'Kaski':('Pokhara & Annapurna','Pokhara'),'Syangja':('Pokhara & Annapurna','Syangja'),
 'Tanahun':('Pokhara & Annapurna','Bandipur'),'Parbat':('Pokhara & Annapurna','Kushma'),
 'Myagdi':('Pokhara & Annapurna','Ghorepani/Beni'),'Baglung':('Pokhara & Annapurna','Baglung'),
 'Manang':('Pokhara & Annapurna','Manang/Annapurna Circuit'),
 'Mustang':('Pokhara & Annapurna','Jomsom/Muktinath'),
 'Chitwan':('Chitwan & Terai Jungle','Sauraha/Chitwan'),'Nawalparasi East':('Chitwan & Terai Jungle','Nawalparasi'),
 'Bardiya':('Chitwan & Terai Jungle','Bardiya'),'Banke':('Chitwan & Terai Jungle','Nepalgunj'),
 'Solukhumbu':('Everest Region','Namche/Lukla'),'Dolakha':('Everest Region','Jiri/Kalinchowk'),
 'Okhaldhunga':('Everest Region','Okhaldhunga'),
 'Lumbini':('Lumbini & Western Terai','Lumbini'),'Rupandehi':('Lumbini & Western Terai','Bhairahawa'),
 'Kapilvastu':('Lumbini & Western Terai','Kapilvastu'),'Dang':('Lumbini & Western Terai','Dang'),
 'Palpa':('Lumbini & Western Terai','Tansen'),
 'Rasuwa':('Langtang & Central Hills','Langtang'),'Dhading':('Langtang & Central Hills','Dhading'),
 'Gorkha':('Langtang & Central Hills','Gorkha'),'Makawanpur':('Langtang & Central Hills','Hetauda'),
 'Lumbini ':('Lumbini & Western Terai','Lumbini'),
 'Kailali':('Far West','Dhangadhi'),'Kanchanpur':('Far West','Mahendranagar'),
 'Doti':('Far West','Doti'),'Achham':('Far West','Achham'),'Bajhang':('Far West','Bajhang'),
 'Baitadi':('Far West','Baitadi'),'Dadeldhura':('Far West','Dadeldhura'),'Darchula':('Far West','Darchula'),
 'Bajura':('Far West','Bajura'),
 'Surkhet':('Karnali & Rara','Birendranagar'),'Mugu':('Karnali & Rara','Rara'),'Jumla':('Karnali & Rara','Jumla'),
 'Humla':('Karnali & Rara','Simikot'),'Kalikot':('Karnali & Rara','Kalikot'),'Dailekh':('Karnali & Rara','Dailekh'),
 'Jajarkot':('Karnali & Rara','Jajarkot'),'Salyan':('Karnali & Rara','Salyan'),'Rukum West':('Karnali & Rara','Rukumkot'),
 'Dolpa':('Karnali & Rara','Dolpa'),
 'Jhapa':('Eastern Terai & Hills','Birtamode'),'Morang':('Eastern Terai & Hills','Biratnagar'),
 'Sunsari':('Eastern Terai & Hills','Dharan'),'Ilam':('Eastern Terai & Hills','Ilam'),
 'Dhankuta':('Eastern Terai & Hills','Dhankuta'),'Udayapur':('Eastern Terai & Hills','Udayapur'),
 'Bhojpur':('Eastern Terai & Hills','Bhojpur'),'Khotang':('Eastern Terai & Hills','Khotang'),
 'Terhathum':('Eastern Terai & Hills','Terhathum'),'Panchthar':('Eastern Terai & Hills','Phidim'),
 'Taplejung':('Eastern Terai & Hills','Taplejung'),'Sankhuwasabha':('Eastern Terai & Hills','Khandbari'),
 'Bara':('Eastern Terai & Hills','Simara'),'Parsa':('Eastern Terai & Hills','Birgunj'),
 'Dhanusha':('Eastern Terai & Hills','Janakpur'),'Mahottari':('Eastern Terai & Hills','Jaleshwar'),
 'Sarlahi':('Eastern Terai & Hills','Malangwa'),'Rautahat':('Eastern Terai & Hills','Gaur'),
 'Saptari':('Eastern Terai & Hills','Rajbiraj'),'Siraha':('Eastern Terai & Hills','Siraha'),
 'Sindhuli':('Eastern Terai & Hills','Sindhulimadhi'),'Ramechhap':('Eastern Terai & Hills','Manthali'),
 'Gulmi':('Lumbini & Western Terai','Gulmi'),'Arghakhanchi':('Lumbini & Western Terai','Sandhikharka'),
 'Pyuthan':('Lumbini & Western Terai','Pyuthan'),'Rolpa':('Lumbini & Western Terai','Rolpa'),
 'Eastern Rukum':('Lumbini & Western Terai','Rukumkot'),'Lamjung':('Pokhara & Annapurna','Besisahar'),
}
TREK_DISTRICTS={'Solukhumbu','Manang','Mustang','Myagdi','Rasuwa','Dolpa','Humla','Taplejung','Sankhuwasabha',
 'Gorkha','Dolakha','Baglung','Lamjung','Parbat','Mugu','Jumla'}
CORE_DISTRICTS={'Kathmandu','Lalitpur','Bhaktapur','Kaski','Chitwan','Rupandehi','Morang','Sunsari','Banke','Kailali'}
def norm(s):
    s=unicodedata.normalize('NFKD',s or '').encode('ascii','ignore').decode().lower()
    return re.sub(r'[^a-z0-9 ]',' ',s).split()

# --- dedupe near-identical entries ---
def dkm(a,b,c,d):
    R=6371;p=math.radians;dl=p(c-a);dn=p(d-b)
    x=math.sin(dl/2)**2+math.cos(p(a))*math.cos(p(c))*math.sin(dn/2)**2
    return 2*R*math.asin(math.sqrt(x))
KEYF=['name','lat','lon','city','district','province','category','address_full','street','phone','mobile','email',
 'website','facebook','instagram','stars','rooms','beds','opening_hours','operator','description','photo_url',
 'photo_page','photo_author','photo_license','photo_source','wikidata','wikipedia','internet_access','wheelchair',
 'osm_url','osm_id','osm_type','postcode','brand','check_date']
def filled(r): return sum(1 for k in ('name','phone','mobile','email','website','street','city','photo_url','stars','rooms','description','opening_hours') if r.get(k))
buckets=defaultdict(list)
for r in recs:
    hl=tuple(sorted(t for t in norm(r['name']) if len(t)>2))
    buckets[hl].append(r)
drop=set(); merged=0
for hl,grp in buckets.items():
    if not hl or len(grp)==1: continue
    grp=sorted(grp,key=filled,reverse=True)
    kept=[]
    for r in grp:
        dup=False
        for k in kept:
            if dkm(r['lat'],r['lon'],k['lat'],k['lon'])<0.12:
                dup=True
                for f in ('phone','mobile','email','website','photo_url','stars','rooms','description'):
                    if not k.get(f) and r.get(f): k[f]=r[f]
                k['duplicate_of']=k['duplicate_of'] if 'duplicate_of' in k else []
                merged+=1; break
        if not dup: kept.append(r)
    keepids={id(x) for x in kept}
    for r in grp:
        if id(r) not in keepids: drop.add(id(r))
recs=[r for r in recs if id(r) not in drop]
print('after dedupe:',len(recs),'| merged away:',merged)

# --- enrich fields ---
MAJOR_BRANDS=['soaltee','yak','yet','hyatt','radisson','marriott','annapurna','malla','shangri','everest','alors',
 'aloft','dusit','tiger palace','mechi crown','kingsbury','ichchha','barahi','fishtail','fulbari','dwarka','dwarika',
 'shanker','himalaya','gokarna','temple tree','pavilions','basanta','kasthmandap','taleju','kathmandu guest house',
 'hotel ambassador','tibet international','royal singi','marshyangdi','manang','vaishali','grand hotel','mountain house',
 'yes hotel','club himalaya','nagarkot','fort resort','tiger tops','gaurishankar','atithi','kutumba','bhangeri']
final=[]
for r in recs:
    h=HUB.get(r['district'],('',''))
    r['tourism_region']=h[0]; r['nearest_hub']=h[1]
    r['is_trekking_lodge']=r['district'] in TREK_DISTRICTS
    r['is_urban_market']=r['district'] in CORE_DISTRICTS
    r['latitude']=r['lat']; r['longitude']=r['lon']
    nm=(r['name'] or '').lower()
    r['is_known_brand']=any(b in nm for b in MAJOR_BRANDS)
    # contact channels
    ch=[]
    if r.get('phone') or r.get('mobile'): ch.append('phone')
    if r.get('email'): ch.append('email')
    if r.get('website'): ch.append('web')
    if r.get('facebook'): ch.append('facebook')
    if r.get('instagram'): ch.append('instagram')
    r['contact_channels']='|'.join(ch)
    r['bookable_online']=bool(r.get('website') or (r.get('facebook') and not r.get('phone')))
    r['data_completeness_pct']=round(100*filled(r)/12)
    r['maps_url']=f"https://www.google.com/maps/search/?api=1&query={r['lat']},{r['lon']}"
    r['display_name']=r['name'] or (r['name_en'] if r.get('name_en') else '')
    r['source']='OpenStreetMap contributors (ODbL 1.0)'
    if r.get('photo_source')=='Hotel official website': r['photo_license']='© hotel website – permission required'
    final.append(r)
final.sort(key=lambda r:(r['province'] or 'zz', r['district'] or 'zz', r['name'] or ''))
json.dump(final,open('data/final.json','w'),ensure_ascii=False)
print('records:',len(final))
print('named:',sum(1 for r in final if r['name']))
print('phone:',sum(1 for r in final if r['phone'] or r['mobile']),'email:',sum(1 for r in final if r['email']),
      'web:',sum(1 for r in final if r['website']),'photo:',sum(1 for r in final if r['photo_url']))
print('any contact:',sum(1 for r in final if r['contact_channels']))
print('top districts:',Counter(r['district'] for r in final).most_common(10))
print('regions:',Counter(r['tourism_region'] for r in final).most_common())
print('avg completeness:',round(sum(r['data_completeness_pct'] for r in final)/len(final),1),'%')
