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
