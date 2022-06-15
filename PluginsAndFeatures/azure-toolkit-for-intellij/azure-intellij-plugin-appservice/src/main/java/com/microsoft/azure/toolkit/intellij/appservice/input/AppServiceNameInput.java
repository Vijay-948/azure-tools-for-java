package com.microsoft.azure.toolkit.intellij.appservice.input;

import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.config.InputConfig;
import com.microsoft.azure.toolkit.ide.guidance.input.GuidanceInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;

public class AppServiceNameInput implements GuidanceInput {
    public static final String SUBSCRIPTION_ID = "subscriptionId";
    public static final String APP_SERVICE_NAME = "appServiceName";
    private final InputConfig config;
    private final ComponentContext context;

    private final AppServiceNameInputPanel inputPanel;

    public AppServiceNameInput(@Nonnull InputConfig config, @Nonnull ComponentContext context) {
        this.config = config;
        this.context = context;
        this.inputPanel = new AppServiceNameInputPanel();

        this.setSubscriptionId((String) context.getParameter(SUBSCRIPTION_ID));
        this.inputPanel.setValue((String) context.getParameter(APP_SERVICE_NAME));
        context.addPropertyListener(APP_SERVICE_NAME, name -> inputPanel.setValue((String) name));
        context.addPropertyListener(SUBSCRIPTION_ID, subscriptionId -> setSubscriptionId((String) subscriptionId));
    }

    @Override
    public String getDescription() {
        return config.getDescription();
    }

    @Override
    public JComponent getComponent() {
        return inputPanel.getRootPanel();
    }

    @Override
    public void applyResult() {
        context.applyResult(APP_SERVICE_NAME, inputPanel.getValue());
    }

    @Override
    public AzureValidationInfo getValidationInfo() {
        return inputPanel.getValidationInfo();
    }

    private void setSubscriptionId(final String subscriptionId) {
        if (StringUtils.isNotBlank(subscriptionId)) {
            inputPanel.setSubscriptionId(subscriptionId);
        }
    }
}
