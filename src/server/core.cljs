(ns server.core
  (:require ["nodegit" :as nodegit]
            ["fs" :as fs]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
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


(defn get-obj-props [obj props]
  (into {} (map (fn [prop]
                  [prop (js->clj (ocall+ obj (name prop)))])
                props)))

(def patch-props
  [:isAdded :isConflicted :isDeleted :isModified :isRenamed :isUnmodified :isUntracked
   :lineStats :size])

(def line-props [:content :origin :newLineno :oldLineno :numLines])


(def hunk-props [:header :oldStart :oldLines :newStart :newLines])

(defn get-line [line]
  (get-obj-props line line-props))

(defn get-hunk [h]
  (get-obj-props h hunk-props))

(defn get-patch [p]
  (merge (get-obj-props p patch-props)
         {:newFile (.. p newFile path)
          :oldFile (.. p oldFile path)}))

(defn get-lines [hunk]
  (p/let [lines (.lines hunk)]
    {:hunk  (get-hunk hunk)
     :lines (map get-line (array-seq lines))}))

(defn get-hunks [patch]
  (p/let [hunks      (.hunks patch)
          with-lines (p/all (map get-lines (array-seq hunks)))]
     {:patch (get-patch patch)
      :hunks with-lines}))

(defn get-patches [diff]
  (p/let [patches     (.patches diff)
          with-hunks  (p/all (map get-hunks patches))]
    {:diff diff
     :patches with-hunks}))

(defn get-diffs [commit]
  (p/let [diffs         (.getDiff commit)
          with-patches  (p/all (map get-patches diffs))]
    {:self  commit
     :diffs with-patches}))


;; diff
;; - [patch]
;; --- [hunk]
;; ------ [line]


;var opts = { flags: git.Diff.FIND.RENAMES}
;return diff.findSimilar(opts);

(defn get-staged-diff [repo]
  (p/let [head-commit (.getHeadCommit repo)
          head-tree   (.getTree head-commit)
          diff        (.treeToIndex (.-Diff nodegit) repo head-tree nil)
          with-similar (.findSimilar diff (clj->js {:flags (oget nodegit "Diff.FIND.RENAMES")}))
          patches      (get-patches diff)]

    (println :staged-diff patches)
    patches))

(defn get-unstaged-diff [repo]
  "This almost matches the 'git diff' command except it includes untracked"
  (p/let [head-commit (.getHeadCommit repo)
          head-tree (.getTree head-commit)
          diff      (.indexToWorkdir (.-Diff nodegit) repo nil
                                     (clj->js {:flags (bit-or
                                                        (oget nodegit "Diff.OPTION.SHOW_UNTRACKED_CONTENT")
                                                        (oget nodegit "Diff.OPTION.RECURSE_UNTRACKED_DIRS"))}))
          patches   (get-patches diff)]
    (println :unstaged-diff patches)
    patches))


(defn get-commit-diff [repo commit-hash]
  (p/let [commit  (.getCommit repo commit-hash)
          diffs (get-diffs commit)]
    (println :diffs diffs)
    diffs))

(defn open-repo [path]
  (.open (.-Repository nodegit) path))

(comment
  (let [rp-path "../../satang/tdax-user-management-ui"]
    (p/let [rp        (open-repo rp-path)]
       (get-staged-diff rp)))

  (let [rp-path "../../satang/tdax-user-management-ui"
        commit-hash "d57e0af85faba6d8feb6e99e5fd82441661b59e7"]
    (p/let [rp        (.open (.-Repository nodegit) rp-path)]
       (get-commit-diff rp commit-hash)))

  ;;   const diff = await Diff.treeToIndex(repo, await head.getTree(), null);
  (let [rp-path "../../satang/tdax-user-management-ui"
        commit-hash "d57e0af85faba6d8feb6e99e5fd82441661b59e7"]
    (p/let [rp        (.open (.-Repository nodegit) rp-path)
            ;cm        (.getCommit rp commit-hash)
            cm         (.getHeadCommit rp)
            ;diffs     (get-diffs cm)
            head-cm-tree  (.getTree cm)
            ;workDirDiffs (.treeToIndex (.-Diff nodegit) rp head-cm-tree nil)
            workDirDiffs (.treeToWorkdir (.-Diff nodegit) rp head-cm-tree nil)
            wdPatches  (get-patches workDirDiffs)]

      (println :wd (.workdir rp))
      (println :cm (.message cm))
      ;(println :diffs diffs (type diffs) :cnt (count diffs))

      (println :workdir-diff workDirDiffs)
      (println :wd-diff (type workDirDiffs))
      (println :wd-patches wdPatches)

      ;(def cm-diffs diffs)

      #_(p/let [cm (.getCommit rp)]
           (println :wdir (.workdir rp))
           (println :cm (.message cm))))))


(defn reload! []
  (println "\nCode updated.")
  (println "../../satang/tdax-user-management-ui"))
  ;   (println "resolve" (.then (js/Promise.resolve 10) #(println :resolve %)))
  ;   (println "repo" repo)
  ;   (println :nodegit nodegit)
  ;   (println :repo (js/Object.keys (.-Repository nodegit)))


(defn main! []
  (println "App loaded!")
  (reload!))


(defn hi [name]
  (prn :hi name))

(comment
  (job "../../satang/tdax-user-management-ui")
  (fs/writeFile "index.html" (dom/render-to-string [:p "hello"]) #(println :done %))
  (println :hello))

