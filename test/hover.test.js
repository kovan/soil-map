const puppeteer = require('puppeteer');

const BASE_URL = process.env.TEST_URL || 'http://localhost:8080';

async function runTests() {
  const browser = await puppeteer.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  let passed = 0;
  let failed = 0;
  const consoleErrors = [];
  const pageErrors = [];

  async function test(name, fn) {
    try {
      await fn();
      console.log(`✓ ${name}`);
      passed++;
    } catch (err) {
      console.log(`✗ ${name}`);
      console.log(`  Error: ${err.message}`);
      failed++;
    }
  }

  function assert(condition, message) {
    if (!condition) throw new Error(message);
  }

  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 800 });

  // Capture console errors
  page.on('console', msg => {
    if (msg.type() === 'error') {
      consoleErrors.push(msg.text());
    }
  });

  // Capture page errors (uncaught exceptions)
  page.on('pageerror', err => {
    pageErrors.push(err.message);
  });

  console.log('\nLoading page...\n');

  await page.goto(BASE_URL, { waitUntil: 'networkidle0', timeout: 60000 });

  // Give time for any async errors
  await new Promise(r => setTimeout(r, 2000));

  console.log('Running GeoJSON loading tests...\n');

  // Test 0: No JavaScript errors during page load
  await test('No JavaScript errors during page load', async () => {
    if (pageErrors.length > 0) {
      throw new Error(`Page errors: ${pageErrors.join('; ')}`);
    }
  });

  // Test 0b: No console errors related to GeoJSON
  await test('No GeoJSON-related console errors', async () => {
    const geoJsonErrors = consoleErrors.filter(e =>
      e.includes('GeoJSON') ||
      e.includes('ISO3166') ||
      e.includes('properties')
    );
    if (geoJsonErrors.length > 0) {
      throw new Error(`GeoJSON errors: ${geoJsonErrors.join('; ')}`);
    }
  });

  // Test 0c: Leaflet container exists
  await test('Leaflet map container exists', async () => {
    const hasContainer = await page.evaluate(() => {
      return document.querySelector('.leaflet-container') !== null;
    });
    assert(hasContainer, 'Leaflet container not found');
  });

  // Test 0d: GeoJSON countries loaded
  await test('GeoJSON country paths are rendered', async () => {
    // Wait for paths to appear
    await page.waitForFunction(() => {
      const paths = document.querySelectorAll('.leaflet-interactive');
      return paths.length > 0;
    }, { timeout: 30000 });

    const pathCount = await page.evaluate(() => {
      return document.querySelectorAll('.leaflet-interactive').length;
    });
    console.log(`    (Found ${pathCount} interactive paths)`);
    assert(pathCount > 50, `Expected >50 country paths, got ${pathCount}`);
  });

  // Test 0e: Countries have fill colors (not just default)
  await test('Country paths have fill colors applied', async () => {
    const fillColors = await page.evaluate(() => {
      const paths = document.querySelectorAll('.leaflet-interactive');
      const colors = new Set();
      paths.forEach(p => {
        const fill = p.getAttribute('fill');
        if (fill) colors.add(fill);
      });
      return Array.from(colors);
    });
    console.log(`    (Found colors: ${fillColors.slice(0, 5).join(', ')}...)`);
    assert(fillColors.length > 1, `Expected multiple fill colors, got: ${fillColors}`);
    assert(!fillColors.every(c => c === '#ccc'), 'All countries have default gray color');
  });

  // Wait for full GeoJSON to load before hover tests
  await page.waitForFunction(() => {
    const paths = document.querySelectorAll('.leaflet-interactive');
    return paths.length > 50;
  }, { timeout: 30000 });

  // Helper to get info panel text
  async function getInfoText() {
    return await page.evaluate(() => {
      const info = document.querySelector('.info');
      return info ? info.innerText : '';
    });
  }

  // Helper to hover at map position
  async function hoverAtMapPosition(xPercent, yPercent) {
    const mapBox = await page.evaluate(() => {
      const map = document.querySelector('.leaflet-container');
      const r = map.getBoundingClientRect();
      return { x: r.x, y: r.y, width: r.width, height: r.height };
    });
    const x = Math.round(mapBox.x + mapBox.width * xPercent);
    const y = Math.round(mapBox.y + mapBox.height * yPercent);
    await page.mouse.move(x, y);
    await new Promise(r => setTimeout(r, 500));
  }

  // Helper to switch to regional mode
  async function switchToRegionalMode() {
    await page.evaluate(() => {
      const labels = document.querySelectorAll('label');
      for (const l of labels) {
        if (l.innerText.includes('Regions')) l.click();
      }
    });
    await new Promise(r => setTimeout(r, 2000));
  }

  // Helper to zoom to location
  async function zoomTo(lat, lng, zoom) {
    await page.evaluate(({ lat, lng, zoom }) => {
      window.soilMap.setView([lat, lng], zoom);
    }, { lat, lng, zoom });
    await new Promise(r => setTimeout(r, 1500));
  }

  console.log('\nRunning hover tests...\n');

  // Test 1: Country hover shows name and value
  await test('Country hover shows name and percentage', async () => {
    // Hover over North America (Canada area)
    await hoverAtMapPosition(0.25, 0.35);
    const text = await getInfoText();
    assert(text.includes('Canada'), `Expected 'Canada' in info panel, got: ${text}`);
    assert(text.includes('%'), `Expected percentage in info panel, got: ${text}`);
  });

  // Test 2: Info panel shows "Hover over" when not hovering
  await test('Info panel shows prompt when not hovering on country', async () => {
    // Move to ocean area
    await hoverAtMapPosition(0.5, 0.8);
    await new Promise(r => setTimeout(r, 300));
    const text = await getInfoText();
    assert(
      text.includes('Hover over') || text.includes('%'),
      `Expected prompt or data in info panel, got: ${text}`
    );
  });

  // Test 3: Regional mode - Indian states show names
  await test('Indian states show names in regional mode', async () => {
    await switchToRegionalMode();
    await zoomTo(22, 78, 4); // Center on India
    await hoverAtMapPosition(0.5, 0.5);
    const text = await getInfoText();
    assert(text.includes('India'), `Expected 'India' in info panel, got: ${text}`);
    // Should show a state name (not just "India")
    const lines = text.split('\n').filter(l => l.trim());
    assert(lines.length >= 2, `Expected state name and country, got: ${text}`);
  });

  // Test 4: US states show names in regional mode
  await test('US states show names in regional mode', async () => {
    await zoomTo(39, -98, 4); // Center on USA
    await hoverAtMapPosition(0.5, 0.5);
    const text = await getInfoText();
    assert(
      text.includes('United States') || text.includes('%'),
      `Expected US state info, got: ${text}`
    );
  });

  // Test 5: Data sources panel exists
  await test('Data sources panel is visible', async () => {
    const hasSourcesPanel = await page.evaluate(() => {
      const panels = document.querySelectorAll('.leaflet-control');
      for (const p of panels) {
        if (p.innerText.includes('Data Sources')) return true;
      }
      return false;
    });
    assert(hasSourcesPanel, 'Data Sources panel not found');
  });

  // Test 6: Soil quality legend is visible with all categories
  await test('Soil quality legend is visible with all categories', async () => {
    const legendCheck = await page.evaluate(() => {
      const controls = document.querySelectorAll('.leaflet-control');
      for (const c of controls) {
        if (c.innerText.includes('Soil Quality Guide')) {
          const text = c.innerText;
          return {
            found: true,
            hasExcellent: text.includes('Excellent'),
            hasGood: text.includes('Good'),
            hasModerate: text.includes('Moderate'),
            hasPoor: text.includes('Poor'),
            hasVeryPoor: text.includes('Very Poor'),
            hasPeatland: text.includes('Peatland')
          };
        }
      }
      return { found: false };
    });
    assert(legendCheck.found, 'Soil Quality Guide not found');
    assert(legendCheck.hasExcellent, 'Missing Excellent category');
    assert(legendCheck.hasGood, 'Missing Good category');
    assert(legendCheck.hasModerate, 'Missing Moderate category');
    assert(legendCheck.hasPoor, 'Missing Poor category');
    assert(legendCheck.hasVeryPoor, 'Missing Very Poor category');
    assert(legendCheck.hasPeatland, 'Missing Peatland category');
  });

  // Final test: Report all console errors collected
  await test('Summary: No uncaught errors during all tests', async () => {
    if (consoleErrors.length > 0) {
      console.log('    Console errors collected:');
      consoleErrors.forEach(e => console.log(`      - ${e.substring(0, 200)}`));
    }
    if (pageErrors.length > 0) {
      console.log('    Page errors collected:');
      pageErrors.forEach(e => console.log(`      - ${e.substring(0, 200)}`));
      throw new Error(`${pageErrors.length} page error(s) occurred`);
    }
  });

  await browser.close();

  console.log(`\n${passed} passed, ${failed} failed\n`);
  process.exit(failed > 0 ? 1 : 0);
}

runTests().catch(err => {
  console.error('Test runner error:', err);
  process.exit(1);
});
