(ns xiast.config
  (:require [clojure.java.io :as io]
            clojure.edn))

;; http://squirrel.pl/blog/2012/10/01/configuration-files-in-clojure/

(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (clojure.edn/read (java.io.PushbackReader. r))))

(def config (load-config "xiast.conf"))
