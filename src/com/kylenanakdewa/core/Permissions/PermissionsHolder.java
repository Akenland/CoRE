package com.kylenanakdewa.core.permissions;

import com.kylenanakdewa.core.common.prompts.Prompt;

/**
 * Represents an object that may hold permission sets.
 * <p>
 * Essentially functions as the equivalent of Bukkit's
 * {@link org.bukkit.permissions.Permissible}, for CoRE Permissions.
 *
 * @author Kyle Nanakdewa
 */
@Deprecated
interface PermissionsHolder {

    /**
     * Gets the current active permission set.
     */
    PermissionSet getCurrentSet();

    /**
     * Sets the current active permission set.
     */
    void setCurrentSet(PermissionSet set);

    /**
     * Revokes all permissions.
     */
    void revokePermissions();

    /**
     * Gets an information prompt about this permission holder.
     */
    Prompt getPermissionsInfoPrompt();

}