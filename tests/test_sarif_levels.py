"""SARIF severity mapping."""

from __future__ import annotations


def test_cvss_bands(scripts) -> None:
    """CVSS scores map onto the four rank names."""
    module = scripts["fail-on-sarif-severity.py"]
    assert module.cvss_to_level(9.2) == "critical"
    assert module.cvss_to_level(7.1) == "high"
    assert module.cvss_to_level(4.2) == "medium"
    assert module.cvss_to_level(3.0) == "low"
    assert module.as_float("nope") is None


def test_property_and_rule_levels(scripts) -> None:
    """Properties and rule fields beat the raw SARIF level."""
    module = scripts["fail-on-sarif-severity.py"]
    assert module.level_from_properties({"severity": "high"}) == "high"
    assert module.level_from_properties({"priority": "9.5"}) == "critical"
    assert module.level_from_properties({"severity": "not-a-level"}) is None
    assert module.level_from_rule({"problem.severity": "medium"}) == "medium"
    assert module.level_from_rule({"precision": "high"}) is None
    assert module.level_from_rule({"security-severity": "7.2"}) == "high"


def test_sarif_level_tags(scripts) -> None:
    """Error results become high or critical from their tags."""
    module = scripts["fail-on-sarif-severity.py"]
    assert module.level_from_sarif("error", ["security"]) == "high"
    assert module.level_from_sarif("error", []) == "high"
    assert module.level_from_sarif("error", ["critical"]) == "critical"
    assert module.level_from_sarif("weird", []) == "medium"
    props = {
        "properties": {"tags": ["critical"]},
        "defaultConfiguration": {"level": "error"},
    }
    assert module._fallback_level({"level": None}, props) == "critical"
    assert module._fallback_level({}, {}) == "warning"


def test_locations(scripts) -> None:
    """A result with no region prints the uri, and an empty result has none."""
    module = scripts["fail-on-sarif-severity.py"]
    located = {
        "locations": [{"physicalLocation": {"artifactLocation": {"uri": "App.kt"}}}]
    }
    assert module.locations(located) == "App.kt"
    assert module.locations({}) == "(no location)"


def test_rule_without_id(scripts) -> None:
    """A driver rule that has no id is skipped."""
    module = scripts["fail-on-sarif-severity.py"]
    sarif: dict = {"runs": [{"tool": {"driver": {"rules": [{}]}}}]}
    assert module.rule_map(sarif) == {}
