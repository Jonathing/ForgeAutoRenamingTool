/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.renamer.gradle;

import net.minecraftforge.gradleutils.shared.EnhancedPlugin;
import org.gradle.api.Project;

import javax.inject.Inject;

abstract class RenamerPlugin extends EnhancedPlugin<Project> {
    static final String NAME = "renamer";
    static final String DISPLAY_NAME = "Renamer Gradle";

    @Inject
    public RenamerPlugin() {
        super(NAME, DISPLAY_NAME, "renamerTools");
    }

    @Override
    public void setup(Project project) { }
}
