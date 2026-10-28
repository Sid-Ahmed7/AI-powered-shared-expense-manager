import { useTranslation } from 'react-i18next';
import { euros } from '@/utils/money';
import type { MemberRow } from '@/types/components';
import { TD, TH } from '@/constants/components';

interface MemberStatsTableProps {
  rows: MemberRow[];
}


export function MemberStatsTable({ rows }: MemberStatsTableProps) {
  const { t } = useTranslation();

  return (
    <table className="w-full border-collapse text-[14px]">
      <thead>
        <tr>
          <th className={TH}>{t('recap.memberCol')}</th>
          <th className={TH}>{t('recap.paidCol')}</th>
          <th className={TH}>{t('recap.consumedCol')}</th>
        </tr>
      </thead>
      <tbody>
        {rows.map((member) => (
          <tr key={member.memberId} className="hover:*:bg-[rgba(255,255,255,0.02)]">
            <td className={TD}>{member.name}</td>
            <td className={TD}>{euros(member.paidCents)}</td>
            <td className={TD}>{euros(member.consumedCents)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}