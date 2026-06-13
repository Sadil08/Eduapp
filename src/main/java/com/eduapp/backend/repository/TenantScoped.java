package com.eduapp.backend.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a repository whose rows belong to a single tenant ({@code school_id}). Callers
 * must always scope queries to the current tenant; the inherited no-arg {@code findAll()}
 * would leak every tenant's rows and is forbidden by the ArchUnit rule in
 * {@code TenantArchitectureTest}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TenantScoped {
}
