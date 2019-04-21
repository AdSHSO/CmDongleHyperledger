#! /usr/bin/python
import requests
import sys
import uuid

debt_id = uuid.uuid4().hex
r = requests.post('http://192.168.0.102:3001/api/DecrementUsage', json = {
  "$class": "org.example.mynetwork.Refill",
  "printer": "resource:org.example.mynetwork.Printer#1",
  "amount": 10,
  "debt_id" : debt_id})
print(r.status_code)
if(r.status_code == 200):
	sys.exit(0)
else:
	sys.exit(-1)
