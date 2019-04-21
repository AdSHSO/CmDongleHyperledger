#! /usr/bin/python
import requests
import sys
import json
r = requests.get('http://192.168.0.102:3001/api/Printer/1')
data = json.loads(r.text)
#print(data['usage'])
print(r.status_code)
if(r.status_code == 200):
	sys.exit(data['usage'])
else:
	sys.exit(-1)
