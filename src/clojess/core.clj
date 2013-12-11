(ns clojess.core
  (:require [clojess.util :as util]
            [clojure.walk :as walk])
  (:import com.healthmarketscience.jackcess.DatabaseBuilder
           java.io.File))

(defn open-db
  "Opens a database file. Returns a Database object."
  [path]
  (DatabaseBuilder/open (File. path)))

(defn table-names
  "Returns a set of all tablenames in the database"
  [database]
  (.getTableNames database))

(defn table
  "Returns the table with given name from the db, or nil if not found"
  [database name]
  (.getTable database name))

(defn tables
  "Returns a set of all tables in the database"
  [database]
  (set 
    (map (partial table database) 
         (table-names database))))

(defn rows
  "Returns a sequence of rows in the table that optionally match a pattern {column value}"
  ([table] 
    (doall 
      (for [row (iterator-seq (.iterator table))]
        (walk/keywordize-keys (into {} row)))))
  ([table pattern]
    (util/matching-rows table pattern)))

(defn row
  "Gets a single row from table by primary key, which is either a single value or a pattern {column value}"
  [table pk]
  (let [index (.getPrimaryKeyIndex table)
        pattern (if (map? pk)
                  pk
                  (zipmap (util/columns-of-index index) [pk]))
        cursor (-> index .newCursor .toCursor)
        values (util/matching-rows-from-cursor cursor pattern)]
    (assert (<= (count values) 1) "Value was not unique")
    (first values)))
