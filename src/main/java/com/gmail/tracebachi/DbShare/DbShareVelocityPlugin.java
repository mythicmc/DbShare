/*
 * DbShare - Multiple HikariDataSource manager for Spigot
 * Copyright (C) 2017 tracebachi@gmail.com (GeeItsZee)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DbShare;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
@Plugin(id = "dbshare", name = "DbShare", version = "@project.version@",
  description = "Multiple HikariDataSource manager for Velocity",
  authors = {"GeeItsZee (tracebachi@gmail.com)"})
public class DbShareVelocityPlugin
{
  private ConfigurationNode config;
  private final Logger logger;
  private final File dataFolder;

  @Inject
  public DbShareVelocityPlugin(ProxyServer server, Logger logger,
    @DataDirectory Path dataDirectory) {
    this.logger = logger;
    this.dataFolder = dataDirectory.toFile();
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event)
  {
    // Reload the config
    reloadConfig();

    List<DataSourceDetails> dataSourceDetailsList = new ArrayList<>();
    ConfigurationNode section = config.getNode("Databases");
    Map<Object, ? extends ConfigurationNode> sources = section.getChildrenMap();

    // Read the new data source details
    for (Map.Entry<Object, ? extends ConfigurationNode> source : sources.entrySet())
    {
      String sourceName = (String) source.getKey();
      String username = source.getValue().getNode("Username").getString();
      String password = source.getValue().getNode("Password").getString();
      String url = source.getValue().getNode("URL").getString();

      dataSourceDetailsList.add(new DataSourceDetails(sourceName, username, password, url));
    }

    // Create the data sources
    DbShare instance = new DbShare();
    instance.createDataSources(dataSourceDetailsList, logger::info, logger::error);

    // Set the DbShare instance
    DbShare.setInstance(instance);
  }

  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent event)
  {
    DbShare instance = DbShare.instance();
    if (instance != null)
    {
      // Unset the DbShare instance
      DbShare.setInstance(null);

      // Close and remove the data sources
      instance.closeAndRemoveDataSources(logger::info, logger::error);
    }
  }

  private void reloadConfig()
  {
    try
    {
      File file = saveResource(this, dataFolder, "config.yml", "config.yml");
      if (file == null) {
        logger.error("Failed to load configuration file.");
        return;
      }
      config = YAMLConfigurationLoader.builder().setPath(file.toPath()).build().load();

      if (config.isVirtual())
      {
        saveResource(this, dataFolder, "config.yml", "config.yml");
      }
    }
    catch (IOException e)
    {
      logger.error("Failed to load configuration file.", e);
    }
  }

  /**
   * Loads the resource from the JAR and saves it to the destination under the plugin's
   * data folder. By default, the destination file will not be replaced if it exists.
   * <p>
   * Source for the majority of this method can be found at:
   * https://www.spigotmc.org/threads/bungeecords-configuration-api.11214/#post-119017
   * <p>
   * Originally authored by: vemacs, Feb 15, 2014
   *
   * @param plugin Plugin that contains the resource in it's JAR.
   * @param resourceName Filename of the resource.
   * @param destinationName Filename of the destination.
   *
   * @return Destination File.
   */
  private static File saveResource(Object plugin, File dataFolder,
    String resourceName, String destinationName)
  {
    if (!dataFolder.exists() && !dataFolder.mkdir())
    {
      return null;
    }

    File destinationFile = new File(dataFolder, destinationName);
    try
    {
      if (!destinationFile.exists())
      {
        if (destinationFile.createNewFile())
        {
          try (InputStream in = plugin.getClass().getResourceAsStream(resourceName);
            OutputStream out = new FileOutputStream(destinationFile))
          {
            ByteStreams.copy(in, out);
          }
        }
        else
        {
          return null;
        }
      }
      return destinationFile;
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }
  }
}
