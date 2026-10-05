import urllib.request, urllib.parse, re, time

queries = [
    ("dior_homme_intense", "site:fragrantica.com Dior Homme Intense 2011"),
    ("dior_fahrenheit", "site:fragrantica.com Fahrenheit Christian Dior for men"),
    ("dior_sauvage_elixir", "site:fragrantica.com Sauvage Elixir Dior"),
    ("azzaro_the_most_wanted", "site:fragrantica.com The Most Wanted Azzaro for men"),
    ("azzaro_wanted_by_night", "site:fragrantica.com Wanted by Night Azzaro for men"),
    ("azzaro_pour_homme", "site:fragrantica.com Azzaro pour Homme Azzaro for men"),
    ("ysl_y_edp", "site:fragrantica.com Y Eau de Parfum Yves Saint Laurent"),
    ("ysl_la_nuit", "site:fragrantica.com La Nuit de l Homme Yves Saint Laurent"),
    ("ysl_myslf", "site:fragrantica.com MYSLF Yves Saint Laurent for men"),
    ("versace_eros_flame", "site:fragrantica.com Eros Flame Versace for men"),
    ("versace_pour_homme", "site:fragrantica.com Versace Pour Homme Versace for men"),
    ("armani_swy_intensely", "site:fragrantica.com Stronger With You Intensely Giorgio Armani"),
    ("armani_adg_profondo", "site:fragrantica.com Acqua di Gio Profondo Giorgio Armani"),
    ("pr_one_million_elixir", "site:fragrantica.com 1 Million Elixir Paco Rabanne"),
    ("pr_invictus_edt", "site:fragrantica.com Invictus Paco Rabanne for men"),
    ("ch_bad_boy_cobalt", "site:fragrantica.com Bad Boy Cobalt Parfum Electrique Carolina Herrera"),
    ("lattafa_yara", "site:fragrantica.com Yara Lattafa Perfumes"),
    ("lattafa_yara_tous", "site:fragrantica.com Yara Tous Lattafa Perfumes"),
    ("lattafa_qaed_al_fursan", "site:fragrantica.com Qaed Al Fursan Lattafa Perfumes"),
    ("lattafa_nebras", "site:fragrantica.com Nebras Lattafa Perfumes"),
    ("lattafa_badee_amethyst", "site:fragrantica.com Bade e Al Oud Amethyst Lattafa"),
    ("lattafa_hayaati", "site:fragrantica.com Hayaati Lattafa Perfumes"),
    ("armaf_cdn_urban_elixir", "site:fragrantica.com Club De Nuit Urban Elixir Armaf"),
    ("armaf_odyssey_mandarin", "site:fragrantica.com Odyssey Mandarin Sky Armaf"),
    ("armaf_odyssey_homme", "site:fragrantica.com Odyssey Homme Armaf for men"),
    ("al_haramain_amber_gold", "site:fragrantica.com Amber Oud Gold Edition Al Haramain"),
    ("al_haramain_detour_noir", "site:fragrantica.com Detour Noir Al Haramain Perfumes"),
    ("al_haramain_laventure", "site:fragrantica.com L Aventure Al Haramain Perfumes"),
    ("alhambra_the_tux", "site:fragrantica.com The Tux Maison Alhambra"),
    ("alhambra_woody_oud", "site:fragrantica.com Woody Oud Maison Alhambra"),
    ("alhambra_porto_neroli", "site:fragrantica.com Porto Neroli Maison Alhambra"),
    ("alhambra_kismet_angel", "site:fragrantica.com Kismet Angel Maison Alhambra")
]

results = {}
for key, q in queries:
    url = "https://html.duckduckgo.com/html/?q=" + urllib.parse.quote(q)
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"})
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            html = resp.read().decode("utf-8", errors="ignore")
            ids = re.findall(r"-(\d{3,6})\.html", html)
            res_id = ids[0] if ids else ""
            results[key] = res_id
            print(f'"{key}": "{res_id}",')
    except Exception as e:
        results[key] = ""
        print(f'"{key}": "", # error {e}')
    time.sleep(0.3)

print("TOTAL FOUND:", len([v for v in results.values() if v]))
