(def ^:private test-file-pattern #".*_test.clj")

(defproject conway-life "0.1.0-SNAPSHOT"
  :description "Conway's Game of Life"
  :url "http://server.fake/conway-life"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [quil "3.1.0"]]
  :main ^:skip-aot conway-life.ui.core
  :test-paths ["src"]
  :test-selectors {:default [(fn [ns] (.endsWith (str ns) "-test"))
                             (constantly true)]}
  :java-source-paths ["src/processing/core"]
  :repl-options {:init-ns conway-life.core}
  :jar-exclusions ~[test-file-pattern]
  :uberjar-exclusions ~[test-file-pattern])
