package org.workcraft.gui

import java.awt.Font
import javax.swing.JFrame
import org.workcraft.services.GlobalServiceManager
import org.workcraft.logging.Logger
import org.workcraft.logging.Logger._
import org.workcraft.logging.StandardStreamLogger
import javax.swing.JDialog
import javax.swing.UIManager
import org.pushingpixels.substance.api.SubstanceLookAndFeel
import org.pushingpixels.substance.api.SubstanceConstants.TabContentPaneBorderKind
import org.streum.configrity.Configuration
import javax.swing.SwingUtilities
import javax.swing.JPanel
import java.awt.BorderLayout
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._
import java.awt.Frame
import javax.swing.WindowConstants
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JComponent
import org.workcraft.gui.docking.DockingRoot
import org.workcraft.gui.docking.DockableWindowConfiguration
import org.workcraft.gui.docking.DockableWindow
import org.workcraft.gui.logger.LoggerWindow
import org.workcraft.gui.modeleditor.ModelEditorPanel
import org.workcraft.services.NewModelImpl
import org.flexdock.docking.DockingManager
import org.flexdock.docking.DockingConstants
import org.workcraft.gui.modeleditor.EditorService
import javax.swing.JOptionPane
import org.workcraft.services.ModelServiceProvider
import org.workcraft.gui.modeleditor.EditorState
import org.workcraft.gui.modeleditor.tools.ToolboxPanel

class MainWindow(
  val globalServices: () => GlobalServiceManager,
  reconfigure: () => Unit,
  shutdown: MainWindow => Unit,
  configuration: Option[GuiConfiguration]) extends JFrame {
  val loggerWindow = new LoggerWindow
  implicit val implicitLogger: () => Logger[IO] = () => loggerWindow

  applyIconManager

  applyGuiConfiguration(configuration)

  setTitle("Workcraft")

  val dockingRoot = new DockingRoot("workcraft")
  setContentPane(dockingRoot)

  val toolboxWindow = new JPanel(new BorderLayout)
  toolboxWindow.add(new NotAvailablePanel(), BorderLayout.CENTER)

  val placeholderDockable = dockingRoot.createRootWindow("", "DocumentPlaceholder", new DocumentPlaceholder, DockableWindowConfiguration(false, false, false))
  val loggerDockable = createUtilityWindow("Log", "Log", loggerWindow, placeholderDockable, DockingConstants.SOUTH_REGION, 0.8)
  val toolboxDockable = createUtilityWindow("Toolbox", "Toolbox", toolboxWindow, placeholderDockable, DockingConstants.EAST_REGION, 0.8)

  var openEditors = List[DockableWindow]()

  val menu = new MainMenu(this, List(loggerDockable, toolboxDockable), globalServices, { case (m, b) => newModel(m, b) }, reconfigure)
  this.setJMenuBar(menu)

  val editorStates = new scala.collection.mutable.HashMap[DockableWindow, EditorState]
  val editorDockables = new scala.collection.mutable.HashMap[DockableWindow, ModelServiceProvider]

  def closeUtilityWindow(window: DockableWindow) = {
    window.close
    menu.windowsMenu.update(window)
  }

  def createUtilityWindow(title: String, persistentId: String, content: JComponent, relativeTo: DockableWindow, relativeRegion: String, split: Double): DockableWindow =
    dockingRoot.createWindowWithSetSplit(title, persistentId, content, DockableWindowConfiguration(maximiseButton = false, onCloseClicked = closeUtilityWindow), relativeTo, relativeRegion, split)

  private def applyGuiConfiguration(configOption: Option[GuiConfiguration]) = SwingUtilities.invokeLater(new Runnable {
    def run = {
      configOption match {
        case Some(config) => {
          setSize(config.xSize, config.ySize)
          setLocation(config.xPos, config.yPos)

          if (config.maximised)
            setExtendedState(Frame.MAXIMIZED_BOTH)
        }
        case None =>
          {
            setSize(800, 600)
          }
          SwingUtilities.updateComponentTreeUI(MainWindow.this)
      }
    }
  })

  def guiConfiguration = {
    val size = getSize()
    val maximised = (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0

    GuiConfiguration(xPos = getX(), yPos = getY(),
      xSize = size.getWidth().toInt, ySize = size.getHeight().toInt, maximised = maximised,
      lookandfeel = LafManager.getCurrentLaf)
  }

  private def applyIconManager(implicit logger: () => Logger[IO]) = MainWindowIconManager.apply(this, logger)

  def newModel(newModelImpl: NewModelImpl, editorRequested: Boolean) = {
    val model = newModelImpl.create
    if (editorRequested)
      openEditor(model)
  }

  def setFocus(editorDockable: DockableWindow) {
    toolboxWindow.removeAll()
    toolboxWindow.add(editorStates(editorDockable).toolbox, BorderLayout.CENTER)
  }

  def openEditor(model: ModelServiceProvider) {
    model.implementation(EditorService) match {
      case None => JOptionPane.showMessageDialog(this, "The model type that you have chosen does not support visual editing :(", "Warning", JOptionPane.WARNING_MESSAGE)
      case Some(editor) => {
        val toolbox = new ToolboxPanel(editor.tools)
        val editorState = new EditorState(toolbox)
        val editorPanel = new ModelEditorPanel(toolbox)(implicitLogger)
        val editorDockable = dockingRoot.createWindow("Говноэдитор", "unused", editorPanel, DockableWindowConfiguration(onCloseClicked = closeEditor), if (openEditors.isEmpty) placeholderDockable else (openEditors.head), DockingConstants.CENTER_REGION)

        editorDockables += ((editorDockable, model))
        editorStates += ((editorDockable, editorState))

        if (openEditors.isEmpty) {
          openEditors = List(editorDockable)
          DockingManager.undock(placeholderDockable)
        } else
          openEditors ::= editorDockable

        setFocus(editorDockable)
      }
    }
  }

  def closeEditor(editorDockable: DockableWindow) {
    // TODO: Ask to save etc.

    openEditors -= editorDockable
    editorDockables -= editorDockable
    editorStates -= editorDockable

    if (openEditors.isEmpty)
      DockingManager.dock(placeholderDockable, editorDockable, DockingConstants.CENTER_REGION)

    editorDockable.close
    DockingManager.undock(editorDockable)
  }

  def exit = shutdown(this)
}