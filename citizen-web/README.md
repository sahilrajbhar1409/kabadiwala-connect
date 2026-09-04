# citizen-web

Household-facing web frontend for Kabadiwala Connect — book a scrap pickup,
see today's rates, and get paid by a verified collector.

Currently a static single-file build (`index.html`, no build step required).
Open directly in a browser, or serve via any static host / the existing
`backend` service.

## Structure

- `index.html` — landing page + booking form (white/light theme)

## Next steps

- Wire the booking form to `backend`'s pickup-request API
- Pull live rates from `ai-service` (price estimation)
- Replace static rate table with an API-driven one
