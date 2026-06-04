import express from 'express';
import path from 'path';
import { createServer as createViteServer } from 'vite';
import dotenv from 'dotenv';

dotenv.config();

const PORT = 3000;

async function startServer() {
  const app = express();

  // API Routes
  app.get('/api/mc/versions', async (req, res) => {
    try {
      const response = await fetch('https://launchermeta.mojang.com/mc/game/version_manifest.json');
      const data = await response.json();
      res.json(data);
    } catch (error) {
      res.status(500).json({ error: 'Failed to fetch Minecraft versions' });
    }
  });

  app.get('/api/mods/search', async (req, res) => {
    const { query = '', limit = '10', type = 'mod' } = req.query;
    try {
      // Modrinth uses facets for filtering
      const facets = `[["project_type:${type}"]]`;
      const url = `https://api.modrinth.com/v2/search?query=${query}&limit=${limit}&facets=${encodeURIComponent(facets)}`;
      const response = await fetch(url);
      const data = await response.json();
      res.json(data);
    } catch (error) {
      res.status(500).json({ error: 'Failed to fetch mods' });
    }
  });

  app.get('/api/news', (req, res) => {
    res.json([
      {
        id: 1,
        title: "Curated Modpacks Now Available",
        tag: "ENGINE UPDATE",
        description: "Discover and install community-favorite modpacks. Each pack is automatically optimized for mobile silicon.",
        image: "https://images.unsplash.com/photo-1549467354-9493f3d2779a?q=80&w=1000&auto=format&fit=crop"
      },
      {
        id: 2,
        title: "Lag Reduction: Performance Mode v1.0",
        tag: "OPTIMIZATION",
        description: "Enable the new Performance Mode in settings to reduce UI latency and maximize in-game FPS.",
        image: "https://images.unsplash.com/photo-1587329310686-914152f45280?q=80&w=1000&auto=format&fit=crop"
      }
    ]);
  });

  // Vite middleware for development
  if (process.env.NODE_ENV !== 'production') {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), 'dist');
    app.use(express.static(distPath));
    app.get('*', (req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Modula Mobile Server running on http://localhost:${PORT}`);
  });
}

startServer();
