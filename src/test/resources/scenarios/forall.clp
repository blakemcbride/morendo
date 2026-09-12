;; Golden scenario: the forall conditional element, alone and after another pattern; the rule
;; activates when every student has passed, and stops being true when a new student arrives or
;; a passing record is retracted.
(deftemplate student (slot name))
(deftemplate passed (slot name) (slot course))
(deftemplate course (slot title))
(defrule all-passed
  (forall (student (name ?n)) (passed (name ?n) (course math)))
=>
  (printout t "everyone passed math" crlf))
(defrule each
  (course (title ?t))
  (forall (student (name ?n)) (passed (name ?n) (course ?t)))
=>
  (printout t "everyone passed " ?t crlf))
(assert (course (title math)))
(printout t "no students yet:" crlf)
(agenda)
(assert (student (name ann)))
(assert (student (name bob)))
(assert (passed (name ann) (course math)))
(printout t "bob has not passed:" crlf)
(agenda)
(assert (passed (name bob) (course math)))
(printout t "both passed:" crlf)
(agenda)
(fire)
(assert (student (name cy)))
(printout t "cy has not passed:" crlf)
(agenda)
(retract 7)
(printout t "cy left:" crlf)
(agenda)
(fire)
(retract 5)
(printout t "ann's pass retracted:" crlf)
(agenda)
