.. _migration-guide-persistence-experimental-2.3.x-2.4.x:

#####################################################
Migration Guide Akka Persistence (experimental) 2.3.3
#####################################################

**Akka Persistence** is an **experimental module**, which means that neither Binary Compatibility nor API stability
is provided for Persistence while under the *experimental* flag. The goal of this phase is to gather user feedback
before we freeze the APIs in a major release.

Renamed EventsourcedProcessor to PersistentActor
================================================
``EventsourcedProcessor`` is now deprecated and replaced by ``PersistentActor`` which provides the same (and more) API.
Migrating to ``2.4.x`` is as simple as changing all your classes to extending  ``PersistentActor``.

Replace all classes like::

    class DeprecatedProcessor extends EventsourcedProcessor { /*...*/ }

To extend ``PersistentActor``::

    class NewPersistentProcessor extends PersistentActor { /*...*/ }

No other API changes are required for this migration.

Removed Processor in favour of extending PersistentActor with persistAsync
==========================================================================

The ``Processor`` is now deprecated since ``2.3.4`` and will be removed in ``2.4.x``.
It's semantics replicated in ``PersistentActor`` in the form of an additional ``persist`` method: ``persistAsync``.

In essence, the difference betwen ``persist`` and ``persistAsync`` is that the former will stash all incomming commands
until all persist callbacks have been processed, whereas the latter does not stash any commands. The new ``persistAsync``
should be used in cases of low consistency yet high responsiveness requirements, the Actor can keep processing incomming
commands, even though not all previous events have been handled.

When these ``persist`` and ``persistAsync`` are used together in the same ``PersistentActor``, the ``persist``
logic will win over the async version so that all guarantees concerning persist still hold. This will however lower
the throughput

Now deprecated code using Processor::

    class OldProcessor extends Processor {
      def receive = {
        case Persistent(cmd) => sender() ! cmd
      }
    }

Replacement code, with the same semantics, using PersistentActor::

    class NewProcessor extends PersistentActor {
      def receiveCommand = {
        case cmd =>
          persistAsync(cmd) { e => sender() ! e }
      }

      def receiveEvent = {
        case _ => // logic for handling replay
      }
    }

It is worth pointing out that using ``sender()`` inside the persistAsync callback block is **valid**, and does *not* suffer
any of the problems Futures have when closing over the sender reference.

Using the``PersistentActor`` instead of ``Processor`` also shifts the responsibility of deciding if a message should be persisted
to the receiver instead of the sender of the message. Previously, using ``Processor``, clients would have to wrap messages as ``Persistent(cmd)``
manually, as well as have to be aware of the receiver being a ``Processor``, which didn't play well with transparency of the ActorRefs in general.
