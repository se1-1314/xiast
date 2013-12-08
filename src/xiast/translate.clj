(ns xiast.translate
  "Internationalisation functions using Tower, including custom Enlive template
  functions.

  To use the Enlive-based functionality put either
    <xiast msg=\"tower/dictionary/path\" />
    (Tag will be replaced entirely by message.)
  or
    <... msg=\"tower/dictionary/path\"> </...>
    (Contents of tag will be replaced by message.)
  tags in your template files."
  (:use net.cgrand.enlive-html)
  (:require [taoensso.tower :as tower
             :refer (with-locale with-tscope t *locale*)]))

(def tower-config
  {:dev-mode? true
   :fallback-locale :en
   ;; TODO Write function to load dictionaries
   :dictionary "dictionaries/all.clj"})

;;; FIXME abstract translation function
(defn translate-by-context
  [kw & format-args]
  (apply t tower/*locale* tower-config kw format-args))

(defn translate-node [& format-args]
  "Replaces node by translation"
  (fn [node]
    (apply translate-by-context (keyword (-> node :attrs :msg)) format-args)))

(defn translate-node-content [& format-args]
  "Substitute contents of node by translation"
  (do-> (fn [node]
          ((content (apply translate-by-context (keyword (-> node :attrs :msg)) format-args))
           node))
        (remove-attr :msg)))

(defmacro translate-nodes
  "Forms are like
     [:name/space/msg args ...]"
  [nodes & forms]
  `(at ~nodes
       ~@(mapcat
          (fn [[keyw & args]]
            [[[:xiast `(attr= :msg ~(subs (str keyw) 1))]] `(translate-node ~@args)
             [`(attr= :msg ~(subs (str keyw) 1))]  `(translate-node-content ~@args)])
          forms)
       [[:xiast (attr? :msg)]] (translate-node)
       [(attr? :msg)] (translate-node-content)))

;; (binding [tower/*locale* :en]
;;   (translate-nodes (html-snippet "<xiast msg=\"index/welcome\"></body") [:oue "aou"]))
;; (tr)