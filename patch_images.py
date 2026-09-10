import urllib.request
import json

def get_image(title):
    url = f"https://en.wikipedia.org/w/api.php?action=query&titles={title}&prop=pageimages&format=json&pithumbsize=400"
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        html = urllib.request.urlopen(req).read().decode('utf-8')
        data = json.loads(html)
        pages = data['query']['pages']
        for page_id in pages:
            if 'thumbnail' in pages[page_id]:
                return pages[page_id]['thumbnail']['source']
    except:
        pass
    return None

new_notes = {
    "notas herbais": "Herb",
    "notas verdes": "Leaf",
    "notas atalcadas": "Face_powder",
    "notas aquaticas": "Water",
    "notas marinhas": "Sea",
    "musk": "Musk",
    "almiscar": "Musk",
    "fava tonka": "Tonka_bean",
    "ylang-ylang": "Cananga_odorata"
}

with open("app/src/main/java/com/aistudio/perfumatico/data/local/DefaultNoteImages.kt", "r") as f:
    content = f.read()

insert_pos = content.rfind(")")

new_lines = ""
for key, title in new_notes.items():
    if f'"{key}" to' not in content:
        img = get_image(title)
        if img:
            new_lines += f',\n    "{key}" to "{img}"'

if new_lines:
    content = content[:insert_pos] + new_lines + "\n" + content[insert_pos:]
    with open("app/src/main/java/com/aistudio/perfumatico/data/local/DefaultNoteImages.kt", "w") as f:
        f.write(content)
    print("Images updated successfully!")
else:
    print("No new images needed.")
