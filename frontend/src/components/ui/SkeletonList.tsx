import { Skeleton } from "@/components/ui/Skeleton";

interface SkeletonListProps {
    count?: number;
    itemClass?: string;
}

export function SkeletonList({ count = 3, itemClass }: SkeletonListProps) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton key={i} className={itemClass} />
      ))}
    </>
  );
}