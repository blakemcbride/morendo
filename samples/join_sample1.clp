;; Join between two Java bean templates. Used by the service configuration samples
;; (samples/configuration/*.json), which declare the classes before loading this file.
(defrule join_account_phoneline
    (woolfel.examples.model.Account
        (accountId ?id)
        (first ?first)
        (last ?last)
    )
    (woolfel.examples.model.Account2
        (accountId ?id)
        (phoneline ?phone)
    )
=>
    (printout t ?first " " ?last " has phone line " ?phone crlf)
)
