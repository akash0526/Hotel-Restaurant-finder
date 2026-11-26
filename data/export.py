import json, csv
from collections import Counter, defaultdict
recs=json.load(open('data/final.json'))
COLS=[('name','Hotel Name'),('name_en','Name (English)'),('category','Category'),('tourism_region','Tourism Region'),
 ('district','District'),('province','Province'),('city','City / Town'),('street','Street'),('housenumber','House No'),
 ('address_full','Full Address'),('postcode','Postcode'),('latitude','Latitude'),('longitude','Longitude'),
 ('maps_url','Google Maps Link'),('phone','Phone'),('mobile','Mobile'),('email','Email'),('website','Website'),
 ('facebook','Facebook'),('instagram','Instagram'),('stars','Star Rating (OSM)'),('rooms','Rooms'),('beds','Beds'),
 ('opening_hours','Opening Hours'),('description','Description'),('photo_url','Photo URL'),('photo_credit','Photo Credit'),
 ('photo_license','Photo Licence'),('photo_source','Photo Source'),('wikidata','Wikidata ID'),('wikipedia','Wikipedia'),
 ('internet_access','Internet'),('wheelchair','Wheelchair Access'),('contact_channels','Contact Channels'),
 ('data_completeness_pct','Completeness %'),('is_known_brand','Known Brand'),('is_trekking_lodge','Trekking Area'),
 ('osm_id','OSM ID'),('osm_type','OSM Type'),('osm_url','OSM Link'),('check_date','Last Verified (OSM)')]
for r in recs:
    r['photo_credit']=r.get('photo_author','')
with open('output/hotels_nepal_master.csv','w',newline='',encoding='utf-8-sig') as f:
    w=csv.writer(f); w.writerow([c[1] for c in COLS])
    for r in recs: w.writerow([r.get(k,'') for k,_ in COLS])
print('CSV rows:',len(recs))

# contacts-only working list for outreach
c=[r for r in recs if (r.get('phone') or r.get('mobile') or r.get('email'))]
with open('output/hotels_nepal_with_contacts.csv','w',newline='',encoding='utf-8-sig') as f:
    w=csv.writer(f); w.writerow(['Hotel Name','Category','District','Province','City','Phone','Mobile','Email','Website','Facebook','Instagram','Latitude','Longitude','Completeness %'])
    for r in c: w.writerow([r['name'],r['category'],r['district'],r['province'],r['city'],r['phone'],r['mobile'],r['email'],r['website'],r['facebook'],r['instagram'],r['latitude'],r['longitude'],r['data_completeness_pct']])
print('contacts CSV rows:',len(c))

json.dump(recs,open('output/hotels_nepal_master.json','w'),ensure_ascii=False,indent=1)
