/*******************************************************************************
 * Copyright (c) 2012 MCForge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package net.battlenexus.bukkit.battleend.db;

import java.sql.Connection;
import java.sql.ResultSet;

import org.bukkit.plugin.Plugin;

public interface SQL {
    
    public void executeQuery(String command);
    
    public void executeQuery(String[] commands);
    
    public ResultSet fillData(String command);
    
    public void init(Plugin plugin);
    
    public void connect();
    
    public void setPrefix(String prefix);
    
    public String getPrefix();
    
    public Connection getConnection();
    
    public String getName();
    
    public void disconnect();

}
