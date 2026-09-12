;; Rule over the woolfel.examples.model.Account4 bean (see src/test).
;; The Java side must call engine.declareObject(Account4.class) before loading this file.
(defrule account4_asserted
    (woolfel.examples.model.Account4
        (accountId ?id)
    )
=>
    (printout t "Account4 asserted with id " ?id crlf)
)
