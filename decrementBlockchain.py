#! /usr/bin/python
import requests
import sys
r = requests.post('http://192.168.0.102:3001/api/DecrementUsage',
 	json={"$class": "org.example.mynetwork.DecrementUsage",
	     "printer": "resource:org.example.mynetwork.Printer#1"})
print(r.status_code)
if(r.status_code == 200):
	sys.exit(0)
else:
	sys.exit(-1)
