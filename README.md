# ToDo

Nederlandstalige Android-app om taken en afspraken te plannen en af te vinken. Werkt volledig offline, zonder account.

## Functies
- Taak of afspraak toevoegen: titel, datum (kalender), tijd (24-uurs kiezer) of "Hele dag", optioneel locatie en notitie
- Agenda met maandkalender: tik een dag aan en zie alles van die dag, gesorteerd op tijd
- "Komend": alle openstaande items vanaf vandaag
- Afvinken en weer openzetten direct in het overzicht (vinkje + doorgestreepte titel)
- Bewerken, verwijderen (met bevestiging en titel), delen via het Android-deelmenu (bijv. WhatsApp)
- Gegevens lokaal opgeslagen in `todo.json` in de app-map; blijft bewaard na afsluiten en herstarten
- Datum en tijd worden als tekst bewaard (bijv. `2026-11-24` en `10:00`), zonder tijdzone-omrekening:
  een afspraak om 10:00 blijft 10:00, ook als je telefoon naar Surinaamse tijd overschakelt

## APK bouwen met GitHub (zonder eigen computer-installatie)
1. Maak een nieuwe repository op github.com en upload de inhoud van deze map (inclusief de verborgen map `.github`).
2. Open het tabblad **Actions**: de workflow "Bouw ToDo.apk" start automatisch (of klik **Run workflow**).
3. Na ± 5 minuten staat **ToDo.apk** onder **Releases** (directe download) en als artifact bij de workflow-run.

## Zelf bouwen
Met Android Studio: map openen en **Build > Build APK(s)**. Of in een terminal met Android SDK: `./gradlew assembleDebug`
(resultaat: `app/build/outputs/apk/debug/app-debug.apk`).

## Techniek
- Java, `minSdk 26` (Android 8.0+), geen externe bibliotheken
- Interface: `app/src/main/assets/www/index.html` (draait in een WebView, volledig offline)
- `MainActivity.java`: opslag in bestand en het Android-deelmenu
