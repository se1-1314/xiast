(ns xiast.authentication)

(defn netid-from-token
  [token]
  "nvgeele")

(defn login
  [netid password]
  (if (and (= netid "nvgeele") (= password "swordfish"))
    {:user "nvgeele"
     :locale "en"}
    nil))