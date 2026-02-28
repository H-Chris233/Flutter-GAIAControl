#!/usr/bin/env python3
from __future__ import annotations

import argparse
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class CoverageRecord:
  lines_found: int
  lines_hit: int

  @property
  def ratio(self) -> float:
    if self.lines_found <= 0:
      return 1.0
    return self.lines_hit / self.lines_found


def _parse_lcov(lcov_path: Path) -> dict[str, CoverageRecord]:
  records: dict[str, CoverageRecord] = {}

  current_file: str | None = None
  da: dict[int, int] = {}

  for raw in lcov_path.read_text(encoding="utf-8").splitlines():
    line = raw.strip()
    if not line:
      continue
    if line.startswith("SF:"):
      current_file = line[3:]
      da = {}
      continue
    if line.startswith("DA:") and current_file is not None:
      rest = line[3:]
      parts = rest.split(",", 1)
      if len(parts) != 2:
        continue
      try:
        line_no = int(parts[0])
        count = int(parts[1])
      except ValueError:
        continue
      da[line_no] = count
      continue
    if line == "end_of_record" and current_file is not None:
      lines_found = len(da)
      lines_hit = sum(1 for c in da.values() if c > 0)
      records[current_file] = CoverageRecord(lines_found, lines_hit)
      current_file = None
      da = {}
      continue

  return records


def _normalize_sf_path(sf: str, repo_root: Path) -> str:
  sf_norm = sf.replace("\\", "/")
  sf_path = Path(sf_norm)
  if sf_path.is_absolute():
    try:
      return sf_path.relative_to(repo_root).as_posix()
    except ValueError:
      pass

  lib_index = sf_norm.rfind("/lib/")
  if lib_index >= 0:
    return sf_norm[lib_index + 1 :]

  return sf_norm


def main() -> int:
  parser = argparse.ArgumentParser(
    description="Fail the build if overall line coverage is below threshold."
  )
  parser.add_argument("--lcov", default="coverage/lcov.info", help="Path to lcov.info")
  parser.add_argument(
    "--root",
    default=".",
    help="Repository root (used to enumerate lib/**/*.dart)",
  )
  parser.add_argument(
    "--exclude",
    action="append",
    default=[],
    help="Exclude a file path (repeatable), e.g. lib/main.dart",
  )
  parser.add_argument(
    "--min-percent",
    type=float,
    default=90.0,
    help="Minimum overall line coverage percentage (default: 90).",
  )
  parser.add_argument(
    "--show-worst",
    type=int,
    default=10,
    help="Show N lowest-covered files on failure (default: 10).",
  )
  parser.add_argument(
    "--fail-on-missing",
    action="store_true",
    help="Fail if any target file is missing from lcov records.",
  )
  args = parser.parse_args()

  repo_root = Path(args.root).resolve()
  lcov_path = (repo_root / args.lcov).resolve()
  if not lcov_path.exists():
    raise SystemExit(f"lcov file not found: {lcov_path}")

  min_percent = float(args.min_percent)
  if min_percent < 0.0 or min_percent > 100.0:
    raise SystemExit("--min-percent must be between 0 and 100")

  exclude = {e.replace("\\", "/") for e in args.exclude}
  target_files = []
  for path in (repo_root / "lib").rglob("*.dart"):
    rel = path.relative_to(repo_root).as_posix()
    if rel in exclude:
      continue
    target_files.append(rel)

  raw_records = _parse_lcov(lcov_path)
  records: dict[str, CoverageRecord] = {}
  for sf, record in raw_records.items():
    normalized = _normalize_sf_path(sf, repo_root)
    records[normalized] = record

  missing = []
  total_found = 0
  total_hit = 0
  per_file: list[tuple[str, CoverageRecord]] = []
  for rel in sorted(target_files):
    record = records.get(rel)
    if record is None:
      missing.append(rel)
      continue
    per_file.append((rel, record))
    total_found += record.lines_found
    total_hit += record.lines_hit

  print(f"Coverage target files: {len(target_files)}")
  print(f"Coverage records found: {len(records)}")

  if missing:
    print("\nMissing coverage records (file not executed during tests):")
    for rel in missing:
      print(f"  - {rel}")

  overall_ratio = 1.0 if total_found <= 0 else (total_hit / total_found)
  overall_percent = overall_ratio * 100.0
  print(f"\nOverall line coverage: {total_hit}/{total_found} ({overall_percent:.2f}%)")

  if missing and args.fail_on_missing:
    return 1

  if overall_percent + 1e-9 < min_percent:
    worst = sorted(per_file, key=lambda item: item[1].ratio)
    show_n = max(0, int(args.show_worst))
    if show_n > 0 and worst:
      print(f"\nLowest covered files (top {min(show_n, len(worst))}):")
      for rel, record in worst[:show_n]:
        percent = record.ratio * 100.0
        print(f"  - {rel}: {record.lines_hit}/{record.lines_found} ({percent:.2f}%)")
    return 1

  print(f"\nOK: overall line coverage >= {min_percent:.2f}%.")
  return 0


if __name__ == "__main__":
  raise SystemExit(main())
