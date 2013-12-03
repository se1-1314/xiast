(ns xiast.authentication)

(defn netid-from-token
  [token]
  "nvgeele")

(defn login
  [netid password]
  (if (and (= netid "nvgeele") (= password "swordfish"))
    {:token (str (java.util.UUID/randomUUID))
     :locale "en"}
    nil))