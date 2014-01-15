# Clojess

An MS Access libary for Clojure, wrapping [Jackcess](http://jackcess.sourceforge.net/).

## Artifacts

Clojess is deployed to [Clojars](https://clojars.org/clojess)

lein dependency: ["clojess" "0.3"]

## Usage

The main namespace for database operations is `clojess.core`.

``` clj
=> (use 'clojess.core)
```

Open a database from a file:
``` clj
=> (open-db "path/to/file.db")
```

List all tables in the database:
``` clj
=> (table-names db)
#{"Table1" "Table2" "Table3"}
```

Get a table from the database:
``` clj
=> (table db "Table1")
```

Get all rows from a table:
``` clj
=> (rows tbl)
```

Get rows matching a column-value map from a table. Note that the value must be the exact type in the database, which is `java.lang.Integer` for integer columns. You must use `(int n)` for the value in that case.
``` clj
=> (rows tbl {:id (int 1)})
```

Get a single row from a table by its primary key. For single-column primary key the value is given; for multi-column primary key, a column-value map.
``` clj
=> (row tbl (int 1))
=> (row tbl {:col1 (int 1)
             :col2 (int 2)})
```

## License

Released under the LGPL.
