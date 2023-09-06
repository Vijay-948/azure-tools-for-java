package com.microsoft.azure.toolkit.ide.cosmos.spark;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.tooling.msservices.components.DefaultLoader;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class SparkOnCosmosActionsContributor implements IActionsContributor {
    private static final String SPARK_NOTEBOOK_LINK = "https://aka.ms/spkadlnb";
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;
    public static final String SERVICE_ACTIONS = "actions.sparkoncosmos.service";
    public static final Action.Id<ResourceGroup> GROUP_CREATE_SOC_SERVICE = Action.Id.of("user/sparkoncosmos.create_sparkoncosmos.group");
    public static final String ADLA_NODE_ACTIONS = "actions.sparkoncosmos.adla";

    public static final Action.Id<Object> PROVISION_CLUSTER = Action.Id.of("user/sparkoncosmos.provision_cluster.spark");
    public static final Action.Id<Object> SUBMIT_SOC_SERVERLESS_JOB = Action.Id.of("user/sparkoncosmos.submit_serverlessjob.spark");
    public static final Action.Id<AzResource> OPEN_SPARK_HISTORY_UI = Action.Id.of("user/hdinsight.open_history_ui.spark");

    public static final Action.Id<Object> OPEN_NOTEBOOK = Action.Id.of("user/sparkoncosmos.open_notebook.spark");

    @Override
    public void registerActions(AzureActionManager am) {
        new Action<>(OPEN_NOTEBOOK)
                .withLabel("Open Notebook")
                .enableWhen(s -> true)
                .withAuthRequired(false)
                .withHandler(resource -> {
                    try {
                        Desktop.getDesktop().browse(URI.create(SPARK_NOTEBOOK_LINK));
                    } catch (IOException ignore) {
                    }
                })
                .withShortcut(am.getIDEDefaultShortcuts().edit())
                .register(am);

        new Action<>(PROVISION_CLUSTER)
                .withLabel("Provision Spark Cluster")
                .enableWhen(s -> true)
                .withAuthRequired(false)
                .withShortcut(am.getIDEDefaultShortcuts().edit())
                .register(am);

        new Action<>(SUBMIT_SOC_SERVERLESS_JOB)
                .withLabel("Submit Apache Spark on Cosmos Serverless Job")
                .enableWhen(s -> true)
                .withAuthRequired(false)
                .withShortcut(am.getIDEDefaultShortcuts().edit())
                .register(am);
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                this.OPEN_NOTEBOOK
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup adlaActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                this.PROVISION_CLUSTER,
                this.SUBMIT_SOC_SERVERLESS_JOB
        );
        am.registerGroup(ADLA_NODE_ACTIONS, adlaActionGroup);

        final IActionGroup group = am.getGroup(ResourceCommonActionsContributor.RESOURCE_GROUP_CREATE_ACTIONS);
        group.addAction(GROUP_CREATE_SOC_SERVICE);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
