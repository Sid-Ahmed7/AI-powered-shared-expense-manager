import { DEVICE, ICON_SIZE } from "@/constants/components";
import { Globe, Monitor, Smartphone } from "lucide-react";

interface DeviceIconProps {
 userAgent?: string;
}


export function DeviceIcon(props: DeviceIconProps) {
  if (!props.userAgent) return <Globe size={ICON_SIZE} />;
  if (DEVICE.test(props.userAgent)) return <Smartphone size={ICON_SIZE} />;
  return <Monitor size={ICON_SIZE} />;
}