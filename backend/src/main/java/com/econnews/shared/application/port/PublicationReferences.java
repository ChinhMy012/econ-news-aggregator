package com.econnews.shared.application.port;

/** Shared protection for the availability of citations used by published content. */
public interface PublicationReferences {
    void lock();
    boolean articleIsEssential(long id);
    boolean documentIsEssential(long id);
}
