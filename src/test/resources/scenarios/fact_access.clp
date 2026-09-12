;; Golden scenario: fact-slot-value, fact-relation, fact-index and fact-existp, at the top
;; level with a bound id and inside a rule with a bound fact.
(deftemplate item (slot name) (slot price) (multislot tags))
(bind ?f (assert (item (name "a") (price 5) (tags x y))))
(printout t (fact-slot-value ?f name) " " (fact-slot-value ?f price) " " (fact-slot-value ?f tags) crlf)
(printout t (fact-relation ?f) " " (fact-index ?f) " " (fact-existp ?f) " " (fact-existp 99) crlf)
(printout t (fact-slot-value 2 name) " " (fact-slot-value ?f missing) crlf)
(defrule show
  ?f <- (item (name ?n))
=>
  (printout t "rule " (fact-relation ?f) " " (fact-index ?f) " " (fact-slot-value ?f price) crlf)
  (retract ?f)
  (printout t "exists after retract " (fact-existp ?f) crlf))
