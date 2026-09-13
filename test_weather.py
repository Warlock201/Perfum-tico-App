import urllib.request
import json
import traceback

def get_weather(city):
    try:
        # Note: Open-Meteo is a great free API that doesn't require keys
        url = f"https://geocoding-api.open-meteo.com/v1/search?name={urllib.parse.quote(city)}&count=1&language=pt"
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        response = urllib.request.urlopen(req)
        data = json.loads(response.read().decode('utf-8'))
        
        if 'results' not in data:
            return "Cidade não encontrada"
            
        lat = data['results'][0]['latitude']
        lon = data['results'][0]['longitude']
        
        weather_url = f"https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,relative_humidity_2m,is_day,precipitation&timezone=America%2FFortaleza"
        req2 = urllib.request.Request(weather_url, headers={'User-Agent': 'Mozilla/5.0'})
        response2 = urllib.request.urlopen(req2)
        weather_data = json.loads(response2.read().decode('utf-8'))
        
        return json.dumps(weather_data['current'], indent=2)
    except Exception as e:
        return f"Error: {str(e)}\n{traceback.format_exc()}"

print("Testando clima em Fortaleza:")
print(get_weather("Fortaleza"))
