(ns clojess.util
  (:require [clojure.string :as s]
            [clojure.walk :as walk]))

(defn camelcase-to-dashes
  [string]
  (let [parts (partition-by #(Character/isUpperCase %) string)]
    (loop [parts parts, acc []]
      (if (seq parts)
        (if (Character/isUpperCase (ffirst parts))
          (let [part (s/join (concat (first parts) (second parts)))]
            (recur (nnext parts) (conj acc (s/lower-case part))))
          (recur (next parts) (conj acc (s/lower-case (s/join (first parts))))))
        (s/join "-" acc)))))

(defn keywordize-keys
  [m]
  (let [f (fn [[k v]]
            (if (string? k)
              [(keyword (camelcase-to-dashes k)) v]
              [k v]))]
    (walk/postwalk (fn [x]
                     (if (map? x)
                       (into {} (map f x))
                       x))
                   m)))

(defn columns-of-index
  [index]
  (for [column (.getColumns index)] (.getName column)))

(defn index-has-columns?
  [columns index]
  (= (map name columns) (columns-of-index index)))

(defn index-for-columns
  [table columns]
  (first
    (for [index (.getIndexes table)
          :when (index-has-columns? columns index)]
      index)))

(defn matching-rows-from-cursor
  [cursor pattern]
  (doall 
    (let [pattern (walk/stringify-keys pattern)]
      (for [row (iterator-seq
                  (reify java.util.Iterator
                    (hasNext [this]
                      (let [savepoint (.getSavepoint cursor)
                                          result (.findNextRow cursor pattern)]
                                      (.restoreSavepoint cursor savepoint)
                                      result))
                    (next [this]
                      (.findNextRow cursor pattern)
                      (.getCurrentRow cursor))
                    (remove [this]
                      (.deleteCurrentRow cursor))))]
        (walk/keywordize-keys (into {} row))))))

(defn matching-rows
  [table pattern]
  (let [columns (keys pattern)
        cursor (or (some-> (index-for-columns table columns) .newCursor .toCursor)
                   (-> table .newCursor .toCursor))]
    (matching-rows-from-cursor cursor pattern)))
