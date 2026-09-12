;; Golden scenario: the graph-query sample (samples/graphquery) without watch-query tracing.
(batch ./samples/graphquery/query.clp)
(bind ?data1 (load-graph ./samples/graphquery/graphdata.clp) )
(bind ?query1 (run-graph-query ?data1 conceptDistanceOne "tesla model s") )
(printout t "first query results: " ?query1 crlf)
(bind ?data2 (load-graph ./samples/graphquery/graphdata2.clp) )
(bind ?query2 (run-graph-query ?data2 conceptDistanceTwo "diablo") )
(printout t "second query results: " ?query2 crlf)
