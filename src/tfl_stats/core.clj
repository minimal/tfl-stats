(ns tfl-stats.core)
(use 'lamina.core 'aleph.http 'aleph.formats)
(use 'aleph.redis)
(use '[clj-time.core :only [date-time now year month day hour minute]])

(require '[clojure.xml :as xml]
         '[clojure.zip :as zip])


(def red (redis-client {:host "localhost"}))

(def tflurl "http://cloud.tfl.gov.uk/TrackerNet/LineStatus")

(defn get-line-status []
  (sync-http-request {:method :get, :url tflurl}))

(defn get-and-parse-line-status []
  (let [res (get-line-status)]
    (if (= 200 (:status res))
      (-> res :body decode-xml :content))))


(defn process-status [status]
  (let [LineStatusId (-> status :attrs :ID)
        content (:content status)
        status-map (->  (first (filter #(= (:tag %) :Status)
                                       content)))]
    {(keyword LineStatusId) {:LineStatusId LineStatusId
                             :StatusId (-> status-map :attrs :ID)
                             :CssClass (-> status-map :attrs :CssClass)}}))

(defn process-statuses
  [statuses]
  {:date (now) 
   :statuses (reduce merge
                     (for [status statuses]
                       (process-status status)))})

(defn mk-date-key [date]
  (reduce str
          (interpose
           ":" 
           [(year date)
            (month date)
            (day date)
            (hour date)
            (minute date)])))

(defn store-statuses
  [status-map]
  (let [key (str "status:" (mk-date-key (:date  status-map)))]
    (red [:set key (encode-json->string (:statuses status-map))])))


(defn do-periodic-task [task]
  (future
    (loop []
      (task)
      (Thread/sleep 60000)
      (recur))))

(defn get-and-store-feed []
  (try 
    (let [statuses (get-and-parse-line-status)]
      (-> statuses
          process-statuses
          store-statuses))
    (catch Exception ex 
      (println "exception:" ex))))

(defn start-scraping []
  (do-periodic-task get-and-store-feed))