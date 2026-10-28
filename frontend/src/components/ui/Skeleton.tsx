import { cls } from "@/lib/cls";

interface SkeletonProps {
  className?: string;
}

export function Skeleton(props: SkeletonProps) {
  return <div className={`${cls.skeleton} ${props.className ?? ''}`} />;
}