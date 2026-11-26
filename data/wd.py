import requests, json
q='''SELECT ?h ?hLabel ?coord ?img ?site ?phone ?addr ?typeLabel ?stars WHERE {
 ?h wdt:P31/wdt:P279* wd:Q27686 .
 ?h wdt:P17 wd:Q837 .
 OPTIONAL{?h wdt:P625 ?coord.} OPTIONAL{?h wdt:P18 ?img.}
 OPTIONAL{?h wdt:P856 ?site.} OPTIONAL{?h wdt:P1329 ?phone.}
 OPTIONAL{?h wdt:P6375 ?addr.} OPTIONAL{?h wdt:P31 ?type.}
 OPTIONAL{?h wdt:P2464 ?stars.}
 SERVICE wikibase:label { bd:serviceParam wikibase:language "en,ne". }
}'''
r=requests.get('https://query.wikidata.org/sparql',params={'query':q,'format':'json'},headers={'User-Agent':'nepal-hotel-data/1.0 (research)'},timeout=120)
print(r.status_code)
d=r.json()['results']['bindings']
print(len(d),'rows')
json.dump(d,open('data/wikidata_hotels.json','w'),ensure_ascii=False)
from collections import defaultdict
agg=defaultdict(lambda:{'img':set(),'site':set(),'phone':set(),'coord':None,'addr':set(),'stars':set(),'label':'','types':set()})
for b in d:
    qid=b['h']['value'].split('/')[-1]
    a=agg[qid]; a['label']=b.get('hLabel',{}).get('value','')
    for f in ('img','site','phone','addr','stars'):
        if f in b: a[f].add(b[f]['value'])
    if 'coord' in b and not a['coord']:
        v=b['coord']['value'].replace('Point(','').replace(')','').split()
        a['coord']=(float(v[1]),float(v[0]))
    if 'typeLabel' in b: a['types'].add(b['typeLabel']['value'])
uniq=len(agg)
withimg=sum(1 for a in agg.values() if a['img'])
print('unique hotels:',uniq,'| with photo:',withimg,'| with website:',sum(1 for a in agg.values() if a['site']),'| with coord:',sum(1 for a in agg.values() if a['coord']))
print([ (a['label'],list(a['types'])[:2]) for a in list(agg.values())[:10]])
