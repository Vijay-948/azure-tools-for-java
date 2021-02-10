/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.hdinsight.sdk.rest.azure.datalake.analytics.accounts.models;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for SubscriptionState.
 */
public final class SubscriptionState extends ExpandableStringEnum<SubscriptionState> {
    /** Static value Registered for SubscriptionState. */
    public static final SubscriptionState REGISTERED = fromString("Registered");

    /** Static value Suspended for SubscriptionState. */
    public static final SubscriptionState SUSPENDED = fromString("Suspended");

    /** Static value Deleted for SubscriptionState. */
    public static final SubscriptionState DELETED = fromString("Deleted");

    /** Static value Unregistered for SubscriptionState. */
    public static final SubscriptionState UNREGISTERED = fromString("Unregistered");

    /** Static value Warned for SubscriptionState. */
    public static final SubscriptionState WARNED = fromString("Warned");

    /**
     * Creates or finds a SubscriptionState from its string representation.
     * @param name a name to look for
     * @return the corresponding SubscriptionState
     */
    @JsonCreator
    public static SubscriptionState fromString(String name) {
        return fromString(name, SubscriptionState.class);
    }

    /**
     * @return known SubscriptionState values
     */
    public static Collection<SubscriptionState> values() {
        return values(SubscriptionState.class);
    }
}
