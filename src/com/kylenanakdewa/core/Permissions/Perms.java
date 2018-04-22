package com.kylenanakdewa.core.Permissions;

import java.util.List;

// Represents the access an object has to permission sets
interface Perms {

    // Getting permission sets
    public PermissionSet getDefault();
	public PermissionSet getUtility();
    public PermissionSet getCheat();
    public List<PermissionSet> getOtherSets();
    
    public List<PermissionSet> getAllSets();

    public List<PermissionSet> getAdminSupervisedSets();
    public List<PermissionSet> getInstantAccessSets();

}