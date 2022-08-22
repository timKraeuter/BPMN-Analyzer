# BPMN to Groove transformation

## Message semantics
- **Each process instance** has a **multi-set of received messages**.
- Message semantics are **asynchronous**, i.e.,
    - _Message throw events_ add a message to a process instance only if the message could be processed immediately (i.e. the corresponding message catch event has incoming tokens). Otherwise, no message is created but the message throw event is triggered.
    - _Message catch events_ consume messages from the received messages multi-set.

It would be easy to make message semantics synchronous.

## Signal semantics
- Signal semantics are **synchronous**. Similar to the [synchronous implementation](https://docs.camunda.org/manual/7.16/reference/bpmn20/events/signal-events/#signal-intermediate-throwing-event) in camunda.
- One graph transformation rule triggers a _signal throw event_ and all corresponding _signal catch events_ with incoming tokens.
  - This is implement using a nested rule with a **vacious forAll** quantifier.

# BPMN to Maude transformation

## Message semantics
- There is a **shared multi-set for all messages**.
- Message semantics are fully **asynchronous**, i.e.,
  - _Message throw events_ add messages into the multi-set.
  - _Message catch events_ consume messages from the multi-set.
  - Messages are **persistent** and only deleted by message catch events. This might lead to **infinite state spaces** if messages are created but never consumes repeatedly.

## Signal semantics
- Signal semantics are **asynchronous**. Similar to the [asynchronous implementation](https://docs.camunda.org/manual/7.16/reference/bpmn20/events/signal-events/#signal-intermediate-throwing-event) in camunda.
- _Signal throw event_ create signals for all corresponding signal catch events with incoming tokens.
- _Signal catch events_ consume incoming tokens and a signal. They can occur anytime in the future after a signal catch event.
