
const express = require('express');
const { spawn } = require('child_process');
const cors = require('cors');
const app = express();

// Security: Only allow Modula Mobile dev/production origins
app.use(cors({ origin: ['http://localhost:3000', 'https://modulamc.in'] }));
app.use(express.json());

let gameProcess = null;
let gameLogs = [];

/**
 * Launch Minecraft via shell command.
 * Warning: This allows arbitrary command execution. 
 * In production, this should be restricted to a whitelist of jar paths.
 */
app.post('/launch', (req, res) => {
  const { command } = req.body;
  if (!command) return res.status(400).json({ error: 'Missing command' });
  if (gameProcess) return res.status(409).json({ error: 'Minecraft is already running' });

  gameLogs = [];
  console.log('[BRIDGE] Launching command:', command);
  
  // Use 'sh -c' to support the long command string from the builder
  gameProcess = spawn('sh', ['-c', command]);

  gameProcess.stdout.on('data', data => {
    const text = data.toString().trim();
    if (text) {
      gameLogs.push({ type: 'INFO', text, ts: Date.now() });
      console.log(`[MC/INFO] ${text}`);
    }
  });

  gameProcess.stderr.on('data', data => {
    const text = data.toString().trim();
    if (text) {
      gameLogs.push({ type: 'ERROR', text, ts: Date.now() });
      console.error(`[MC/ERROR] ${text}`);
    }
  });

  gameProcess.on('exit', (code) => {
    gameLogs.push({ type: 'SYSTEM', text: `Minecraft process exited with code ${code}`, ts: Date.now() });
    console.log(`[BRIDGE] Minecraft process exited: ${code}`);
    gameProcess = null;
  });

  res.json({ status: 'launched', pid: gameProcess.pid });
});

app.get('/status', (req, res) => {
  res.json({
    running: gameProcess !== null,
    pid: gameProcess?.pid ?? null,
    logCount: gameLogs.length,
    engine: 'Modula-Bridge-v1.0'
  });
});

app.get('/logs', (req, res) => {
  const since = parseInt(req.query.since ?? '0');
  res.json(gameLogs.filter(l => l.ts > since));
});

app.post('/stop', (req, res) => {
  if (gameProcess) {
    gameProcess.kill('SIGTERM');
    gameProcess = null;
    res.json({ status: 'stopped' });
  } else {
    res.status(404).json({ error: 'No game running' });
  }
});

const PORT = 25565;
app.listen(PORT, () => {
  console.log(`
  ═══════════════════════════════════════════
  MODULA MOBILE BRIDGE SERVER
  Listening on: http://localhost:${PORT}
  
  Usage: Keep this terminal open while using
  the Modula Mobile web app.
  ═══════════════════════════════════════════
  `);
});
