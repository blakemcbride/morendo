;; Golden scenario: deffacts, the CLIPS reset, list-deffacts, ppdeffacts and undeffacts.
(deftemplate person (slot name) (slot age) (multislot tags))
(deffacts people "the starting facts"
  (person (name "ann") (age 30) (tags a b))
  (person (name "bob") (age 40)))
(deffacts more
  (person (name "cy") (age (+ 40 10))))
(defrule adult
  (person (name ?n) (age ?a&:(>= ?a 40)))
=>
  (printout t ?n " is an adult" crlf))
(defrule no-lhs => (printout t "no-lhs fired" crlf))
(facts)
(reset)
(facts)
(fire)
(list-deffacts)
(ppdeffacts people)
(undeffacts more)
(assert (person (name "dee") (age 60)))
(reset)
(facts)
(fire)
