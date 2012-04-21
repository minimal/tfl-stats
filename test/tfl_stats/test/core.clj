(ns tfl-stats.test.core
  (:use [tfl-stats.core])
  (:use [clojure.test])
  (:use midje.sweet)
  (:use aleph.formats)
  (:use [clj-time.core :only [date-time now]]))


;; Fixtures
(def status-map
  {:tag :LineStatus, :attrs {:ID "0", :StatusDetails ""}, :content [{:tag :BranchDisruptions, :attrs nil, :content nil} {:tag :Line, :attrs {:ID "1", :Name "Bakerloo"}, :content nil} {:tag :Status, :attrs {:ID "GS", :CssClass "GoodService", :Description "Good Service", :IsActive "true"}, :content [{:tag :StatusType, :attrs {:ID "1", :Description "Line"}, :content nil}]}]})

(def status-map-disrupted
  {:tag :LineStatus, :attrs {:ID "9", :StatusDetails "No service between Harrow-on-the-Hill and Rickmansworth / Croxley due to planned engineering work. GOOD SERVICE on the rest of the line."}, :content [{:tag :BranchDisruptions, :attrs nil, :content [{:tag :BranchDisruption, :attrs nil, :content [{:tag :StationTo, :attrs {:ID "191", :Name "Rickmansworth"}, :content nil} {:tag :StationFrom, :attrs {:ID "101", :Name "Harrow-on-the-Hill"}, :content nil}]} {:tag :BranchDisruption, :attrs nil, :content [{:tag :StationTo, :attrs {:ID "56", :Name "Croxley"}, :content nil} {:tag :StationFrom, :attrs {:ID "101", :Name "Harrow-on-the-Hill"}, :content nil}]}]} {:tag :Line, :attrs {:ID "11", :Name "Metropolitan"}, :content nil} {:tag :Status, :attrs {:ID "PC", :CssClass "DisruptedService", :Description "Part Closure", :IsActive "true"}, :content [{:tag :StatusType, :attrs {:ID "1", :Description "Line"}, :content nil}]}]})


;; Tests


(let [statuses (get-and-parse-line-status)]
  (fact "facts about the feed"
        (-> statuses first :tag) => :LineStatus
        (-> statuses first :attrs :ID) => truthy
        (-> statuses first :attrs :StatusDetails) => truthy
        (-> statuses first :content) => sequential?))

(fact "status is processed correctly"
      (process-status status-map) =>
      {:0 {:LineStatusId "0", :StatusId "GS", :CssClass "GoodService"}})


(fact "handle disrupted status"
      (process-status status-map-disrupted) =>
      {:9 {:LineStatusId "9", :StatusId "PC", :CssClass "DisruptedService"}})

(let [date (date-time 2012 04 17  12 00)
      processed-statuses {:date date
                          :statuses {:0 {:LineStatusId "0",
                                         :StatusId "GS",
                                         :CssClass "GoodService"}}}
      _ (store-statuses processed-statuses)
      key (str "status:" (mk-date-key date))
      fetched (red [:get key])]
  (against-background
   [(after :facts (red [:del key]))]
   (fact "Data is stored in redis correctly"
         (decode-json fetched) => map?)))