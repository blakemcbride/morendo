;; Golden scenario: type tests, integer and float conversion, div, = and <> in predicates,
;; and the CLIPS form of defglobal.
(printout t (numberp 1) (numberp 1.5) (numberp "1") " "
           (integerp 1) (integerp 1.0) (integerp "x") " "
           (floatp 1.5) (floatp 1) " "
           (stringp "s") (stringp 1) (lexemep sym) (multifieldp (create$ 1 2)) (multifieldp 1) crlf)
(printout t (integer 3.7) " " (integer -3.7) " " (float 3) " " (floatp (float 3)) " " (integerp (integer 3.7)) crlf)
(printout t (div 7 2) " " (div -7 2) " " (div 100 3 3) " " (mod 7 2) crlf)
(printout t (= 1 1.0) " " (= 1 2) " " (<> 1 2) " " (!= 1 1) crlf)
(defglobal ?*limit* = 10 ?*name* = "x")
(printout t ?*limit* " " ?*name* crlf)
(defglobal ?*old* 5)
(printout t ?*old* crlf)
(deftemplate n (slot v))
(defrule not-three (n (v ?v&:(<> ?v 3))) => (printout t "not three: " ?v crlf))
(defrule is-three (n (v ?v&:(= ?v 3))) => (printout t "three: " ?v crlf))
(defrule big (n (v ?v&:(and (!= ?v 3) (> ?v ?*limit*)))) => (printout t "big: " ?v crlf))
(assert (n (v 3)))
(assert (n (v 4)))
(assert (n (v 40)))
