/**
 * Copyright 2007 Karl-Heinz Krempels, Alexander Wilden
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://jamocha.sourceforge.net/
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.morendo.gui.tab.settings;

import org.morendo.gui.JamochaGui;
import org.morendo.messagerouter.StringChannel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial") // Swing components are never serialized here
public final class EngineSettingsPanel extends AbstractSettingsPanel implements ActionListener {

    private JCheckBox profileAssertCheckBox;

    private JCheckBox profileRetractCheckBox;

    private JCheckBox profileFireCheckBox;

    private JCheckBox profileAddActivationCheckBox;

    private JCheckBox profileRemoveActivationCheckBox;

    private JCheckBox watchActivationsCheckBox;

    private JCheckBox watchFactsCheckBox;

    private JCheckBox watchRulesCheckBox;

    public EngineSettingsPanel(JamochaGui gui) {
        super(gui);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        setLayout(gridbag);

        // Profile Assert
        addLabel(this, new JLabel("Profile Assert:"), gridbag, c, 1);
        JPanel profileAssertPanel = new JPanel(new BorderLayout());

        profileAssertCheckBox = new JCheckBox();
        profileAssertCheckBox.setEnabled(true);
        profileAssertCheckBox.addActionListener(this);
        profileAssertPanel.add(profileAssertCheckBox, BorderLayout.WEST);
        addInputComponent(this, profileAssertPanel, gridbag, c, 1);

        // Profile Retract
        addLabel(this, new JLabel("Profile Retract:"), gridbag, c, 2);
        JPanel profileRetractPanel = new JPanel(new BorderLayout());

        profileRetractCheckBox = new JCheckBox();
        profileRetractCheckBox.setEnabled(true);
        profileRetractCheckBox.addActionListener(this);
        profileRetractPanel.add(profileRetractCheckBox, BorderLayout.WEST);
        addInputComponent(this, profileRetractPanel, gridbag, c, 2);

        // Profile Fire
        addLabel(this, new JLabel("Profile Fire:"), gridbag, c, 3);
        JPanel profileFirePanel = new JPanel(new BorderLayout());

        profileFireCheckBox = new JCheckBox();
        profileFireCheckBox.setEnabled(true);
        profileFireCheckBox.addActionListener(this);
        profileFirePanel.add(profileFireCheckBox, BorderLayout.WEST);
        addInputComponent(this, profileFirePanel, gridbag, c, 3);

        // Profile Add Activation
        addLabel(this, new JLabel("Profile Add Activation:"), gridbag, c, 4);
        JPanel profileAddActivationPanel = new JPanel(new BorderLayout());

        profileAddActivationCheckBox = new JCheckBox();
        profileAddActivationCheckBox.setEnabled(true);
        profileAddActivationCheckBox.addActionListener(this);
        profileAddActivationPanel.add(profileAddActivationCheckBox, BorderLayout.WEST);
        addInputComponent(this, profileAddActivationPanel, gridbag, c, 4);

        // Profile Remove Activation
        addLabel(this, new JLabel("Profile Remove Activation:"), gridbag, c, 5);
        JPanel profileRemoveActivationPanel = new JPanel(new BorderLayout());

        profileRemoveActivationCheckBox = new JCheckBox();
        profileRemoveActivationCheckBox.setEnabled(true);
        profileRemoveActivationCheckBox.addActionListener(this);
        profileRemoveActivationPanel.add(profileRemoveActivationCheckBox, BorderLayout.WEST);
        addInputComponent(this, profileRemoveActivationPanel, gridbag, c, 5);

        // Activations
        addLabel(this, new JLabel(" Watch Activations:"), gridbag, c, 6);
        JPanel watchActivationsPanel = new JPanel(new BorderLayout());

        watchActivationsCheckBox = new JCheckBox();
        watchActivationsCheckBox.setEnabled(true);
        watchActivationsCheckBox.addActionListener(this);
        watchActivationsPanel.add(watchActivationsCheckBox, BorderLayout.WEST);
        addInputComponent(this, watchActivationsPanel, gridbag, c, 6);

        // Facts
        addLabel(this, new JLabel("Watch Facts:"), gridbag, c, 7);
        JPanel watchFactsPanel = new JPanel(new BorderLayout());

        watchFactsCheckBox = new JCheckBox();
        watchFactsCheckBox.setEnabled(true);
        watchFactsCheckBox.addActionListener(this);
        watchFactsPanel.add(watchFactsCheckBox, BorderLayout.WEST);
        addInputComponent(this, watchFactsPanel, gridbag, c, 7);

        // Rules
        addLabel(this, new JLabel("Watch Rules:"), gridbag, c, 8);
        JPanel watchRulesPanel = new JPanel(new BorderLayout());

        watchRulesCheckBox = new JCheckBox();
        watchRulesCheckBox.setEnabled(true);
        watchRulesCheckBox.addActionListener(this);
        watchRulesPanel.add(watchRulesCheckBox, BorderLayout.WEST);
        addInputComponent(this, watchRulesPanel, gridbag, c, 8);
    }

    @Override
    public void save() {
        gui.getPreferences()
                .put("engine.profileAssert", Boolean.toString(profileAssertCheckBox.isSelected()));
        gui.getPreferences()
                .put(
                        "engine.profileRetract",
                        Boolean.toString(profileRetractCheckBox.isSelected()));
        gui.getPreferences()
                .put("engine.profileFire", Boolean.toString(profileFireCheckBox.isSelected()));
        gui.getPreferences()
                .put(
                        "engine.profileAddActivation",
                        Boolean.toString(profileAddActivationCheckBox.isSelected()));
        gui.getPreferences()
                .put(
                        "engine.profileRemoveActivation",
                        Boolean.toString(profileRemoveActivationCheckBox.isSelected()));
        gui.getPreferences()
                .put(
                        "engine.watchActivations",
                        Boolean.toString(watchActivationsCheckBox.isSelected()));
        gui.getPreferences()
                .put("engine.watchFacts", Boolean.toString(watchFactsCheckBox.isSelected()));
        gui.getPreferences()
                .put("engine.watchRules", Boolean.toString(watchRulesCheckBox.isSelected()));
    }

    public void actionPerformed(ActionEvent event) {

        StringChannel guiStringChannel = gui.getStringChannel();

        if (event.getSource() == profileAssertCheckBox) {
            if (profileAssertCheckBox.isSelected())
                guiStringChannel.executeCommand("(profile assert-fact)");
            else guiStringChannel.executeCommand("(unprofile assert-fact)");
        } else if (event.getSource() == profileRetractCheckBox) {
            if (profileRetractCheckBox.isSelected())
                guiStringChannel.executeCommand("(profile retract-fact)");
            else guiStringChannel.executeCommand("(unprofile retract-fact)");
        } else if (event.getSource() == profileFireCheckBox) {
            if (profileFireCheckBox.isSelected()) guiStringChannel.executeCommand("(profile fire)");
            else guiStringChannel.executeCommand("(unprofile fire)");
        } else if (event.getSource() == profileAddActivationCheckBox) {
            if (profileAddActivationCheckBox.isSelected())
                guiStringChannel.executeCommand("(profile add-activation)");
            else guiStringChannel.executeCommand("(unprofile add-activation)");
        } else if (event.getSource() == profileRemoveActivationCheckBox) {
            if (profileRemoveActivationCheckBox.isSelected())
                guiStringChannel.executeCommand("(profile remove-activation)");
            else guiStringChannel.executeCommand("(unprofile remove-activation)");
        } else if (event.getSource() == watchActivationsCheckBox) {
            if (watchActivationsCheckBox.isSelected())
                guiStringChannel.executeCommand("(watch activations)");
            else guiStringChannel.executeCommand("(unwatch activations)");
        } else if (event.getSource() == watchFactsCheckBox) {
            if (watchFactsCheckBox.isSelected()) guiStringChannel.executeCommand("(watch facts)");
            else guiStringChannel.executeCommand("(unwatch facts)");
        } else if (event.getSource() == watchRulesCheckBox) {
            if (watchRulesCheckBox.isSelected()) guiStringChannel.executeCommand("(watch rules)");
            else guiStringChannel.executeCommand("(unwatch rules)");
        }
    }
}
