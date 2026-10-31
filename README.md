# firetv-data

Data files for Sachin's Fire TV "Claude Home" app.

- `football-today.json` — today's football matches (Premier League, Bundesliga, La Liga, Champions League; top clubs) with the Indian streaming app that shows each one. Written every morning by a scheduled Claude task.
- `claudehome/` — source of the Claude Home Android TV app (added later).

Schema of `football-today.json`:

```json
{
  "generated": "2026-11-01T06:50:00+01:00",
  "date": "2026-11-01",
  "timezone": "Europe/Berlin",
  "matches": [
    {
      "kickoff": "2026-11-01T18:30:00+01:00",
      "home": "Borussia Dortmund",
      "away": "Bayern Munich",
      "competition": "Bundesliga",
      "app": "Zee5",
      "package": "com.zee5.amazon",
      "big": true,
      "source": "https://..."
    }
  ]
}
```

`package` values on the stick: JioHotstar `in.startv.hotstar`, Sony LIV `com.onemainstream.sonyliv.android`, FanCode `com.fancode.tv`, Zee5 `com.zee5.amazon`. Unknown or unavailable app: `"app": "Not on your apps"`, `"package": ""`.
