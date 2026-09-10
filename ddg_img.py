import urllib.request
import re
import sys
import json

query = sys.argv[1].replace(' ', '+')
url = f"https://duckduckgo.com/i.js?l=us-en&o=json&q={query}"
try:
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    html = urllib.request.urlopen(req).read().decode('utf-8')
    data = json.loads(html)
    print(data['results'][0]['image'])
except Exception as e:
    print(e)
