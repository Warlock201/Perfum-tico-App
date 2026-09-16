import re

top_notes = ["Bergamota", "Baunilha", "Âmbar", "Patchouli", "Lavanda", "Sândalo", "Cedro", "Vetiver", "Musk", "Limão", "Fava Tonka", "Jasmim", "Couro", "Toranja", "Cardamomo", "Rosa", "Gerânio", "Maçã", "Sálvia", "Especiarias", "Canela", "Gengibre", "Oud", "Pimenta Preta", "Açafrão", "Pimenta Rosa", "Mandarina", "Notas Amadeiradas", "Íris", "Cítricos", "Madeiras", "Noz-moscada", "Almíscar", "Musgo", "Incenso", "Flor de Laranjeira", "Pimenta", "Musgo de Carvalho", "Abacaxi", "Alecrim", "Ambroxan", "Âmbar Cinzento", "Tabaco", "Notas Aquáticas", "Caramelo", "Hortelã", "Notas Florais", "Laranja", "Benjoim", "Baunilha Bourbon", "Notas Verdes", "Menta", "Amêndoa", "Cassis", "Groselha Preta", "Sálvia Esclareia", "Pralinê", "Café", "Aldeídos", "Madeira Guaiac", "Madeira de Cashmere", "Acorde Gourmand", "Frutas Cítricas", "Madeira de Cedro", "Manga", "Melão", "Cereja", "Ameixa", "Maçã Verde", "Folhas de Violeta", "Lichia", "Mel", "Framboesa", "Coco", "Madeira de Sândalo", "Pêssego", "Ylang-Ylang", "Amora", "Toranja", "Absinto", "Flor de Íris", "Flores Brancas", "Pistache", "Sal Marinho", "Amêndoa Amarga", "Orquídea", "Magnólia", "Madeiras Claras", "Lírio-do-Vale", "Lírio do Vale", "Peônia", "Eucalipto"]

with open("app/src/main/java/com/aistudio/perfumatico/utils/StringExt.kt", "r") as f:
    content = f.read()

import unicodedata
def normalize(s):
    s = s.strip()
    s = re.sub(r'^(sa[ií]da|topo|top|cora[cç][aã]o|coracao|heart|fundo|base)\s*:\s*', '', s, flags=re.IGNORECASE)
    s = unicodedata.normalize('NFD', s).encode('ascii', 'ignore').decode('utf-8')
    s = s.lower()
    s = re.sub(r'[^a-z0-9_]', '_', s)
    s = re.sub(r'_+', '_', s).strip('_')
    return s

for note in top_notes:
    norm = normalize(note)
    if f'"{norm}" ->' not in content:
        print(f"MISSING: {note} ({norm})")
