(ns server.core
  (:require ["nodegit" :as nodegit]
            ["fs" :as fs]
            [promesa.core :as p]
            [promesa.exec :as pe]
            [reagent.dom.server :as dom]
            [reagent.core :as r :refer [atom]]))

(def value-a 1)

(defonce value-b 2)

; (def nodegit (js/require "nodegit"))

(def repo (.open (.-Repository nodegit) "../../satang/tdax-user-management-ui"))

; nodegit.Repository.open('../../satang/tdax-user-management-ui')

(defn get-commit [])

(defn get-lines [hunk]
  (p/let [lines (.lines hunk)]
    {:self hunk
     :lines lines}))

(defn get-hunks [patch]
  (println :gh patch (type patch))
  (p/let [hunks (.hunks patch)]
          ;with-lines (map get-lines (array-seq hunks))]
     {:self  patch
      :hunks hunks})) ;with-lines}))

(defn get-patches [diff]
  (println :gp diff)
  (p/let [patches (.patches diff)]
    {:self diff
     :patches patches}))

;; diff
;; - [patch]
;; --- [hunk]
;; ------ [line]

(comment
  (let [rp-path "../../satang/tdax-user-management-ui"
        commit-hash "d57e0af85faba6d8feb6e99e5fd82441661b59e7"]
    (p/let [rp        (.open (.-Repository nodegit) rp-path)
            cm        (.getCommit rp commit-hash)
            diff-list (.getDiff cm)
            patches   (map get-patches (array-seq diff-list))]
            ;patches   (p/all (map #(.patches %) (array-seq diff-list)))]
            ;hunks     (p/all (map get-hunks (array-seq (first patches))))]
            ;hunks     (p/all (map #(.hunks %)   (array-seq (first patches))))
            ;lines     (p/all (map #(.lines %)   (array-seq (first hunks))))]

      (println :wd (.workdir rp))
      (println :cm (.message cm))
      (println :diff-list diff-list (type diff-list) :cnt (count (array-seq diff-list)))
      (println :patches patches)
      ;(println :patchs (count (array-seq (first patches))))
      ;(println :hunks (count hunks))
      ;(doseq [p (array-seq (first patches))]
      ;  (println :P (.lineStats p)))
      ;(println :hunks hunks (count hunks))
      ;(println :lines lines (count lines))
      #_(p/let [cm (.getCommit rp)]
           (println :wdir (.workdir rp))
           (println :cm (.message cm))))))


(defn job [path]
  (-> (.open (.-Repository nodegit) path)
      ;(.then #(.getHeadCommit %))
      (.then #(.getCommit % "d57e0af85faba6d8feb6e99e5fd82441661b59e7"))
      (.then (fn [cm]
               (let [sha (.sha cm)
                     author (.. cm author name)
                     message (.message cm)]
                 (println :commit sha :by author :msg message)
                 cm)))
      (.then #(.getDiff %))
      (.then (fn [difflist]
               (.forEach
                 difflist
                 (fn [diff]
                   (println :diff diff :nd (.numDeltas diff))
                   (->
                     (.patches diff)
                     (.then (fn [patches]
                              (.forEach
                                patches
                                (fn [patch]
                                  (println :patch patch :t (type patch)  :ls (.lineStats patch))
                                  (->
                                    (.hunks patch)
                                    (.then (fn [hunks]
                                             (.forEach hunks
                                                       (fn [hunk]
                                                        (do
                                                          (println :h hunk (type hunk) (.lines hunk))
                                                          (->
                                                            (.lines hunk)
                                                            (.then (fn [lines]
                                                                     (println :diff
                                                                              (.. patch oldFile path)
                                                                              (.. patch newFile path))
                                                                     (println :hh (.. hunk header trim) :hhh)
                                                                     (.forEach lines
                                                                               (fn [line]
                                                                                 #(println :l line)
                                                                                 (println :ll
                                                                                          (.oldLineno line)
                                                                                          (.newLineno line)
                                                                                          (char (.origin line))
                                                                                          (.. line content))))))))))))))))))))
               (println :done :difflist)))))


(defn reload! []
  (println "\nCode updated.")
  (job "../../satang/tdax-user-management-ui"))
  ;   (println "resolve" (.then (js/Promise.resolve 10) #(println :resolve %)))
  ;   (println "repo" repo)
  ;   (println :nodegit nodegit)
  ;   (println :repo (js/Object.keys (.-Repository nodegit)))

(defn run []
  (-> (.open (.-Repository nodegit) "../../satang/tdax-user-management-ui")
      (.then (fn [rp]
               (js/console.log "then-rp" (.workdir rp)))))

  (println :workdir js/__dirname)

  (println "commit---"
           (-> (.open (.-Repository nodegit) "../../satang/tdax-user-management-ui")
               (.then (fn [rp]
                        (js/console.log "then-rp" rp
                          (.getCommit rp "74a24cb5dee2842be5cf9d70e2a59dfb0242b93c"))))
               (.then #(println :c (.message %)))))

  (println "Trying values:" value-a value-b))





(defn main! []
  (println "App loaded!")
  (reload!))


(defn hi [name]
  (prn :hi name))

(comment
  (job "../../satang/tdax-user-management-ui")
  (fs/writeFile "index.html" (dom/render-to-string [:p "hello"]) #(println :done %))
  (println :hello))

