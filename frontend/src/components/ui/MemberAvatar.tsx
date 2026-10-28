import { SIZES_AVATAR } from "@/constants/components";
import type { SizeComponents } from "@/types/components";

interface MemberAvatarProps {
  name: string;
  size?: SizeComponents;
  shape?: 'circle' | 'rounded';
}

export function MemberAvatar({ name, size = 'md', shape = 'rounded' }: MemberAvatarProps) {
  const sizes = SIZES_AVATAR[size];
  const radius = shape === 'circle' ? 'rounded-full' : sizes.radius;
  return (
    <div className={`${sizes.outer} ${radius} bg-[linear-gradient(145deg,var(--primary-soft),rgba(249,115,22,0.08))] border border-[rgba(249,115,22,0.2)] grid place-items-center ${sizes.text} font-black text-[var(--primary)] shrink-0`}>
      {name.charAt(0).toUpperCase()}
    </div>
  );
}