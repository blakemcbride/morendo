;; Golden scenario: return-value constraints, =(expr), against a literal computation and against
;; a variable of an earlier pattern.
(deftemplate a (slot x))
(deftemplate b (slot x))
(defrule next (a (x ?x)) (b (x =(+ ?x 1))) => (printout t "next after " ?x crlf))
(defrule five (a (x =(* 1 5))) => (printout t "five" crlf))
(defrule twice (a (x ?x)) (b (x ?y&:(eq ?y (* ?x 2)))) => (printout t "twice " ?x crlf))
(assert (a (x 1)))
(assert (a (x 5)))
(assert (b (x 2)))
(assert (b (x 3)))
(assert (b (x 10)))
