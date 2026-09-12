;; Golden scenario: halt stops the current fire; the remaining activations stay on the agenda.
(deftemplate n (slot v))
(defrule one-at-a-time
  (n (v ?v))
=>
  (printout t "fired " ?v crlf)
  (halt))
(assert (n (v 1)))
(assert (n (v 2)))
(assert (n (v 3)))
(fire)
(printout t "after first fire" crlf)
(fire)
(printout t "after second fire" crlf)
