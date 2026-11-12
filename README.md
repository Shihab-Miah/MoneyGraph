# MoneyGraph

An offline-first, privacy-focused personal finance manager designed for seamless transaction tracking, visual cash flow analysis, and strict budgeting without cloud dependency.

## Key Features

- **100% Offline & Private:** Zero account requirements, trackers, or unsolicited network calls. All financial data remains encrypted on-device.
- **Visual Analytics:** Real-time expense breakdowns via category pie charts, monthly comparison bar charts, and cash flow diagrams.
- **Multi-Account Ledgers:** Manage multiple wallets, bank accounts, and cards with automatic balance tracking and inter-account transfers.
- **Budgeting System:** Set monthly spending ceilings per category with dynamic progress bars and threshold warnings.
- **Encrypted Local Backups:** Portable AES-CBC encrypted backups (`.mbak`) and clean PDF/CSV export for reporting and record keeping.

---

## Architecture & Data Specifications

### Database Schema (SQLite)

The core relational data model consists of four decoupled tables:

```sql
-- Accounts (Wallets, Bank Accounts, Cards)
CREATE TABLE table_account (
    acc_id INTEGER PRIMARY KEY AUTOINCREMENT,
    acc_name TEXT NOT NULL,
    acc_amount TEXT NOT NULL,    -- Serialized BigDecimal string to eliminate float errors
    acc_initial TEXT NOT NULL,   -- Opening balance
    acc_icon INTEGER DEFAULT 0   -- Icon identifier
);

-- Categories (Income & Expense classifications)
CREATE TABLE table_category (
    cat_id INTEGER PRIMARY KEY AUTOINCREMENT,
    cat_name TEXT NOT NULL,
    cat_type INTEGER NOT NULL,   -- 1 = Income, 2 = Expense
    cat_icon INTEGER DEFAULT 0
);

-- Records (All financial events)
CREATE TABLE table_record (
    rec_id INTEGER PRIMARY KEY AUTOINCREMENT,
    rec_time INTEGER NOT NULL,   -- Unix epoch timestamp in milliseconds
    rec_type INTEGER NOT NULL,   -- 1 = Income, 2 = Expense, 3 = Transfer
    rec_amount TEXT NOT NULL,    -- Serialized BigDecimal string
    rec_note TEXT,               -- Memo / Description
    rec_acc_id INTEGER,          -- FK -> table_account.acc_id
    rec_cat_id INTEGER,          -- FK -> table_category.cat_id
    rec_from_id INTEGER,         -- Source Account FK (transfers only)
    rec_to_id INTEGER            -- Destination Account FK (transfers only)
);

-- Budgets (Monthly category caps)
CREATE TABLE table_budget (
    bug_id INTEGER PRIMARY KEY AUTOINCREMENT,
    bug_cat_id INTEGER NOT NULL, -- FK -> table_category.cat_id
    bug_limit TEXT NOT NULL,     -- Monthly limit (BigDecimal string)
    bug_time INTEGER NOT NULL    -- Start-of-month epoch timestamp
);
```

### Account Rules
- **Archiving / Hidden Accounts:** Accounts whose names begin with a period (`.`) are flagged as ignored and excluded from global net-worth aggregations.
- **High-Precision Math:** All financial calculations strictly employ arbitrary-precision decimal representations (`BigDecimal`) to prevent IEEE-754 floating-point inaccuracies.

---

## Backup Format Specification

Backups use an AES-CBC + HMAC-SHA256 authenticated packaging format:
* **Payload:** JSON document containing arrays for `accounts`, `categories`, `records`, and `budgets`.
* **Cipher:** `AES/CBC/PKCS5Padding` with random 16-byte IV.
* **Integrity:** `HmacSHA256` computed over `(IV || Ciphertext)`.
* **Container format:** `Base64(IV):Base64(Ciphertext):Base64(HMAC)` saved as `.mbak`.

---

## Roadmap

- [x] Reverse-engineer legacy application data structures and encryption schemes
- [ ] Implement modern Android UI shell (Material 3)
- [ ] Database repository layer with Room / SQLite
- [ ] Custom canvas chart layouts (Pie, Bar, Flow)
- [ ] Export engine (PDF generator & CSV builder)
- [ ] Backup import/export compatibility engine
