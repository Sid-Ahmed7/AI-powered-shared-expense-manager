import { useEffect, type ReactNode } from 'react';
import { X } from 'lucide-react';
import { cls } from '@/lib/cls';
import { Button } from '@/components/ui/Button';
import { ICON_SIZE } from '@/constants/components';

interface ModalProps {
  title: string;
  onClose: () => void;
  children: ReactNode;
}

export function Modal(props: ModalProps) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && props.onClose();
    document.addEventListener('keydown', onKey);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = '';
    };
  }, [props.onClose]);

  return (
    <div className={cls.modalBackdrop} onMouseDown={(e) => e.target === e.currentTarget && props.onClose()}>
      <div className={cls.modal} role="dialog" aria-modal="true" aria-label={props.title}>
        <div className={cls.modalHead}>
          <h2 className="text-[17px]">{props.title}</h2>
          <Button variant="icon" onClick={props.onClose} aria-label="Fermer">
            <X size={ICON_SIZE} />
          </Button>
        </div>
        <div className={cls.modalBody}>{props.children}</div>
      </div>
    </div>
  );
}
