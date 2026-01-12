const puppeteer = require('puppeteer');
const http = require('http');
const fs = require('fs');
const path = require('path');

// Simple HTTP server
const server = http.createServer((req, res) => {
    if (req.url === '/favicon.ico') {
        res.writeHead(204);
        res.end();
        return;
    }
    const filePath = path.join(__dirname, req.url === '/' ? 'index.html' : req.url);
    try {
        const content = fs.readFileSync(filePath);
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.end(content);
    } catch (e) {
        res.writeHead(404);
        res.end('Not found');
    }
});

async function test() {
    server.listen(8765);
    console.log('Server running on http://localhost:8765');

    const browser = await puppeteer.launch({
        headless: 'new',
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });

    const page = await browser.newPage();

    // Capture console logs
    page.on('console', msg => {
        console.log('BROWSER:', msg.text());
    });

    await page.goto('http://localhost:8765', { waitUntil: 'networkidle0', timeout: 60000 });

    // Wait for map to load
    await page.waitForSelector('.leaflet-container', { timeout: 30000 });
    await new Promise(r => setTimeout(r, 5000)); // Wait for GeoJSON to load

    // Directly get all SVG path fill colors from DOM
    console.log('\n=== DIRECT SVG FILL CHECK ===');
    const svgFills = await page.evaluate(() => {
        const paths = document.querySelectorAll('.leaflet-interactive');
        const results = [];
        paths.forEach((p, i) => {
            const fill = p.getAttribute('fill');
            const stroke = p.getAttribute('stroke');
            results.push({ index: i, fill, stroke });
        });
        return results;
    });

    // Count occurrences of each fill color
    const fillCounts = {};
    svgFills.forEach(({ fill }) => {
        fillCounts[fill] = (fillCounts[fill] || 0) + 1;
    });
    console.log('Fill color distribution:', fillCounts);

    // Check specific colors
    const usaColorCount = svgFills.filter(f => f.fill === '#795548').length;
    const spainColorCount = svgFills.filter(f => f.fill === '#A1887F').length;
    console.log('Paths with USA color (#795548):', usaColorCount);
    console.log('Paths with Spain color (#A1887F):', spainColorCount);

    // First check USA - zoom to North America
    console.log('\n=== CHECKING USA ===');
    await page.evaluate(() => {
        if (window.map) {
            map.setView([38, -98], 4); // Center on USA
        }
    });
    await new Promise(r => setTimeout(r, 3000));

    // Find USA by hovering
    let usaFill = null;
    let usaName = null;
    const usaPaths = await page.$$('.leaflet-interactive');
    console.log('Paths in USA view:', usaPaths.length);
    for (const path of usaPaths) {
        try {
            await path.hover();
            await new Promise(r => setTimeout(r, 30));
            const info = await page.evaluate(() => {
                const nameEl = document.querySelector('.info .region-name');
                return nameEl ? nameEl.textContent : null;
            });
            if (info && (info.toLowerCase().includes('united states') || info === 'USA')) {
                usaFill = await page.evaluate(el => el.getAttribute('fill'), path);
                usaName = info;
                console.log('Found USA:', usaName, 'Fill:', usaFill);
                break;
            }
        } catch (e) {}
    }

    // Now check Spain - zoom to Europe
    console.log('\n=== CHECKING SPAIN ===');
    await page.evaluate(() => {
        if (window.map) {
            map.setView([40, -3], 5); // Center on Spain
        }
    });
    await new Promise(r => setTimeout(r, 3000));

    let spainFill = null;
    let spainName = null;
    const spainPaths = await page.$$('.leaflet-interactive');
    console.log('Paths in Spain view:', spainPaths.length);

    // Log all country names found when hovering in Spain area
    const spainAreaCountries = [];
    for (const path of spainPaths) {
        try {
            await path.hover();
            await new Promise(r => setTimeout(r, 30));
            const info = await page.evaluate(() => {
                const nameEl = document.querySelector('.info .region-name');
                return nameEl ? nameEl.textContent : null;
            });
            if (info && !spainAreaCountries.includes(info)) {
                spainAreaCountries.push(info);
                const fill = await page.evaluate(el => el.getAttribute('fill'), path);
                // Check if this might be Spain (Portugal, France, or nearby)
                if (info.toLowerCase().includes('spain') || info === 'ESP' ||
                    info === 'Spain' || info === 'España' || info === 'Kingdom of Spain') {
                    spainFill = fill;
                    spainName = info;
                    console.log('Found Spain:', spainName, 'Fill:', spainFill);
                }
            }
        } catch (e) {}
    }
    console.log('Countries found near Spain area:', spainAreaCountries);

    console.log('\n=== FINAL COMPARISON ===');
    console.log('USA:', usaName, '- Fill:', usaFill, '(expected: #795548)');
    console.log('Spain:', spainName, '- Fill:', spainFill, '(expected: #A1887F)');

    // Take USA zoomed screenshot
    await page.evaluate(() => {
        if (window.map) {
            map.setView([38, -98], 4);
        }
    });
    await new Promise(r => setTimeout(r, 2000));
    await page.screenshot({ path: 'usa-screenshot.png', fullPage: true });
    console.log('USA screenshot saved');

    // Take Spain zoomed screenshot
    await page.evaluate(() => {
        if (window.map) {
            map.setView([40, -3], 5);
        }
    });
    await new Promise(r => setTimeout(r, 2000));
    await page.screenshot({ path: 'spain-screenshot.png', fullPage: true });
    console.log('Spain screenshot saved');

    // Zoom out for final screenshot
    await page.evaluate(() => {
        if (window.map) {
            map.setView([30, 0], 2);
        }
    });
    await new Promise(r => setTimeout(r, 2000));

    // Get debug panel values
    const debugInfo = await page.evaluate(() => {
        const usaColor = document.getElementById('usa-color')?.style.background;
        const espColor = document.getElementById('esp-color')?.style.background;
        const usaVal = document.getElementById('usa-val')?.textContent;
        const espVal = document.getElementById('esp-val')?.textContent;
        return { usaColor, espColor, usaVal, espVal };
    });

    console.log('\n=== DEBUG PANEL ===');
    console.log('USA:', debugInfo.usaVal, '- Color:', debugInfo.usaColor);
    console.log('Spain:', debugInfo.espVal, '- Color:', debugInfo.espColor);

    // Find USA and Spain by simulating hover and checking info panel
    const findCountryColor = async (countryName) => {
        const paths = await page.$$('.leaflet-interactive');
        for (const path of paths) {
            try {
                await path.hover();
                await new Promise(r => setTimeout(r, 50));
                const info = await page.evaluate(() => {
                    const nameEl = document.querySelector('.info .region-name');
                    return nameEl ? nameEl.textContent : null;
                });
                if (info && info.includes(countryName)) {
                    const fill = await page.evaluate(el => el.getAttribute('fill'), path);
                    return { name: info, fill };
                }
            } catch (e) {
                // Skip non-hoverable elements
            }
        }
        return null;
    };

    // Take a screenshot
    await page.screenshot({ path: 'map-screenshot.png', fullPage: true });
    console.log('Screenshot saved to map-screenshot.png');

    // Log all unique country names found
    console.log('\n=== SCANNING ALL COUNTRIES ===');
    const countriesFound = {};
    const paths = await page.$$('.leaflet-interactive');
    console.log('Total paths:', paths.length);

    for (let i = 0; i < paths.length; i++) {
        try {
            await paths[i].hover();
            await new Promise(r => setTimeout(r, 20));
            const info = await page.evaluate(() => {
                const nameEl = document.querySelector('.info .region-name');
                return nameEl ? nameEl.textContent : null;
            });
            if (info && !countriesFound[info]) {
                const fill = await page.evaluate(el => el.getAttribute('fill'), paths[i]);
                countriesFound[info] = fill;
            }
        } catch (e) {}
    }

    // Show USA and Spain specifically
    const allNames = Object.keys(countriesFound).sort();
    console.log('All countries found:', allNames);

    // Find anything with "United" or "Spain" in the name
    const usaLike = allNames.filter(n => n.toLowerCase().includes('united') || n.toLowerCase().includes('america'));
    const spainLike = allNames.filter(n => n.toLowerCase().includes('spain') || n.toLowerCase().includes('españa'));
    console.log('USA-like names:', usaLike);
    console.log('Spain-like names:', spainLike);

    console.log('Total unique countries:', allNames.length);

    // Verify the colors from scan
    const usaScanFill = countriesFound['United States of America'] || countriesFound['United States'];
    const spainScanFill = countriesFound['Spain'];

    if (usaScanFill && spainScanFill) {
        console.log('\n=== VERIFICATION FROM SCAN ===');
        console.log('USA scan fill:', usaScanFill, '(expected: #795548)');
        console.log('Spain scan fill:', spainScanFill, '(expected: #A1887F)');
        if (usaScanFill === '#795548' && spainScanFill === '#A1887F') {
            console.log('✓ Colors are CORRECT!');
        } else {
            console.log('✗ Colors MISMATCH!');
        }
    }

    await browser.close();
    server.close();
    console.log('\nTest complete.');
}

test().catch(e => {
    console.error(e);
    process.exit(1);
});
