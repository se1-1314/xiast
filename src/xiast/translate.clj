(ns xiast.translate
  "Internationalisation functions using Tower, including custom Enlive template
  functions.

  To use the Enlive-based functionality put either
    <xiast msg=\"tower/dictionary/path\" />
    (Tag will be replaced entirely by message.)
  or
    <... msg=\"tower/dictionary/path\"> </...>
    (Contents of tag will be replaced by message.)
  tags in your template files.")

(def config
  {:dev-mode? true
   :fallback-locale :en
   ;; TODO Write function to load dictionaries
   :dictionary {:en (load-file "resources/dictionaries/en.clj")
                :nl-BE (load-file "resources/dictionaries/nl.clj")}})

(defn replace-node [& format-args]
  "Replaces node by translation"
  (fn [node]
    (apply t tower/*locale* translate/config (keyword (-> node :attrs :msg))
           format-args)))
(defn content-node [&])

(defn translate-snip [nodes ])
