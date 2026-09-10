import json

with open('app/src/main/assets/base_perfumes.json', 'r', encoding='utf-8') as f:
    perfumes = json.load(f)

vibrato = {
    "nome": "Vibrato",
    "marca": "Sospiro Perfumes",
    "familia": "Cítrico Aromático",
    "notas": "",
    "longevidade": 9,
    "projeção": 9,
    "priceMin": 1500,
    "priceMax": 2500
}

perfumes.append(vibrato)

with open('app/src/main/assets/base_perfumes.json', 'w', encoding='utf-8') as f:
    json.dump(perfumes, f, ensure_ascii=False, indent=2)

print("Vibrato added successfully!")
