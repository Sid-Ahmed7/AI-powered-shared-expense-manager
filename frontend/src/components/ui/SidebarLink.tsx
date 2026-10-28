import { ICON_SIZE, SIDEBAR_LINK } from '@/constants/components';
import type { ElementType } from 'react';

interface SidebarLinkProps {
  icon?: ElementType;
  children: React.ReactNode;
  onClick?: () => void;
  as?: ElementType;
  to?: string;
  [key: string]: unknown;
}



export function SidebarLink({ icon: Icon, children, onClick, as: Tag = 'button', ...rest }: SidebarLinkProps) {
  return (
    <Tag className={SIDEBAR_LINK} onClick={onClick} {...rest}>
      {Icon && <Icon size={ICON_SIZE} />}
      {children}
    </Tag>
  );
}