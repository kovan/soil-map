# World Soil Organic Matter Map

An interactive choropleth map showing soil organic carbon percentages across countries and regions worldwide.

Built with ClojureScript, Reagent, and Leaflet.

## Development

### Prerequisites

- Node.js (v18+)
- Java (for ClojureScript compilation)

### Setup

```bash
npm install
```

### Run Development Server

```bash
npm run dev
```

Open http://localhost:8080 in your browser.

## Build

```bash
npm run build
```

Output goes to `public/` directory.

## Deploy to Cloudflare Pages

### Prerequisites

- [Wrangler CLI](https://developers.cloudflare.com/workers/wrangler/install-and-update/)
- Cloudflare account

### Authenticate

```bash
npx wrangler login
```

### Create Project (first time only)

```bash
npx wrangler pages project create soil-quality-map --production-branch main
```

### Deploy

```bash
npm run build
npx wrangler pages deploy public --project-name soil-quality-map
```

```basg
npm run build && npx wrangler pages deploy public --project-name soil-quality-map --upload-source-maps
```

### Custom Domain

1. Go to https://dash.cloudflare.com
2. Add your domain to Cloudflare (if not already)
3. Add DNS records:
   - `CNAME @ soil-quality-map.pages.dev` (proxied)
   - `CNAME www soil-quality-map.pages.dev` (proxied)
4. In Pages > soil-quality-map > Custom domains, add your domain

## Data Sources

- FAO Global Soil Partnership
- ISRIC SoilGrids
- USDA Web Soil Survey
- Scientific literature

Values represent average soil organic carbon percentage in topsoil (0-30cm depth).
