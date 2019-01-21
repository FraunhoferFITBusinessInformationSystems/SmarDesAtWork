/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements;

import java.util.concurrent.TimeUnit;

/**
 * Created by vh on 12.03.2018.
 */

public class Constants {
    public final static String NAMESPACE = BuildConfig.APPLICATION_ID;

    public interface ACTIONS {
        String FILE_UPLOAD_SERVICE_START    = NAMESPACE + ".action.start_upload";
        String FILE_UPLOAD_SERVICE_STOP     = NAMESPACE + ".action.stop_upload";

        String GET_CONFIG_NOTIFICATION      = "GetConfig";
        String GET_DATA_NOTIFICATION        = "GetData";
        String GET_ALL_NOTIFICATION         = "GetAll";
        String PUT_DATA_NOTIFICATION        = "PutConfig";
    }

    public interface EXTRAS {
        String FIREBASE_TOKEN_KEY           = NAMESPACE + ".extra.fbtoken";
    }

    public interface NOTIFICATION_ID {
        int FILE_UPLOAD_SERVICE = 4201;
    }

    public interface VALUES {
        boolean   DEBUG_MODE                = false;
        boolean   LOG_RETROFIT              = false;
        int       ECHO_POLLING_INTERVAL     = 3;
        TimeUnit  ECHO_POLLING_TIMEUNIT     = TimeUnit.SECONDS;
    }
}