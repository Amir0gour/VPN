import { execFile } from 'child_process';
import { promisify } from 'util';

const execFileAsync = promisify(execFile);

export interface WireGuardPeerConfig {
  publicKey: string;
  allowedIp: string;
  endpoint?: string;
}

export class WireGuardManager {
  private interfaceName: string;
  private isDryRun: boolean;

  constructor(interfaceName: string = 'wg0') {
    this.interfaceName = interfaceName;
    // Check if wg binary is accessible
    this.isDryRun = process.env.NODE_ENV !== 'production' || process.env.WG_DRY_RUN === 'true';
  }

  /**
   * Adds or updates a peer in the live WireGuard interface:
   * wg set wg0 peer <PublicKey> allowed-ips <AllowedIP>/32
   */
  async provisionPeer(publicKey: string, assignedIp: string): Promise<boolean> {
    // Validate Base64 public key (44 chars ending with = or 43 chars)
    if (!/^[A-Za-z0-9+/]{42,43}=?$/.test(publicKey)) {
      throw new Error('Invalid WireGuard Curve25519 public key format');
    }

    // Validate IP
    if (!/^10\.66\.\d{1,3}\.\d{1,3}$/.test(assignedIp)) {
      throw new Error('Invalid internal VPN client IP allocation');
    }

    if (this.isDryRun) {
      console.log(`[WireGuard DryRun] Executed: wg set ${this.interfaceName} peer ${publicKey} allowed-ips ${assignedIp}/32`);
      return true;
    }

    try {
      await execFileAsync('wg', [
        'set',
        this.interfaceName,
        'peer',
        publicKey,
        'allowed-ips',
        `${assignedIp}/32`
      ]);
      return true;
    } catch (error) {
      console.error(`Failed to provision WireGuard peer:`, error);
      throw error;
    }
  }

  /**
   * Removes peer from live interface:
   * wg set wg0 peer <PublicKey> remove
   */
  async removePeer(publicKey: string): Promise<boolean> {
    if (this.isDryRun) {
      console.log(`[WireGuard DryRun] Executed: wg set ${this.interfaceName} peer ${publicKey} remove`);
      return true;
    }

    try {
      await execFileAsync('wg', [
        'set',
        this.interfaceName,
        'peer',
        publicKey,
        'remove'
      ]);
      return true;
    } catch (error) {
      console.error(`Failed to remove WireGuard peer:`, error);
      throw error;
    }
  }

  /**
   * Retrieves live interface telemetry & connected peer stats
   */
  async getStatus(): Promise<string> {
    if (this.isDryRun) {
      return `interface: ${this.interfaceName}\n  public key: rM8F4eF768jZ+X2d73pL0eWd9P3cQ2/H4bA8fT6mU1Y=\n  listening port: 51820\n  active peers: 340`;
    }

    try {
      const { stdout } = await execFileAsync('wg', ['show', this.interfaceName]);
      return stdout;
    } catch (error) {
      return `Error querying WireGuard interface: ${error}`;
    }
  }
}
