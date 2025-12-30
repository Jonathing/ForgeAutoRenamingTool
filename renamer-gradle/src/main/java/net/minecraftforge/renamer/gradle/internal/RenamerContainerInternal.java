package net.minecraftforge.renamer.gradle.internal;

import net.minecraftforge.renamer.gradle.RenamerContainer;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

interface RenamerContainerInternal extends RenamerContainer, HasPublicType {
    @Override
    default TypeOf<?> getPublicType() {
        return TypeOf.typeOf(RenamerContainer.class);
    }
}
