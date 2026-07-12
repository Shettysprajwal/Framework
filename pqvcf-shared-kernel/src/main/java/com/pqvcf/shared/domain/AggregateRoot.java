package com.pqvcf.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all aggregate roots in the PQVCF domain model.
 *
 * <p>An aggregate root is the entry point to an aggregate — a cluster of domain objects
 * that should be treated as a unit for the purpose of data changes. Only the aggregate
 * root should be directly persisted or referenced by external aggregates.
 *
 * <p>This class manages domain events raised during the aggregate's lifecycle.
 * Domain events are collected and dispatched by the infrastructure layer after persistence.
 *
 * <p>Research Note: Aggregate roots enforce consistency boundaries critical for
 * compliance proof generation. All state changes that affect compliance must flow
 * through the aggregate root to ensure domain events are captured for the audit trail.
 *
 * @param <ID> The type of the aggregate's identity
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public abstract class AggregateRoot<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Returns the unique identifier of this aggregate root.
     *
     * @return the aggregate's identity, never null
     */
    public abstract ID getId();

    /**
     * Raises a domain event to be dispatched after the aggregate is persisted.
     *
     * @param event the domain event to raise, must not be null
     */
    protected void raiseEvent(DomainEvent event) {
        Objects.requireNonNull(event, "Domain event must not be null");
        this.domainEvents.add(event);
    }

    /**
     * Returns all raised domain events and clears the internal event list.
     * Called by the infrastructure layer after successful persistence.
     *
     * @return an unmodifiable snapshot of the domain events raised
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = Collections.unmodifiableList(new ArrayList<>(this.domainEvents));
        this.domainEvents.clear();
        return events;
    }

    /**
     * Returns a read-only view of pending domain events without clearing them.
     *
     * @return unmodifiable list of pending domain events
     */
    public List<DomainEvent> peekDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregateRoot<?> that = (AggregateRoot<?>) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
