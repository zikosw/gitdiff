(ns server.core
  (:require ["nodegit" :as nodegit]))

(def value-a 1)

(defonce value-b 2)

; (def nodegit (js/require "nodegit"))

(def repo (.open (.-Repository nodegit) "../../satang/tdax-user-management-ui"))

; nodegit.Repository.open('../../satang/tdax-user-management-ui')

(defn get-commit [])


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
      (.done (fn [difflist]
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
                                  (println :pp (js/Object.getOwnPropertyNames patch))
                                  (println :pp (js/Object.hasOwnProperty patch "lineStats"))
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

(comment
  (println :hello))
