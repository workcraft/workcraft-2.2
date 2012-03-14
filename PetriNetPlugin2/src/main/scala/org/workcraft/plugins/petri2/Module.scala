package org.workcraft.plugins.petri2
import org.workcraft.services.Module
import org.workcraft.services.Service
import org.workcraft.services.GlobalServiceProvider
import org.workcraft.services.NewModelImpl
import org.workcraft.services.GlobalScope
import org.workcraft.services.NewModelService
import org.workcraft.services.ModelServiceProvider
import org.workcraft.services.Exporter
import org.workcraft.services.ExporterService

object NewPetriNet extends NewModelImpl {
  def name = "Petri Net"
  def create = new PetriNetModel
}

object PetriNetServiceProvider extends GlobalServiceProvider {
  def implementations[T](service: Service[GlobalScope, T]) = service match {
    case NewModelService => List(NewPetriNet)
    case ExporterService => List(LolaExporter, PnExporter)
    case _ => Nil
  }
}

class PetriNetModule extends Module {
  def name = "Petri Net 2"
  def serviceProvider = PetriNetServiceProvider
}
