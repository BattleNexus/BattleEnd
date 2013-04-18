/*******************************************************************************
 * Copyright (c) 2012 MCForge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package net.battlenexus.bukkit.battleend.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.Plugin;

public class MySQL implements SQL {
    protected Connection connection;
    protected String IP;
    protected int port;
    protected String DB;
    protected String prefix;
    protected String username;
    protected String pass;

    public void executeQuery(String command) {
        try {
            if (connection.isClosed()) connect();
        }
        catch (SQLException e) {
            connect();
        }
        try {
            PreparedStatement pstm = connection.prepareStatement(command);
            pstm.executeUpdate();
            pstm.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeQuery(String[] commands) {
        try {
            if (connection.isClosed()) connect();
        }
        catch (SQLException e) {
            connect();
        }
        try {
            Statement statement = connection.createStatement();
            for (String s : commands) {
                try {
                    statement.executeUpdate(s);
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet fillData(String command) {
        try {
            if (connection.isClosed()) connect();
        }
        catch (SQLException e) {
            connect();
        }
        try {
            return connection.prepareStatement(command).executeQuery();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void connect() {
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            connection = DriverManager.getConnection(getURL() + DB + getProperties(),
                    username, pass);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getURL() {
        return "jdbc:mysql://" + IP + ":" + port + "/";
    }

    public String getProperties() {
        return "?autoDeserialize=true";
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    public String getFullURL() {
        return getURL() + DB;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setUsername(String user) {
        this.username = user;
    }

    public void setPassword(String pass) {
        this.pass = pass;
    }

    public void setDatabase(String DB) {
        this.DB = DB;
    }

    public void init(Plugin plugin) {
        try {
            String database = plugin.getConfig().getString("database.databasename");

            setUsername(plugin.getConfig().getString("database.username", "root_default"));
            setPassword(plugin.getConfig().getString("database.password", "password_default"));
            setDatabase(plugin.getConfig().getString("database.databasename", "trader"));
            setIP(plugin.getConfig().getString("database.ip", "127.0.0.1"));
            setPort(plugin.getConfig().getInt("database.port", 3306));
            if (username.equals("root_default") && pass.equals("password_default")) {
                plugin.getLogger().warning("Please configure your MySQL settings in the config.yml file!");
                plugin.getPluginLoader().disablePlugin(plugin);
                return;
            }
            connect();
            plugin.getLogger().info("[DtltraderDB] Connected...");
            plugin.getLogger().info("[DtltraderDB] Running init...");
            String[] execute = {
                    "CREATE DATABASE IF NOT EXISTS " + database + ";",
                    "USE " + database + ";",
                    "CREATE TABLE IF NOT EXISTS times (time_id int(11), timestamp int(10));"
            };
            executeQuery(execute);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }
    }

    public String getName() {
        return "MySQL";
    }

    public void disconnect() {
        try {
            if (getConnection() == null)
                return;
            getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
