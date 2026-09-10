import urllib.request
import re
import sys

query = sys.argv[1]
url = f"https://www.pexels.com/search/{query}/"
try:
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'})
    html = urllib.request.urlopen(req).read().decode('utf-8')
    match = re.search(r'images\.pexels\.com/photos/(\d+)/pexels-photo-\1\.jpeg', html)
    if match:
        print(f"https://images.pexels.com/photos/{match.group(1)}/pexels-photo-{match.group(1)}.jpeg?auto=compress&cs=tinysrgb&w=400&h=400&fit=crop")
    else:
        print("Not found")
except Exception as e:
    print(e)
