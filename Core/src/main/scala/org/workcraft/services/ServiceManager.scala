package org.workcraft.services
import org.workcraft.pluginmanager.PluginManager

class GlobalServiceManager (pluginManager: PluginManager) {
  val modules = pluginManager.plugins(classOf[Module]).map(_.singleton)
  val serviceProviders = modules.map(_.serviceProvider)
  
  def implementations[T] (service: Service[GlobalScope, T]) = serviceProviders.flatMap(_.implementations(service)).toList
}