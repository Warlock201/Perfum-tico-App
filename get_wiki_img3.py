import urllib.request
import json
import sys
import time

def get_image(title):
    url = f"https://en.wikipedia.org/w/api.php?action=query&titles={urllib.parse.quote(title)}&prop=pageimages&format=json&pithumbsize=400"
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 Perfumatico/1.0'})
        html = urllib.request.urlopen(req).read().decode('utf-8')
        data = json.loads(html)
        pages = data['query']['pages']
        for page_id in pages:
            if 'thumbnail' in pages[page_id]:
                return pages[page_id]['thumbnail']['source']
    except Exception as e:
        print(f"Error fetching {title}: {e}")
    return None

notes = {
    "maca": "Apple",
    "lavanda": "Lavandula",
    "melancia": "Watermelon",
    "cedro": "Cedrus",
    "ambar": "Amber",
    "sandalo": "Sandalwood",
    "bergamota": "Bergamot_orange",
    "couro": "Leather",
    "baunilha": "Vanilla",
    "limao": "Lemon",
    "pimenta": "Black_pepper",
    "jasmim": "Jasmine",
    "rosa": "Rose",
    "vetiver": "Chrysopogon_zizanioides",
    "hortela": "Mint",
    "canela": "Cinnamon",
    "patchouli": "Patchouli",
    "fava tonka": "Dipteryx_odorata",
    "cardamomo": "Cardamom",
    "neroli": "Neroli",
    "oud": "Agarwood",
    "abacaxi": "Pineapple",
    "ameixa": "Plum",
    "cereja": "Cherry",
    "coco": "Coconut",
    "framboesa": "Raspberry",
    "morango": "Strawberry",
    "pessego": "Peach",
    "iris": "Iris_(plant)",
    "lirio": "Lilium",
    "geranio": "Pelargonium",
    "violeta": "Viola_(plant)",
    "tabaco": "Tobacco",
    "almiscar": "Musk",
    "cafe": "Coffee_bean",
    "cacau": "Cocoa_bean",
    "incenso": "Incense",
    "noz moscada": "Nutmeg",
    "toranja": "Grapefruit",
    "laranja": "Orange_(fruit)",
    "mandarina": "Mandarin_orange",
    "gengibre": "Ginger",
    "noz": "Walnut",
    "cha": "Tea",
    "salvia": "Salvia_officinalis",
    "alecrim": "Rosemary",
    "ylang": "Cananga_odorata",
    "cravo": "Clove",
    "pistache": "Pistachio",
    "mel": "Honey"
}

with open("default_notes_kt.txt", "w") as f:
    f.write("package com.aistudio.perfumatico.data.local\n\n")
    f.write("val DefaultNoteImages = mapOf(\n")
    for key, title in notes.items():
        img = get_image(title)
        if img:
            f.write(f'    "{key}" to "{img}",\n')
            print(f"Got {key}")
        time.sleep(0.5)
    f.write('    "default" to "https://images.unsplash.com/photo-1615599814502-d8a43f87b8f0?w=200&h=200&fit=crop"\n')
    f.write(")\n")
