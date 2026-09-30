"""Doc-link destinations, roots, and the linter CLI."""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[1]


def test_fences_and_titles(scripts, tmp_path: Path) -> None:
    """Fenced links are ignored and titled destinations keep the path."""
    scripts["lint-doc-links.py"]
    from doc_links.fences import strip_fences
    from doc_links.parse import iter_destinations, path_from_destination
    from doc_links.title import strip_title

    docs = tmp_path / "docs"
    docs.mkdir()
    (docs / "README.md").write_text("# Hi\n", encoding="utf-8")
    page = tmp_path / "page.md"
    page.write_text(
        "See [ok](docs/README.md) and [web](https://example.com).\n"
        "```\n[skip](missing.md)\n```\n"
        "[broken](missing.md)\n",
        encoding="utf-8",
    )
    stripped = strip_fences(page.read_text(encoding="utf-8"))
    assert "[skip](missing.md)" not in stripped
    dests = iter_destinations(stripped)
    assert any(item.startswith("docs/") for item in dests)
    assert path_from_destination("https://example.com") is None
    assert path_from_destination("docs/README.md#part") == "docs/README.md"
    assert strip_title("<docs/README.md>") == "docs/README.md"
    assert strip_title("") == ""


def test_lint_pass_and_fail(scripts, tmp_path: Path) -> None:
    """A broken link fails and a present link passes."""
    module = scripts["lint-doc-links.py"]
    docs = tmp_path / "docs"
    docs.mkdir()
    (docs / "README.md").write_text("# Hi\n", encoding="utf-8")
    page = tmp_path / "page.md"
    page.write_text("[broken](missing.md)\n", encoding="utf-8")
    assert module.main(["--root", str(tmp_path)]) == 1
    page.write_text("See [ok](docs/README.md).\n", encoding="utf-8")
    assert module.main(["--root", str(tmp_path)]) == 0
    assert module.main(["--root", str(tmp_path / "empty-missing")]) == 1


def test_unclosed_destinations() -> None:
    """An unclosed angle or parenthesis destination is skipped."""
    from doc_links.dest import read_inline_destination
    from doc_links.parse import iter_destinations

    assert read_inline_destination("   ", 0) == (None, 3)
    assert read_inline_destination("<docs/README.md", 0)[0] is None
    assert read_inline_destination("(docs/README.md", 0)[0] is None
    nested = read_inline_destination("foo(bar)baz)", 0)
    assert nested[0] == "foo(bar)baz"
    assert iter_destinations("See [blank](   ) later") == []


def test_directory_and_file_relative(tmp_path: Path) -> None:
    """A directory target and a file-relative miss are both failures."""
    from doc_links.check import check_file

    empty = tmp_path / "emptydir"
    empty.mkdir()
    page = tmp_path / "page.md"
    page.write_text("[dir](emptydir)\n[rel](./missing.md)\n", encoding="utf-8")
    failures = check_file(page, tmp_path)
    assert any("directory target" in item for item in failures)
    assert any("missing.md" in item for item in failures)


def test_walk_skips_submodule(tmp_path: Path) -> None:
    """A gitdir pointer is not the repository root."""
    from doc_links import root as root_mod

    bare = tmp_path / "bare"
    bare.mkdir()
    assert root_mod.resolve_root(bare) == bare.resolve()
    git_file = tmp_path / ".git"
    git_file.write_text("gitdir: ../somewhere\n", encoding="utf-8")
    assert root_mod.is_submodule_git(git_file)
    assert root_mod.walk_repo_root(tmp_path) is None


def test_resolve_root_env_and_git(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """Env, a real git toplevel, and a nested submodule all resolve."""
    from doc_links import root as root_mod

    (tmp_path / "README.md").write_text("# root\n", encoding="utf-8")
    git_file = tmp_path / ".git"
    git_file.write_text("not a pointer\n", encoding="utf-8")
    assert root_mod.walk_repo_root(tmp_path) == tmp_path.resolve()
    monkeypatch.delenv("CONSUMING_REPO_ROOT", raising=False)
    monkeypatch.chdir(tmp_path)
    assert root_mod.resolve_root(None) == tmp_path.resolve()
    sub = tmp_path / "sub"
    sub.mkdir()
    (sub / "README.md").write_text("# sub\n", encoding="utf-8")
    (sub / ".git").write_text("gitdir: ../somewhere\n", encoding="utf-8")
    assert root_mod.walk_repo_root(sub) == tmp_path.resolve()
    monkeypatch.setenv("CONSUMING_REPO_ROOT", str(tmp_path))
    assert root_mod.resolve_root(None) == tmp_path.resolve()


def test_git_toplevel_of_repo() -> None:
    """The checkout that contains this file is a git toplevel."""
    from doc_links import root as root_mod

    assert root_mod.git_toplevel(ROOT) == ROOT.resolve()


def test_root_errors(tmp_path: Path, monkeypatch: pytest.MonkeyPatch) -> None:
    """OSError from git and from reading .git fails closed."""
    from doc_links import root as root_mod

    def explode(*_args, **_kwargs):
        """Raise the OSError a missing git binary would raise."""
        raise OSError("no git")

    monkeypatch.setattr(root_mod.subprocess, "run", explode)
    assert root_mod.git_toplevel(tmp_path) is None
    directory = tmp_path / "gitdir"
    directory.mkdir()
    assert root_mod.is_submodule_git(directory) is False
    git_file = tmp_path / ".git"
    git_file.write_text("gitdir: x\n", encoding="utf-8")

    def _explode(*_args, **_kwargs):
        """Raise the OSError an unreadable gitdir file would raise."""
        raise OSError("io")

    monkeypatch.setattr(Path, "read_text", _explode)
    assert root_mod.is_submodule_git(git_file) is False


def test_scan_skips(tmp_path: Path) -> None:
    """Paths outside the root and nested git checkouts are skipped."""
    from doc_links.scan import is_skipped

    outside = tmp_path.parent / "outside.md"
    assert is_skipped(outside, tmp_path) is True
    nested = tmp_path / "vendor" / "pkg"
    nested.mkdir(parents=True)
    assert is_skipped(nested / "README.md", tmp_path) is True
    git_dir = tmp_path / "nested" / ".git"
    git_dir.mkdir(parents=True)
    inner = tmp_path / "nested" / "docs" / "page.md"
    inner.parent.mkdir()
    inner.write_text("# x\n", encoding="utf-8")
    assert is_skipped(inner, tmp_path) is True


def test_lint_cli(tmp_path: Path) -> None:
    """The script entry point reports a broken link."""
    page = tmp_path / "page.md"
    page.write_text("[broken](missing.md)\n", encoding="utf-8")
    completed = subprocess.run(
        [sys.executable, "scripts/lint-doc-links.py", "--root", str(tmp_path)],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    assert completed.returncode == 1
