# TODO

## MassTest bugs

### StealCardSpell NPE (~1-2% of MassTest runs)

`NullPointerException: Cannot invoke "Zones.name()" because "destination" is null`
at `GameLogic.stealCard()` line 3215.

Call chain: `StealCardSpell.onCast` → `ConditionalSpell` → battlecry resolution.

The `destination` zone is null when `stealCard` is called on a target whose
destination zone cannot be determined (e.g. stealing a card from an entity that
has already been removed or is in an unexpected zone).

Fix: determine why `stealCard` receives a null destination and either guard
against the invalid state or prevent the spell from targeting removed entities.
