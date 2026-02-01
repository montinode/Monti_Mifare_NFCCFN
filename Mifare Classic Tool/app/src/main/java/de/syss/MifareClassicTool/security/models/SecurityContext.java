/*
 * Copyright 2013 Gerhard Klostermeier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.syss.MifareClassicTool.security.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Security context for key derivation operations.
 * Contains state and configuration for security operations.
 * @author Monti Security Team
 */
public class SecurityContext {
    private final String contextId;
    private final long createdAt;
    private final List<String> operations;
    private boolean locked;

    /**
     * Create a new security context.
     * @param contextId Unique identifier for this context
     */
    public SecurityContext(String contextId) {
        if (contextId == null || contextId.isEmpty()) {
            throw new IllegalArgumentException("Context ID cannot be null or empty");
        }
        this.contextId = contextId;
        this.createdAt = System.currentTimeMillis();
        this.operations = new ArrayList<>();
        this.locked = false;
    }

    /**
     * Get the context ID.
     * @return Context identifier
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Get the creation timestamp.
     * @return Timestamp in milliseconds
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Log a security operation.
     * @param operation Description of the operation
     */
    public synchronized void logOperation(String operation) {
        if (!locked) {
            operations.add(System.currentTimeMillis() + ": " + operation);
        }
    }

    /**
     * Get all logged operations.
     * @return List of operations
     */
    public synchronized List<String> getOperations() {
        return new ArrayList<>(operations);
    }

    /**
     * Lock the context to prevent further operations.
     */
    public synchronized void lock() {
        this.locked = true;
    }

    /**
     * Check if the context is locked.
     * @return true if locked
     */
    public synchronized boolean isLocked() {
        return locked;
    }

    /**
     * Get the age of this context in milliseconds.
     * @return Age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - createdAt;
    }

    @Override
    public String toString() {
        return "SecurityContext{" +
                "contextId='" + contextId + '\'' +
                ", createdAt=" + createdAt +
                ", operations=" + operations.size() +
                ", locked=" + locked +
                '}';
    }
}
