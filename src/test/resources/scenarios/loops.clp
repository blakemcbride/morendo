;; Golden scenario: the control functions progn, while, loop-for-count, foreach,
;; progn$, break and return.
(progn (bind ?x 1) (bind ?x (+ ?x 1)) (printout t "progn " ?x crlf))
(bind ?i 0)
(while (< ?i 5) do
  (bind ?i (+ ?i 1))
  (if (= ?i 3) then (break)))
(printout t "while stopped at " ?i crlf)
(loop-for-count 2 (printout t "tick" crlf))
(loop-for-count (?k 1 3) (printout t "k=" ?k crlf))
(loop-for-count (?k 2) (printout t "k2=" ?k crlf))
(foreach ?e (create$ a b c) (printout t ?e-index ":" ?e " "))
(printout t crlf)
(progn$ (?e (create$ x y)) (printout t ?e-index ":" ?e " "))
(printout t crlf)
(bind ?sum 0)
(foreach ?v (create$ 1 2 3 4) (bind ?sum (+ ?sum ?v)))
(printout t "sum " ?sum crlf)
(bind ?n 0)
(while TRUE do
  (bind ?n (+ ?n 1))
  (if (>= ?n 10) then (break)))
(printout t "n " ?n crlf)
(deftemplate n (slot v))
(defrule stop-early
  (n (v ?v))
=>
  (printout t "rule " ?v crlf)
  (if (> ?v 1) then (return))
  (printout t "after " ?v crlf))
(defrule count-down
  (n (v ?v))
=>
  (loop-for-count (?k 1 ?v) (printout t "count " ?k crlf)))
(assert (n (v 1)))
(assert (n (v 2)))
