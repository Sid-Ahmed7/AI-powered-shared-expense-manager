export function describeDevice(userAgent: string | undefined, unknown: string): string {
  if (!userAgent) return unknown;
  const browser = /Edg\//.test(userAgent) ? 'Edge' :
    /Firefox\//.test(userAgent) ? 'Firefox' :
    /Chrome\//.test(userAgent) ? 'Chrome' :
    /Safari\//.test(userAgent) ? 'Safari' : 'Navigateur';
  const os = /Windows/.test(userAgent) ? 'Windows' :
    /Android/.test(userAgent) ? 'Android' :
    /iPhone|iPad/.test(userAgent) ? 'iOS' :
    /Mac OS X/.test(userAgent) ? 'macOS' :
    /Linux/.test(userAgent) ? 'Linux' : '';
  return os ? `${browser} · ${os}` : browser;
}