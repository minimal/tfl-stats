(ns tfl-stats.test.core
  (:use [tfl-stats.core])
  (:use [clojure.test]))


;; Fixtures
(def status-map
  {:tag :LineStatus, :attrs {:ID "0", :StatusDetails ""}, :content [{:tag :BranchDisruptions, :attrs nil, :content nil} {:tag :Line, :attrs {:ID "1", :Name "Bakerloo"}, :content nil} {:tag :Status, :attrs {:ID "GS", :CssClass "GoodService", :Description "Good Service", :IsActive "true"}, :content [{:tag :StatusType, :attrs {:ID "1", :Description "Line"}, :content nil}]}]})

(def status-map-disrupted
  {:tag :LineStatus, :attrs {:ID "9", :StatusDetails "No service between Harrow-on-the-Hill and Rickmansworth / Croxley due to planned engineering work. GOOD SERVICE on the rest of the line."}, :content [{:tag :BranchDisruptions, :attrs nil, :content [{:tag :BranchDisruption, :attrs nil, :content [{:tag :StationTo, :attrs {:ID "191", :Name "Rickmansworth"}, :content nil} {:tag :StationFrom, :attrs {:ID "101", :Name "Harrow-on-the-Hill"}, :content nil}]} {:tag :BranchDisruption, :attrs nil, :content [{:tag :StationTo, :attrs {:ID "56", :Name "Croxley"}, :content nil} {:tag :StationFrom, :attrs {:ID "101", :Name "Harrow-on-the-Hill"}, :content nil}]}]} {:tag :Line, :attrs {:ID "11", :Name "Metropolitan"}, :content nil} {:tag :Status, :attrs {:ID "PC", :CssClass "DisruptedService", :Description "Part Closure", :IsActive "true"}, :content [{:tag :StatusType, :attrs {:ID "1", :Description "Line"}, :content nil}]}]})


;; Tests

(deftest test-process-status
  (is (= (process-status status-map)
         {:0 {:LineStatusId "0", :StatusId "GS", :CssClass "GoodService"}})))


(deftest test-process-status-disrupted
  (is (= (process-status status-map-disrupted)
         {:9 {:LineStatusId "9", :StatusId "PC", :CssClass "DisruptedService"}})))

