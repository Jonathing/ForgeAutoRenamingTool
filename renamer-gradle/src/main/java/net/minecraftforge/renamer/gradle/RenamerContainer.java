package net.minecraftforge.renamer.gradle;

import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderConvertible;
import org.gradle.api.tasks.TaskProvider;

public interface RenamerContainer {
    default void mappings(String channel, String version) {
        mappings("net.minecraft:mappings_" + channel + ':' + version + "@tsrg.gz");
    }

    void mappings(String artifact);

    void mappings(Dependency dependency);

    void mappings(Provider<? extends Dependency> dependency);

    default void mappings(ProviderConvertible<? extends Dependency> dependency) {
        this.mappings(dependency.asProvider());
    }

    TaskProvider<? extends Task> getJarTask();
}
