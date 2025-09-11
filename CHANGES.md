# 2025-09-11 — Code sweep and housekeeping

Swept repository for backup files and removed `.bak` artifacts left during refactoring.
Ensured DB helper methods (`getOrInsert*`) now declare `throws SQLException` and updated call sites in `src/main/java/LedgerGUI.java` to properly handle exceptions.
Added non-modal status bar, Preferences dialog and DB path resolution improvements.
Added tests for DB path resolution and submit flow integration; full test suite passed locally.

Notes:

If you rely on those `.bak` files, restore them from version control history or backups before this commit.
