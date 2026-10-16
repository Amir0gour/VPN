-- NovaVPN Production Database Schema (PostgreSQL)
-- Implements Zero-Knowledge & Zero-Log Architecture:
-- Browsing URLs, IP destinations, and DNS payloads are NEVER stored.

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    plan_tier VARCHAR(20) DEFAULT 'free' CHECK (plan_tier IN ('free', 'premium')),
    bandwidth_used_bytes BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS servers (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    country_code VARCHAR(4) NOT NULL,
    flag_emoji VARCHAR(8) NOT NULL,
    city VARCHAR(100) NOT NULL,
    endpoint_ip INET NOT NULL,
    endpoint_port INTEGER DEFAULT 51820,
    server_public_key VARCHAR(64) NOT NULL,
    is_premium BOOLEAN DEFAULT FALSE,
    max_capacity INTEGER DEFAULT 1000,
    current_clients INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'online' CHECK (status IN ('online', 'maintenance', 'offline')),
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS vpn_peers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    server_id VARCHAR(50) REFERENCES servers(id) ON DELETE CASCADE,
    client_public_key VARCHAR(64) NOT NULL,
    assigned_internal_ip INET NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_provisioned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE (server_id, assigned_internal_ip)
);

CREATE TABLE IF NOT EXISTS system_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Seed initial servers
INSERT INTO servers (id, name, country, country_code, flag_emoji, city, endpoint_ip, endpoint_port, server_public_key, is_premium, max_capacity, current_clients, latitude, longitude)
VALUES
('ch-zurich-01', 'Zurich • Secure Core #1', 'Switzerland', 'CH', '🇨🇭', 'Zurich', '194.126.177.10', 51820, 'rM8F4eF768jZ+X2d73pL0eWd9P3cQ2/H4bA8fT6mU1Y=', FALSE, 1000, 340, 47.3769, 8.5417),
('is-reykjavik-01', 'Reykjavik • Zero-Log Haven', 'Iceland', 'IS', '🇮🇸', 'Reykjavik', '185.112.82.14', 51820, 'tY9G3kK559vL+A1m84qN1fXe8Q4dR3/J5cB9gU7nV2Z=', TRUE, 1500, 315, 64.1466, -21.9426),
('se-stockholm-01', 'Stockholm • 10Gbps P2P', 'Sweden', 'SE', '🇸🇪', 'Stockholm', '193.180.119.5', 51820, 'aB3D7kL992xM+B4v63rK2fZw9S5eT4/K6dC0hV8pW3A=', TRUE, 2000, 900, 59.3293, 18.0686),
('nl-amsterdam-01', 'Amsterdam • Fast Gateway', 'Netherlands', 'NL', '🇳🇱', 'Amsterdam', '185.107.56.23', 51820, 'kL4E8jR221wP+C5m94tL3aYx0T6fU5/L7eD1iW9qX4B=', FALSE, 1200, 744, 52.3676, 4.9041),
('de-frankfurt-01', 'Frankfurt • DE-CIX Backbone', 'Germany', 'DE', '🇩🇪', 'Frankfurt', '194.187.249.12', 51820, 'mN5F9kS332xQ+D6n05uM4bZy1U7gV6/M8fE2jX0rY5C=', FALSE, 1500, 765, 50.1109, 8.6821),
('us-newyork-01', 'New York • Ultra Stream #1', 'United States', 'US', '🇺🇸', 'New York', '198.51.100.24', 51820, 'pQ6G0lT443yR+E7o16vN5cAz2V8hW7/N9gF3kY1sZ6D=', FALSE, 2500, 1700, 40.7128, -74.0060),
('us-losangeles-01', 'Los Angeles • Pacific Hub', 'United States', 'US', '🇺🇸', 'Los Angeles', '198.51.100.89', 51820, 'sT7H1mU554zS+F8p27wO6dB03W9iX8/O0hG4lZ2tA7E=', TRUE, 2000, 1080, 34.0522, -118.2437),
('jp-tokyo-01', 'Tokyo • Low Ping Asia', 'Japan', 'JP', '🇯🇵', 'Tokyo', '203.0.113.45', 51820, 'uV8I2nV665aT+G9q38xP7eC14X0jY9/P1iH5ma3uB8F=', FALSE, 1800, 684, 35.6762, 139.6503),
('sg-singapore-01', 'Singapore • Southeast Gateway', 'Singapore', 'SG', '🇸🇬', 'Singapore', '203.0.113.92', 51820, 'wX9J3oW776bU+H0r49yQ8fD25Y1kZ0/Q2jI6nb4vC9G=', TRUE, 2200, 924, 1.3521, 103.8198)
ON CONFLICT (id) DO NOTHING;
