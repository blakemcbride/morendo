;; Golden scenario: function calls as slot values in assert, modify and duplicate,
;; from the top level and from rule actions.
(deftemplate item (slot name) (slot price) (slot total) (multislot tags))
(assert (item (name (str-cat "a" "1")) (price (+ 2 3)) (total (* 2 (+ 2 3))) (tags (create$ x y))))
(assert (item (name "b") (price 7) (total 0)))
(defrule price-it
  ?f <- (item (name ?n) (price ?p) (total 0))
=>
  (modify ?f (total (* ?p 3))))
(defrule copy-it
  ?f <- (item (name "b") (total ?t&:(> ?t 0)))
=>
  (duplicate ?f (name (str-cat "b" "-copy")) (total 0) (tags (create$ copy))))
(fire)
(facts)
(bind ?f2 2)
(modify ?f2 (tags (create$ p q r)))
(duplicate ?f2 (name (str-cat "d" "up")))
(facts)
