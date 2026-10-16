import express, { Request, Response, NextFunction } from 'express';
import helmet from 'helmet';
import cors from 'cors';
import rateLimit from 'express-rate-limit';
import jwt from 'jsonwebtoken';
import bcrypt from 'bcrypt';
import { z } from 'zod';
import { WireGuardManager } from './wireguard';

const app = express();
const PORT = process.env.PORT || 4000;
const JWT_SECRET = process.env.JWT_SECRET || 'novavpn-super-secret-development-key-256';
const ADMIN_API_KEY = process.env.ADMIN_API_KEY || 'novavpn-admin-secure-token';

const wireGuard = new WireGuardManager(process.env.WG_INTERFACE || 'wg0');

// Middleware
app.use(helmet());
app.use(cors({ origin: '*' }));
app.use(express.json({ limit: '1mb' }));

// Zero-Log Operational Logger (Strictly logs HTTP method & status code only, NEVER payload or URL queries)
app.use((req: Request, res: Response, next: NextFunction) => {
  const start = Date.now();
  res.on('finish', () => {
    const duration = Date.now() - start;
    // Audit sanitized: only log sanitized route pattern, status, and duration
    console.log(`[NovaVPN API] ${req.method} ${req.baseUrl || req.path} -> ${res.statusCode} (${duration}ms)`);
  });
  next();
});

// Rate limiting
const generalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 200,
  message: { error: 'Too many requests from this IP, please try again later.' }
});
app.use('/api/', generalLimiter);

const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20,
  message: { error: 'Too many authentication attempts. Please retry after 15 minutes.' }
});

// In-Memory fallback store if PostgreSQL is running standalone
const mockUsers = new Map<string, { id: string; email: string; passwordHash: string; plan: string }>();
const mockServers = [
  {
    id: 'ch-zurich-01',
    name: 'Zurich • Secure Core #1',
    country: 'Switzerland',
    countryCode: 'CH',
    flagEmoji: '🇨🇭',
    city: 'Zurich',
    endpointIp: '194.126.177.10',
    endpointPort: 51820,
    serverPublicKey: 'rM8F4eF768jZ+X2d73pL0eWd9P3cQ2/H4bA8fT6mU1Y=',
    isPremium: false,
    pingMs: 18,
    loadPercentage: 34,
    status: 'online'
  },
  {
    id: 'is-reykjavik-01',
    name: 'Reykjavik • Zero-Log Haven',
    country: 'Iceland',
    countryCode: 'IS',
    flagEmoji: '🇮🇸',
    city: 'Reykjavik',
    endpointIp: '185.112.82.14',
    endpointPort: 51820,
    serverPublicKey: 'tY9G3kK559vL+A1m84qN1fXe8Q4dR3/J5cB9gU7nV2Z=',
    isPremium: true,
    pingMs: 42,
    loadPercentage: 21,
    status: 'online'
  },
  {
    id: 'se-stockholm-01',
    name: 'Stockholm • 10Gbps P2P',
    country: 'Sweden',
    countryCode: 'SE',
    flagEmoji: '🇸🇪',
    city: 'Stockholm',
    endpointIp: '193.180.119.5',
    endpointPort: 51820,
    serverPublicKey: 'aB3D7kL992xM+B4v63rK2fZw9S5eT4/K6dC0hV8pW3A=',
    isPremium: true,
    pingMs: 28,
    loadPercentage: 45,
    status: 'online'
  },
  {
    id: 'nl-amsterdam-01',
    name: 'Amsterdam • Fast Gateway',
    country: 'Netherlands',
    countryCode: 'NL',
    flagEmoji: '🇳🇱',
    city: 'Amsterdam',
    endpointIp: '185.107.56.23',
    endpointPort: 51820,
    serverPublicKey: 'kL4E8jR221wP+C5m94tL3aYx0T6fU5/L7eD1iW9qX4B=',
    isPremium: false,
    pingMs: 14,
    loadPercentage: 62,
    status: 'online'
  }
];

let nextClientSubnet = 2;

// Health Endpoint
app.get('/api/health', async (_req: Request, res: Response) => {
  const wgStatus = await wireGuard.getStatus();
  res.json({
    status: 'operational',
    service: 'NovaVPN Backend Mesh API',
    version: '1.0.0',
    timestamp: new Date().toISOString(),
    uptimeSeconds: Math.floor(process.uptime()),
    wireguard: wgStatus
  });
});

// Authentication Schemas
const RegisterSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8, 'Password must be at least 8 characters')
});

app.post('/api/auth/register', authLimiter, async (req: Request, res: Response) => {
  try {
    const { email, password } = RegisterSchema.parse(req.body);
    if (mockUsers.has(email)) {
      return res.status(409).json({ error: 'User already exists' });
    }

    const salt = await bcrypt.genSalt(12);
    const passwordHash = await bcrypt.hash(password, salt);
    const id = `user-${Date.now()}`;

    mockUsers.set(email, { id, email, passwordHash, plan: 'free' });

    const token = jwt.sign({ userId: id, email, plan: 'free' }, JWT_SECRET, { expiresIn: '30d' });
    res.status(201).json({
      message: 'Account created successfully',
      token,
      user: { id, email, plan: 'free' }
    });
  } catch (error) {
    if (error instanceof z.ZodError) {
      return res.status(400).json({ error: error.errors[0].message });
    }
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.post('/api/auth/login', authLimiter, async (req: Request, res: Response) => {
  try {
    const { email, password } = RegisterSchema.parse(req.body);
    const user = mockUsers.get(email);
    if (!user) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    const valid = await bcrypt.compare(password, user.passwordHash);
    if (!valid) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    const token = jwt.sign({ userId: user.id, email: user.email, plan: user.plan }, JWT_SECRET, { expiresIn: '30d' });
    res.json({
      message: 'Authentication successful',
      token,
      user: { id: user.id, email: user.email, plan: user.plan }
    });
  } catch (error) {
    if (error instanceof z.ZodError) {
      return res.status(400).json({ error: error.errors[0].message });
    }
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Server Discovery Endpoint
app.get('/api/servers', (_req: Request, res: Response) => {
  res.json({
    servers: mockServers,
    total: mockServers.length,
    timestamp: Date.now()
  });
});

// Secure WireGuard Peer Provisioning
const ProvisionSchema = z.object({
  clientPublicKey: z.string().min(40).max(64),
  serverId: z.string()
});

app.post('/api/vpn/provision', async (req: Request, res: Response) => {
  try {
    const { clientPublicKey, serverId } = ProvisionSchema.parse(req.body);
    const targetServer = mockServers.find(s => s.id === serverId);
    if (!targetServer) {
      return res.status(404).json({ error: 'Specified VPN server not found' });
    }

    // Allocate next virtual internal IP: 10.66.66.X
    const assignedIp = `10.66.66.${nextClientSubnet++}`;
    if (nextClientSubnet > 250) nextClientSubnet = 2;

    // Securely invoke WireGuard kernel peer command
    await wireGuard.provisionPeer(clientPublicKey, assignedIp);

    const clientConfig = `[Interface]
PrivateKey = <CLIENT_PRIVATE_KEY>
Address = ${assignedIp}/32
DNS = 10.66.0.1, 1.1.1.1
MTU = 1420

[Peer]
PublicKey = ${targetServer.serverPublicKey}
Endpoint = ${targetServer.endpointIp}:${targetServer.endpointPort}
AllowedIPs = 0.0.0.0/0, ::/0
PersistentKeepalive = 25`;

    res.json({
      message: 'WireGuard peer successfully provisioned',
      assignedIp,
      dnsPrimary: '10.66.0.1',
      dnsSecondary: '1.1.1.1',
      serverEndpoint: `${targetServer.endpointIp}:${targetServer.endpointPort}`,
      serverPublicKey: targetServer.serverPublicKey,
      clientConfigTemplate: clientConfig
    });
  } catch (error) {
    console.error('Peer provisioning error:', error);
    res.status(500).json({ error: error instanceof Error ? error.message : 'Provisioning failure' });
  }
});

// Admin Endpoint: Manage Servers
app.post('/api/admin/servers', (req: Request, res: Response) => {
  const authHeader = req.headers.authorization;
  if (authHeader !== `Bearer ${ADMIN_API_KEY}`) {
    return res.status(403).json({ error: 'Unauthorized: Invalid admin credentials' });
  }

  const { id, name, country, city, endpointIp, serverPublicKey, isPremium } = req.body;
  if (!id || !endpointIp || !serverPublicKey) {
    return res.status(400).json({ error: 'Missing required server parameters' });
  }

  const newServer = {
    id,
    name: name || id,
    country: country || 'Unknown',
    countryCode: 'XX',
    flagEmoji: '🌐',
    city: city || 'Node',
    endpointIp,
    endpointPort: 51820,
    serverPublicKey,
    isPremium: Boolean(isPremium),
    pingMs: 25,
    loadPercentage: 10,
    status: 'online'
  };

  mockServers.push(newServer);
  res.status(201).json({ message: 'Server registered in NovaVPN registry', server: newServer });
});

app.listen(PORT, () => {
  console.log(`NovaVPN API listening on port ${PORT}`);
});
