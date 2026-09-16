import json

with open("app/src/main/assets/base_perfumes.json", "r") as f:
    data = json.load(f)

notes = set()
for p in data:
    if "notas" in p:
        parts = p["notas"].split("|")
        for part in parts:
            for note in part.split(","):
                n = note.strip().lower()
                # we need to remove accents if the normalization does it
                notes.add(n)

print("\n".join(sorted(list(notes))))
