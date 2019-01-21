/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.entities;

import java.util.Date;

import de.vogler_engineering.smartdevicesapp.model.management.OnlineState;
import lombok.Data;

@Data
public class EchoResult {

    private final long ms;
    private final OnlineState onlineState;
    private final long msUp;
    private final long msDown;
    private final Date requestDate;
    private final Throwable throwable;

}
