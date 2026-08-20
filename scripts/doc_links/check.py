"""Check that extracted destinations resolve under the repository root."""

from __future__ import annotations

from pathlib import Path

from doc_links.fences import strip_fences
from doc_links.parse import iter_destinations, path_from_destination


def is_repo_relative(path_only: str) -> bool:
    """Return True when ``path_only`` is not explicitly file-relative."""
    return not path_only.startswith(("./", "../")) and path_only not in {".", ".."}


def candidate_targets(source: Path, path_only: str, root: Path) -> list[Path]:
    """Return paths to try for a relative link (file-relative, then repo-root)."""
    targets = [source.parent / path_only]
    if is_repo_relative(path_only):
        targets.append(root / path_only)
    return targets


def target_ok(path: Path) -> bool:
    """Return True when ``path`` is a file, or a directory that contains README.md."""
    resolved = path.resolve()
    if resolved.is_file():
        return True
    return resolved.is_dir() and (resolved / "README.md").is_file()


def _missing_target(md_file: Path, path_only: str, root: Path) -> Path:
    """Return the path that should have existed for a broken link."""
    if is_repo_relative(path_only):
        return (root / path_only).resolve()
    return (md_file.parent / path_only).resolve()


def _failure_message(rel: Path, raw: str, target: Path) -> str:
    """Format one broken-link line."""
    if target.is_dir():
        return f"{rel} -> {raw} (directory target; link a file)"
    return f"{rel} -> {raw}"


def check_file(md_file: Path, root: Path) -> list[str]:
    """Return failure strings for one documentation file."""
    failures: list[str] = []
    text = strip_fences(md_file.read_text(encoding="utf-8"))
    rel = md_file.relative_to(root)
    for raw in iter_destinations(text):
        path_only = path_from_destination(raw)
        if path_only is None:
            continue
        if any(target_ok(candidate) for candidate in candidate_targets(md_file, path_only, root)):
            continue
        target = _missing_target(md_file, path_only, root)
        failures.append(_failure_message(rel, raw, target))
    return failures
