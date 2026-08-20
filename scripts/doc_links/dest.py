"""Read an inline markdown destination after ``](``."""

from __future__ import annotations

from doc_links.title import strip_title


def _read_angle_dest(text: str, start: int) -> tuple[str | None, int]:
    """Read a ``<destination>`` starting at ``start`` (the ``<``)."""
    end = text.find(">", start + 1)
    if end == -1:
        return None, start + 1
    close = text.find(")", end + 1)
    return text[start + 1 : end].strip(), (close + 1 if close != -1 else end + 1)


def _read_paren_dest(text: str, start: int) -> tuple[str | None, int]:
    """Read an unbracketed destination with nested parentheses."""
    depth = 1
    index = start
    while index < len(text):
        char = text[index]
        if char == "(":
            depth += 1
        elif char == ")":
            depth -= 1
            if depth == 0:
                return strip_title(text[start:index]), index + 1
        index += 1
    return None, start + 1


def read_inline_destination(text: str, start: int) -> tuple[str | None, int]:
    """Read the destination of a ``](`` link starting at ``start`` (after ``](``)."""
    index = start
    while index < len(text) and text[index] in " \t":
        index += 1
    if index >= len(text):
        return None, index
    if text[index] == "<":
        return _read_angle_dest(text, index)
    return _read_paren_dest(text, index)
