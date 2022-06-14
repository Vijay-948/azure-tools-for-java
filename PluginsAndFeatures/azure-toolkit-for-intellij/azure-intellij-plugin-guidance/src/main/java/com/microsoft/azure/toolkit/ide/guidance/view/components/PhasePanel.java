package com.microsoft.azure.toolkit.ide.guidance.view.components;

import com.intellij.icons.AllIcons;
import com.intellij.ui.HideableDecorator;
import com.intellij.ui.JBColor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.ide.guidance.InputComponent;
import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.Status;
import com.microsoft.azure.toolkit.ide.guidance.input.GuidanceInput;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessage;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class PhasePanel extends JPanel {
    private JPanel pnlRoot;
    private JPanel pnlRootContent;
    private JLabel lblStatusIcon;
    private JLabel lblTitle;
    private JPanel pnlInputs;
    private JButton runButton;
    private JTextPane paneDescription;
    private JTextArea txtOutput;
    private JPanel pnlStepsHolder;
    private JPanel pnlSteps;
    private JPanel pnlOutput;
    private JPanel pnlRootContentHolder;

    private final Phase phase;
    private HideableDecorator phaseDecorator;
    private HideableDecorator stepDecorator;
    private List<GuidanceInput> inputComponents;

    public PhasePanel(@Nonnull Phase phase) {
        super();
        this.phase = phase;
        $$$setupUI$$$();
        init();
    }

    private void init() {
        this.setLayout(new GridLayoutManager(1, 1));
        this.add(pnlRoot, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0));

        renderPhase();
        txtOutput.setVisible(false);
    }

    private void renderPhase() {
        lblStatusIcon.setIcon(IntelliJAzureIcons.getIcon(AzureIcons.Common.AZURE));
        paneDescription.setText(phase.getDescription());

        phaseDecorator = new HideableDecorator(pnlRootContentHolder, phase.getTitle(), false);
        phaseDecorator.setOn(phase.getStatus() == Status.READY);
        phaseDecorator.setContentComponent(pnlRootContent);
        // Render Steps
//        fillSteps();
        renderInputs();
        prepareOutput();
        update(phase.getStatus());
        runButton.setIcon(IntelliJAzureIcons.getIcon(AzureIcons.Action.START));
        phase.addStatusListener(this::update);

        runButton.addActionListener(e -> {
            txtOutput.setVisible(true);
            inputComponents.forEach(component -> component.applyResult());
            phase.execute();
        });
    }

    private void prepareOutput() {
        final IAzureMessager messager = new ConsoleTextMessager();
        phase.setOutput(messager);
        txtOutput.setBackground(new Color(40, 40, 40));
    }

    class ConsoleTextMessager implements IAzureMessager {
        @Override
        public boolean show(IAzureMessage message) {
            txtOutput.setText(message.getContent());
            return true;
        }
    }

    private void update(final Status status) {
        // update icon
        lblStatusIcon.setIcon(getStatusIcons(status));
        if (status == Status.INITIAL) {
            this.runButton.setVisible(false);
        }
        if (status == Status.READY) {
            this.setBackgroundColor(this, new Color(69, 73, 74));
            this.phaseDecorator.setOn(true);
            this.runButton.setVisible(true);
            this.runButton.setEnabled(true);
        }
        if (status == Status.RUNNING) {
            this.runButton.setEnabled(false);
        }
        if (status == Status.SUCCEED) {
            this.runButton.setVisible(false);
            this.setBackgroundColor(this, JBColor.background());
            this.phaseDecorator.setOn(false);
        }
        // update enable/disable
    }

    private void setBackgroundColor(JPanel panel, Color color) {
        panel.setBackground(color);
        Arrays.stream(panel.getComponents()).filter(component -> component instanceof JPanel).forEach(child -> setBackgroundColor((JPanel) child, color));
        Arrays.stream(panel.getComponents()).filter(component -> component instanceof JTextPane || component instanceof JButton).forEach(child -> child.setBackground(color));
    }

    @Nullable
    private Icon getStatusIcons(final Status status) {
        if (status == Status.RUNNING) {
            return IntelliJAzureIcons.getIcon(AzureIcons.Common.REFRESH_ICON);
        } else if (status == Status.SUCCEED) {
            return AllIcons.RunConfigurations.ToolbarPassed;
        } else if (status == Status.FAILED) {
            return AllIcons.RunConfigurations.ToolbarError;
        } else {
            return null;
        }
    }

    private void renderInputs() {
        inputComponents = phase.getInputs();
        if (CollectionUtils.isEmpty(inputComponents)) {
            return;
        }
        pnlInputs.setLayout(new GridLayoutManager(inputComponents.size(), 1));
        for (int i = 0; i < inputComponents.size(); i++) {
//            final InputComponent component = inputComponents.get(i);
//            final GridConstraints gridConstraints = new GridConstraints(i, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0);
//            pnlInputs.add(component.getComponent(), gridConstraints);
        }
    }

    private void fillSteps() {
//        final List<Step> steps = phase.getSteps();
//        if (Collections.isEmpty(steps) || steps.size() == 1) {
//            // skip render steps panel for single task
//            return;
//        }
//        stepDecorator = new HideableDecorator(pnlStepsHolder, "Steps", false);
//        stepDecorator.setContentComponent(pnlSteps);
//        pnlSteps.setLayout(new GridLayoutManager(steps.size(), 1));
//        for (int i = 0; i < steps.size(); i++) {
//            final Step step = steps.get(i);
//            final StepPanel stepPanel = new StepPanel(step);
//            final GridConstraints gridConstraints = new GridConstraints(i, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0);
//            pnlSteps.add(stepPanel, gridConstraints);
//        }
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }
}
