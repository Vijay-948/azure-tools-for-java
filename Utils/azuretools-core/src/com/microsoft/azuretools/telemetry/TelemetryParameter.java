/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azuretools.telemetry;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TODO(qianjin): this class is designed to replace TelemetryConstants. action: replace all references to it and then delete it.
 */
public final class TelemetryParameter {

    // This needs to be at the beginning for the initialization to happen correctly
    private static final ConcurrentMap<String, TelemetryParameter> VALUES_BY_NAME = new ConcurrentHashMap<>();
    @Getter
    private final String serviceName;
    @Getter
    private final String operationName;

    private TelemetryParameter(String serviceName, String operationName) {
        this.serviceName = serviceName;
        this.operationName = operationName;
        VALUES_BY_NAME.put(this.operationName, this);
    }

    private static String buildOperationName(String head, String tail) {
        return head + "-" + tail;
    }

    public interface WebApp {
        String MODULE = "webapp";

        TelemetryParameter CREATE = new TelemetryParameter(MODULE, "create-webapp");
        TelemetryParameter DELETE = new TelemetryParameter(MODULE, "delete-webapp");
        TelemetryParameter DEPLOY = new TelemetryParameter(MODULE, "deploy-webapp");
        TelemetryParameter START = new TelemetryParameter(MODULE, "start-webapp");
        TelemetryParameter STOP = new TelemetryParameter(MODULE, "stop-webapp");
        TelemetryParameter RESTART = new TelemetryParameter(MODULE, "restart-webapp");
        TelemetryParameter SHOW_PROPERTIES = new TelemetryParameter(MODULE, "showprop-webapp");
        TelemetryParameter SHINTO = new TelemetryParameter(MODULE, "sshinto-webapp");
        TelemetryParameter OPEN_IN_PORTAL = new TelemetryParameter(MODULE, "open-inbrowser-webapp");
        TelemetryParameter START_STREAMING_LOG = new TelemetryParameter(MODULE, "start-streaming-log-webapp");
        TelemetryParameter STOP_STREAMING_LOG = new TelemetryParameter(MODULE, "stop-streaming-log-webapp");

        TelemetryParameter DEPLOY_DOCKERLOCAL = new TelemetryParameter(MODULE, "deploy-webapp-dockerlocal");
        TelemetryParameter DEPLOY_DOCKERHOST = new TelemetryParameter(MODULE, "deploy-webapp-dockerhost");
        TelemetryParameter DEPLOY_CONTAINER = new TelemetryParameter(MODULE, "deploy-webapp-container");
        // deployment slot
        TelemetryParameter OPEN_CREATEWEBAPP_DIALOG = new TelemetryParameter(MODULE, "open-create-webapp-dialog");

        TelemetryParameter DEPLOY_TO_SLOT = new TelemetryParameter(MODULE, "webappDeployToSlot");
        TelemetryParameter CREATE_NEWWEBAPP = new TelemetryParameter(MODULE, "createNewWebapp");

        interface DeploymentSlot {
            TelemetryParameter DEPLOY = new TelemetryParameter(MODULE, "deploy-webapp-slot");
            TelemetryParameter CREATE = new TelemetryParameter(MODULE, "create-webapp-slot");
            TelemetryParameter STOP = new TelemetryParameter(MODULE, "stop-webapp-slot");
            TelemetryParameter START = new TelemetryParameter(MODULE, "start-webapp-slot");
            TelemetryParameter RESTART = new TelemetryParameter(MODULE, "restart-webapp-slot");
            TelemetryParameter DELETE = new TelemetryParameter(MODULE, "delete-webapp-slot");
            TelemetryParameter SWAP = new TelemetryParameter(MODULE, "swap-webapp-slot");
            TelemetryParameter SHOW_PROPERTIES = new TelemetryParameter(MODULE, "show-webapp-slot-prop");
            TelemetryParameter OPEN_IN_PORTAL = new TelemetryParameter(MODULE, "open-webappslot-inbrowser");
            TelemetryParameter START_STREAMING_LOG = new TelemetryParameter(MODULE, "start-streaming-log-webapp-slot");
            TelemetryParameter STOP_STREAMING_LOG = new TelemetryParameter(MODULE, "stop-streaming-log-webapp-slot");
        }
    }

    public interface FunctionApp {
        String MODULE = "function";

        TelemetryParameter TRIGGER = new TelemetryParameter(MODULE, buildOperationName("trigger", MODULE));
        TelemetryParameter ENABLE = new TelemetryParameter(MODULE, buildOperationName("enable", MODULE));
        TelemetryParameter DISABLE = new TelemetryParameter(MODULE, buildOperationName("disable", MODULE));
        TelemetryParameter RUN = new TelemetryParameter(MODULE, "run-function-app");
        TelemetryParameter CREATE_TRIGGER = new TelemetryParameter(MODULE, "create-function-trigger");
        TelemetryParameter CREATE_PROJECT = new TelemetryParameter(MODULE, "create-function-project");
        TelemetryParameter CREATE = new TelemetryParameter(MODULE, "create-function-app");
        TelemetryParameter DEPLOY = new TelemetryParameter(MODULE, "deploy-function-app");
        TelemetryParameter DELETE = new TelemetryParameter(MODULE, "delete-function-app");
        TelemetryParameter START = new TelemetryParameter(MODULE, "start-function-app");
        TelemetryParameter STOP = new TelemetryParameter(MODULE, "stop-function-app");
        TelemetryParameter RESTART = new TelemetryParameter(MODULE, "restart-function-app");
        TelemetryParameter SHOW_PROPERTIES = new TelemetryParameter(MODULE, "showprop-function-app");
        TelemetryParameter START_STREAMING_LOG = new TelemetryParameter(MODULE, "start-streaming-log-function-app");
        TelemetryParameter STOP_STREAMING_LOG = new TelemetryParameter(MODULE, "stop-streaming-log-function-app");
        TelemetryParameter OPEN_IN_PORTAL = new TelemetryParameter(MODULE, "open-inbrowser-function-app");
    }

    public interface MySQL {
        String MODULE = "azure-mysql";

        TelemetryParameter CREATE = new TelemetryParameter(MODULE, buildOperationName(MODULE, "create"));
        TelemetryParameter DELETE = new TelemetryParameter(MODULE, buildOperationName(MODULE, "delete"));
        TelemetryParameter START = new TelemetryParameter(MODULE, buildOperationName(MODULE, "start"));
        TelemetryParameter STOP = new TelemetryParameter(MODULE, buildOperationName(MODULE, "stop"));
        TelemetryParameter RESTART = new TelemetryParameter(MODULE, buildOperationName(MODULE, "restart"));
        TelemetryParameter OPEN_IN_PORTAL = new TelemetryParameter(MODULE, buildOperationName(MODULE, "open-in-portal"));
        TelemetryParameter SHOW_PROPERTIES = new TelemetryParameter(MODULE, buildOperationName(MODULE, "show-properties"));
        TelemetryParameter CONNECT_TO_SERVER = new TelemetryParameter(MODULE, buildOperationName(MODULE, "connect-to-server"));
        TelemetryParameter BIND_INTO = new TelemetryParameter(MODULE, buildOperationName(MODULE, "bind-into"));
    }

    @Override
    public int hashCode() {
        return this.operationName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof TelemetryParameter)) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            TelemetryParameter rhs = (TelemetryParameter) obj;
            return this.operationName.equalsIgnoreCase(rhs.operationName);
        }
    }

}
