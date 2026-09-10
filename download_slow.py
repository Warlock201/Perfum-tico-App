import os
import urllib.request
import urllib.parse
import unicodedata
import time

notes = [
    "BERGAMOTA", "LIMÃO SICILIANO", "MANDARINA", "TORANJA", "LARANJA DOCE", "MAÇÃ VERDE", "ABACAXI", "PÊSSEGO", "MELANCIA", "ROSA", "JASMIM", "LAVANDA", "NÉROLI", "ÍRIS", "GERÂNIO", "FLOR DE LARANJEIRA", "TUBEROSA", "SÂNDALO", "CEDRO", "VETIVER", "PATCHOULI", "OUD", "MADEIRA GAIAC", "PIMENTA ROSA", "PIMENTA PRETA", "CARDAMOMO", "CANELA", "NOZ-MOSCADA", "CRAVO-DA-ÍNDIA", "AÇAFRÃO", "BAUNILHA", "FAVA TONKA", "CARAMELO", "CACAU", "CAFÉ", "ÂMBAR", "INCENSO", "MUSK", "COURO", "NOTAS AQUÁTICAS", "LIMA", "YUZU", "TANGERINA", "CEREJA", "AMEIXA", "COCO", "FIGO", "PÊRA", "AMORA", "FRAMBOESA", "CASSIS (GROSELHA PRETA)", "MARACUJÁ", "MANGA", "YLANG-YLANG", "MAGNÓLIA", "PEÔNIA", "LÍRIO-DO-VALE", "ORQUÍDEA", "FRÉSIA", "VIOLETA", "MENTA", "MANJERICÃO", "SÁLVIA", "ALECRIM", "CHÁ VERDE", "CHÁ PRETO", "ABSINTO", "GÁLBANO", "FOLHAS DE VIOLETA", "EUCALIPTO", "CIPRESTE", "JUNÍPERO (ZIMBRO)", "PINHEIRO", "MUSGO DE CARVALHO", "MIRRA", "OLÍBANO", "BENJOIM", "LÁDANO", "AMBROXAN", "ALDEÍDOS", "PRALINÊ", "MEL", "AMÊNDOA", "RUM", "CONHAQUE", "TABACO", "CHOCOLATE", "CHOCOLATE BRANCO", "GENGIBRE", "PETITGRAIN", "LICHIA", "MELÃO", "PISTACHE", "ALGODÃO DOCE", "MARSHMALLOW", "HELIOTRÓPIO", "OSMANTHUS", "CAMOMILA", "LÓTUS", "CAMURÇA", "BÉTULA", "PAU-ROSA", "CASHMERAN", "ISO E SUPER", "ANIS ESTRELADO", "COENTRO", "ELEMI", "CHAMPANHE", "NOTAS OZÔNICAS"
]

def normalize_note_name(name):
    normalized = unicodedata.normalize('NFD', name.strip())
    no_accents = "".join(c for c in normalized if not unicodedata.combining(c))
    return no_accents.lower().replace(" ", "_")

def get_english_name(normalized):
    no_underscore = normalized.replace("_", " ")
    mapping = {
        "maca": "apple fruit",
        "lavanda": "lavender flower",
        "melancia": "watermelon slice",
        "cedro": "cedar wood",
        "ambar": "amber resin",
        "sandalo": "sandalwood",
        "bergamota": "bergamot orange",
        "couro": "leather texture",
        "baunilha": "vanilla bean pod",
        "limao": "lemon fruit",
        "pimenta": "black pepper spice",
        "jasmim": "jasmine flower",
        "rosa": "red rose flower",
        "vetiver": "vetiver grass roots",
        "canela": "cinnamon sticks",
        "patchouli": "patchouli leaves",
        "cardamomo": "cardamom pods",
        "abacaxi": "pineapple fruit",
        "ameixa": "plum fruit",
        "cereja": "cherry fruit",
        "coco": "coconut",
        "framboesa": "raspberry fruit",
        "morango": "strawberry fruit",
        "pessego": "peach fruit",
        "iris": "iris flower",
        "lirio": "lily flower",
        "geranio": "geranium flower",
        "violeta": "violet flower",
        "tabaco": "tobacco leaves",
        "cafe": "roasted coffee beans",
        "incenso": "burning incense smoke",
        "noz moscada": "nutmeg spice",
        "laranja": "orange fruit slice",
        "gengibre": "ginger root",
        "salvia": "sage herb",
        "alecrim": "rosemary herb",
        "ylang": "ylang ylang flower",
        "cravo": "clove spice",
        "pistache": "pistachio nut",
        "mel": "honey dripping",
        "yuzu": "yuzu fruit",
    }
    for k, v in mapping.items():
        if k in no_underscore:
            return v
            
    if "hortela" in no_underscore or "menta" in no_underscore: return "mint leaves"
    if "fava tonka" in no_underscore or "tonka" in no_underscore: return "tonka bean"
    if "neroli" in no_underscore or "flor de laranjeira" in no_underscore: return "neroli orange blossom"
    if "oud" in no_underscore or "agarwood" in no_underscore: return "agarwood oud"
    if "almiscar" in no_underscore or "musk" in no_underscore: return "white musk powder"
    if "cacau" in no_underscore or "chocolate" in no_underscore: return "cocoa beans"
    if "toranja" in no_underscore or "grapefruit" in no_underscore: return "grapefruit slice"
    if "mandarina" in no_underscore or "tangerina" in no_underscore: return "tangerine fruit"
    if "noz" in no_underscore or "amendoa" in no_underscore: return "almond nut"
    if "cha" in no_underscore or "tea" in no_underscore: return "green tea leaves"
    if "notas aquaticas" in no_underscore or "aquatic" in no_underscore: return "water splash splash"
    if "lima" in no_underscore or "lime" in no_underscore: return "lime wedge"
    if "madeira gaiac" in no_underscore or "guaiac" in no_underscore: return "guaiac wood"
    if "acafrao" in no_underscore or "saffron" in no_underscore: return "saffron threads"
    
    return no_underscore

dest_dir = "app/src/main/res/drawable-nodpi"
os.makedirs(dest_dir, exist_ok=True)

import math
def string_hashcode(s):
    h = 0
    for c in s:
        h = (31 * h + ord(c)) & 0xFFFFFFFF
    return ((h + 0x80000000) & 0xFFFFFFFF) - 0x80000000

for note in notes:
    norm = normalize_note_name(note)
    eng_name = get_english_name(norm)
    prompt = f"A single {eng_name}, centered, isolated on a solid dark gray background, macro photography, soft studio lighting, highly detailed"
    encoded_prompt = urllib.parse.quote(prompt)
    seed = abs(string_hashcode(eng_name))
    url = f"https://image.pollinations.ai/prompt/{encoded_prompt}?width=200&height=200&nologo=true&seed={seed}&v=2"
    
    safe_name = norm.replace("-", "_").replace("(", "").replace(")", "").replace(" ", "")
    dest_file = os.path.join(dest_dir, f"note_{safe_name}.jpg")
    
    if not os.path.exists(dest_file):
        print(f"Downloading: {note} -> {dest_file}")
        retries = 3
        while retries > 0:
            try:
                req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 Perfumatico/1.0'})
                with urllib.request.urlopen(req, timeout=30) as response, open(dest_file, 'wb') as out_file:
                    out_file.write(response.read())
                time.sleep(1) # wait 1s to prevent rate limiting
                break
            except Exception as e:
                print(f"Failed to download {note}: {e}")
                time.sleep(3)
                retries -= 1
                if os.path.exists(dest_file):
                    os.remove(dest_file)
