import urllib.request
import re
import sys

query = sys.argv[1]
url = f"https://unsplash.com/s/photos/{query}"
try:
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    html = urllib.request.urlopen(req).read().decode('utf-8')
    match = re.search(r'images\.unsplash\.com/photo-([a-zA-Z0-9\-]+)\?', html)
    if match:
        print(f"https://images.unsplash.com/photo-{match.group(1)}?w=400&h=400&fit=crop")
    else:
        print("Not found")
except Exception as e:
    print(e)
