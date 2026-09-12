(defrule Over_30
    (woolfel.examples.model.Account
        (first ?first)
        (last ?last)
        (age ?age&:(> ?age 30))
    )
=>
    (printout t ?first " " ?last " is over 30" crlf)
)
(defrule Hobby_List
    (woolfel.examples.model.Account
        (accountId ?accountId)
        (first ?first)
        (last ?last)
    )
    (woolfel.examples.model.AccountHobby
        (accountId ?accountId)
        (hobbyCode ?hobbyCode)
        (rating ?hobbyRating)
    )
    (woolfel.examples.model.Hobby
        (hobbyCode ?hobbyCode)
        (name ?hobbyName)
    )
=>
    (printout t ?first " " ?last " has hobby " ?hobbyName crlf)
)

