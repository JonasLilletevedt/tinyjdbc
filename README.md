# tinyjdbc

A minimal JDBC helper for Java.  
Removes repetitive `try/catch`, `PreparedStatement`, and `ResultSet` boilerplate.

Designed for small projects that use plain SQL without heavy ORM frameworks.

---

## Features

- `one()` — fetch a single row as `Optional<T>`
- `list()` — fetch multiple rows as `List<T>`
- `insert()` — run `INSERT` and return the generated key (`RETURN_GENERATED_KEYS`)
- `update()` — run `UPDATE` or any modifying query
- `delete()` — run `DELETE` (wrapper around `update()`)
- Functional interfaces: `Binder` and `Mapper` for clean parameter binding and result mapping

---

## Quikstart
`tinyjdbc` is a single Java file; no setup and no dependencies.

Just copy [`Sql.java`](tinydjdbc/Sql.java) into your project (for example, under `src/main/java/your/package/`).
Then import and use it:
```java
import your.package.Sql;
```

## Example

```java
Sql sql = new Sql();

// INSERT
int id = sql.insert(conn,
    "INSERT INTO accounts(owner_id, iban, created_at) VALUES (?,?,?)",
    ps -> {
        ps.setInt(1, 1);
        ps.setString(2, "NO123");
        ps.setString(3, "2025-10-25");
    }
);

// SELECT ONE
var account = sql.one(conn,
    "SELECT id, owner_id, iban, created_at FROM accounts WHERE id = ?",
    ps -> ps.setInt(1, id),
    rs -> new Account(
        rs.getInt("id"),
        rs.getInt("owner_id"),
        rs.getString("iban"),
        rs.getString("created_at")
    )
);

// DELETE
int deleted = sql.update(conn,
    "DELETE FROM accounts WHERE id = ?",
    ps -> ps.setInt(1, id)
);
```

## Real Example: `AccountRepo`

Here's a real usage example from my **Bankers** project, showing how `tinyjdbc` can be used in a clear repository class.

```java
package no.setup.bankers.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import no.setup.bankers.domain.Account;

public class AccountRepo {
    private final Sql sql;
    public AccountRepo(Sql sql) {
        this.sql = sql;
    }

    public static Account mapAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("id"),
                rs.getInt("owner_id"),
                rs.getString("iban"),
                rs.getString("created_at")
            );
    }

    public int create(
        Connection c,
        int ownerId,
        String iban,
        String createdAt
        ) {
        String q = "INSERT INTO accounts(owner_id, iban, created_at) VALUES (?, ?, ?)";
        return sql.insert(
            c,
            q,
            ps -> {
                ps.setInt(1, ownerId);
                ps.setString(2, iban);
                ps.setString(3, createdAt);
            }
        );
    }

    public Optional<Account> findById(Connection c, int id) {
        String q = "SELECT id, owner_id, iban, created_at FROM accounts WHERE id = ?";

        return sql.one(
            c,
            q,
            ps -> ps.setInt(1, id),
            rs -> mapAccount(rs)
        );
    }

    public Optional<Account> findByIban(Connection c, String iban) {
        String q = "SELECT id, owner_id, iban, created_at FROM accounts WHERE iban = ?";

        return sql.one(
            c,
            q,
            ps -> ps.setString(1, iban),
            rs -> mapAccount(rs)
        );

    }

    public List<Account> findByOwnerId(Connection c, int ownerId) {
        String q = "SELECT id, owner_id, iban, created_at FROM accounts WHERE owner_id = ?";

        return sql.list(
            c,
            q,
            ps -> ps.setInt(1, ownerId),
            rs -> mapAccount(rs)
        );
    }
}
```

This demonstrates how the `Sql` helper keeps the repository classes concise, type-safe and clean, while allowing you to write plain SQL with full control.

## Error Handling

All SQL errors are wrapped in a runtime `DbException` that contains the original `SQLException` as a cause.
This keeps call sites clean while preserving the root cause for logging/diagnostics.

## License
MIT; see `LICENSE`

Created by Jonas Lilletvedt
