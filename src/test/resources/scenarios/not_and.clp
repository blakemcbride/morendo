;; Golden scenario: the (not (and ...)) conditional element. The group holds for a tuple of the
;; patterns before it when no combination of facts matches every element of the group; the
;; elements may be patterns, negated patterns, tests and nested groups, and a variable first bound
;; inside a group is local to it. A call among the elements is a test.
(deftemplate order (slot id) (slot customer))
(deftemplate customer (slot name) (slot region))
(deftemplate embargo (slot region))
(deftemplate hold (slot customer) (slot reason))

;; an order ships unless its customer is in a region under embargo
(defrule ship
  (order (id ?o) (customer ?c))
  (not (and (customer (name ?c) (region ?r))
            (embargo (region ?r))))
=>
  (printout t "ship order " ?o crlf))

;; the group is the rule's only condition, with a test inside it
(defrule all-clear
  (not (and (hold (customer ?c) (reason ?why))
            (test (neq ?why "paid"))
            (order (customer ?c))))
=>
  (printout t "no unpaid hold on a customer with orders" crlf))

;; a nested group that uses a variable of the enclosing group
(defrule quiet
  (not (and (hold (customer ?c))
            (not (and (customer (name ?c) (region ?r))
                      (embargo (region ?r))))))
=>
  (printout t "every held customer is under embargo" crlf))

;; ?r inside the group is local, so the ?r of the last pattern is a new variable
(defrule local
  (order (id ?o))
  (not (and (customer (region ?r)) (embargo (region ?r))))
  (customer (name ?n) (region ?r))
=>
  (printout t "order " ?o " with customer " ?n " in " ?r crlf))

;; calls only: the elements are tests
(defrule outside-range
  (order (id ?o))
  (not (and (> ?o 100) (< ?o 200)))
=>
  (printout t "order " ?o " is outside 100-200" crlf))

(assert (customer (name "acme") (region "north")))
(assert (customer (name "bolt") (region "south")))
(assert (order (id 1) (customer "acme")))
(assert (order (id 150) (customer "bolt")))
(printout t "no embargo, no holds:" crlf)
(agenda)
(fire)
(assert (embargo (region "south")))
(printout t "south under embargo:" crlf)
(agenda)
(bind ?hold (assert (hold (customer "acme") (reason "credit"))))
(printout t "acme on hold for credit:" crlf)
(agenda)
(fire)
(defrule late
  (not (and (hold (customer ?c)) (order (customer ?c))))
  (customer (name ?n))
=>
  (printout t "late: " ?n crlf))
(printout t "late rule defined while an order is held:" crlf)
(agenda)
(assert (embargo (region "north")))
(printout t "north under embargo too:" crlf)
(agenda)
(modify ?hold (reason "paid"))
(printout t "acme's hold paid:" crlf)
(agenda)
(retract ?hold)
(printout t "hold removed:" crlf)
(agenda)
(fire)
(ppdefrule quiet)
