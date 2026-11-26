import json
from collections import Counter, defaultdict
import openpyxl
from openpyxl.styles import Font, PatternFill, Alignment
from openpyxl.utils import get_column_letter
recs=json.load(open('data/final.json'))
wb=openpyxl.Workbook()
HDR=PatternFill('solid',fgColor='1F4E5F'); HF=Font(color='FFFFFF',bold=True,size=10)

def sheet(ws, headers, rows, widths):
    ws.append(headers)
    for c in ws[1]:
        c.fill=HDR; c.font=HF; c.alignment=Alignment(vertical='center',wrap_text=True)
    ws.row_dimensions[1].height=30
    for r in rows: ws.append(r)
    for i,w in enumerate(widths,1): ws.column_dimensions[get_column_letter(i)].width=w
    ws.freeze_panes='A2'; ws.auto_filter.ref=ws.dimensions

ws=wb.active; ws.title='Summary'
tot=len(recs)
stats=[('Total accommodation records',tot,''),
 ('Hotels with a name',sum(1 for r in recs if r['name']),''),
 ('With phone or mobile',sum(1 for r in recs if r['phone'] or r['mobile']),''),
 ('With email',sum(1 for r in recs if r['email']),''),
 ('With website',sum(1 for r in recs if r['website']),''),
 ('With any contact channel',sum(1 for r in recs if r['contact_channels']),''),
 ('With GPS coordinates',sum(1 for r in recs if r['latitude']),'100% – exact lat/lon for every record'),
 ('With a photo',sum(1 for r in recs if r['photo_url']),''),
 ('Star-rated (OSM tag)',sum(1 for r in recs if r['stars']),''),
 ('Average completeness',f"{round(sum(r['data_completeness_pct'] for r in recs)/tot,1)}%",''),
 ('Districts covered',len(set(r['district'] for r in recs if r['district'])),'of 77'),
 ('Provinces covered',len(set(r['province'] for r in recs if r['province'])),'of 7')]
ws.append(['Nepal Hotels & Accommodation — Master Database']); ws['A1'].font=Font(bold=True,size=14,color='1F4E5F')
ws.append(['Compiled 26 Nov 2026 · Source: OpenStreetMap (Geofabrik Nepal extract) + Wikidata + Wikimedia Commons + hotel websites'])
ws['A2'].font=Font(italic=True,size=9)
ws.append([])
ws.append(['Metric','Value','Note'])
for c in ws[4]: c.fill=HDR; c.font=HF
for s in stats: ws.append(list(s))
ws.column_dimensions['A'].width=46; ws.column_dimensions['B'].width=14; ws.column_dimensions['C'].width=46

ws2=wb.create_sheet('By District')
cnt=Counter((r['district'],r['province'],r['tourism_region']) for r in recs if r['district'])
rows=[]
for (d,p,reg),n in cnt.most_common():
    sub=[r for r in recs if r['district']==d]
    rows.append([d,p,reg,n,sum(1 for r in sub if r['phone'] or r['mobile']),sum(1 for r in sub if r['email']),
                 sum(1 for r in sub if r['website']),sum(1 for r in sub if r['photo_url']),
                 round(sum(r['data_completeness_pct'] for r in sub)/len(sub),1)])
sheet(ws2,['District','Province','Tourism Region','Accommodations','With Phone','With Email','With Website','With Photo','Avg Completeness %'],rows,[20,18,26,16,12,12,13,11,18])

ws3=wb.create_sheet('By Region')
cr=Counter(r['tourism_region'] or 'Unassigned' for r in recs)
rows=[]
for reg,n in cr.most_common():
    sub=[r for r in recs if (r['tourism_region'] or 'Unassigned')==reg]
    rows.append([reg,n,sum(1 for r in sub if r['contact_channels']),sum(1 for r in sub if r['photo_url']),
                 len(set(r['district'] for r in sub))])
sheet(ws3,['Tourism Region','Accommodations','With Contact','With Photo','Districts'],rows,[30,16,14,12,11])

ws4=wb.create_sheet('Hotels')
H=[('name','Hotel Name'),('category','Category'),('district','District'),('province','Province'),('city','City'),
   ('phone','Phone'),('mobile','Mobile'),('email','Email'),('website','Website'),('facebook','Facebook'),
   ('latitude','Latitude'),('longitude','Longitude'),('maps_url','Google Maps'),('photo_url','Photo'),
   ('stars','Stars'),('rooms','Rooms'),('tourism_region','Region'),('contact_channels','Channels'),
   ('data_completeness_pct','Complete %'),('osm_url','OSM')]
rows=[[r.get(k,'') for k,_ in H] for r in recs]
sheet(ws4,[t for _,t in H],rows,[34,14,16,14,16,18,14,26,30,24,11,11,42,50,7,7,24,16,11,40])

ws5=wb.create_sheet('Contactable')
c=[r for r in recs if r['contact_channels']]
H2=[('name','Hotel Name'),('category','Category'),('district','District'),('phone','Phone'),('mobile','Mobile'),
    ('email','Email'),('website','Website'),('latitude','Latitude'),('longitude','Longitude'),('photo_url','Photo')]
sheet(ws5,[t for _,t in H2],[[r.get(k,'') for k,_ in H2] for r in c],[34,14,16,18,14,26,30,11,11,50])

wb.save('output/Nepal_Hotels_Master_Database.xlsx')
print('xlsx saved')
