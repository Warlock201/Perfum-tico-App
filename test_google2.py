import urllib.request, urllib.parse, re
url = "https://www.google.com/search?q=lavender+flower+ingredient&tbm=isch"
req = urllib.request.Request(url, headers={'User-Agent': 'Lynx/2.8.9'})
try:
    html = urllib.request.urlopen(req).read().decode('utf-8', errors='ignore')
    matches = re.findall(r'<img[^>]+src="([^"]+)"', html)
    for m in matches:
        if m.startswith('http'):
            print(m)
            break
except Exception as e:
    print(e)
