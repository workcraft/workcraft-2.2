import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.graphics.Graphics._
import org.workcraft.plugins.shared.CommonVisualSettings
import org.workcraft.graphics.formularendering.FormulaToGraphics
import org.workcraft.graphics.LabelPositioning
import pcollections.TreePVector
import java.awt.geom.Rectangle2D
import java.awt.BasicStroke
import pcollections.PVector
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Scalaz._
import org.workcraft.plugins.cpog.VariableState
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.plugins.cpog.VisualComponent
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty
import org.workcraft.util.Pair
import org.workcraft.graphics.RichGraphicalContent

package org.workcraft.plugins.cpog.scala {

  object VisualVariable {
    val size = 1
    private val strokeWidth = 0.08f

    private val nameFont = FormulaToGraphics.fancyFont
    private val valueFont = nameFont.deriveFont(0.75f)

    def image(variable: Variable): Expression[RichGraphicalContent] =
      for (
        state <- variable.state;
        label <- variable.visualProperties.label;
        fillColor <- CommonVisualSettings.fillColor : Expression[java.awt.Color];
        foreColor <- CommonVisualSettings.foregroundColor : Expression[java.awt.Color];
        labelPositioning <- variable.visualProperties.labelPositioning
      ) yield {
        val frame = rectangle(size, size, Some ((new BasicStroke(strokeWidth), foreColor)), Some(fillColor))
        val valueLabel = formulaLabel(state.toString, valueFont, foreColor)
        val nameLabel = formulaLabel(label, nameFont, foreColor)

        (valueLabel align (frame, HorizontalAlignment.Center, VerticalAlignment.Center)) over
        (nameLabel alignSideways (frame, labelPositioning)) over
        frame
      }
    
	def getProperties(v : Variable) : PVector[EditableProperty] = {
		val states = (TreePVector.empty[Pair[String, VariableState]]
		.plus(Pair.of("[1] true", VariableState.TRUE))
		.plus(Pair.of("[0] false", VariableState.FALSE))
		.plus(Pair.of("[?] undefined", VariableState.UNDEFINED)))
		
		VisualComponent.getProperties(v.visualProperties)
			.plus(ChoiceProperty.create("State", states, v.state));
	}
    
  }
}
/*

public class VisualVariableGui
{
	
	private static BoundedColorisableGraphicalContent makeLabel(final String formula) {
		return FormulaToGraphics.print(formula, valueFont, Label.podgonFontRenderContext()).asBoundedColorisableImage();
	}
	
	private static Function<String, BoundedColorisableGraphicalContent> makeLabel =
		new Function<String, BoundedColorisableGraphicalContent>() {
			@Override
			public BoundedColorisableGraphicalContent apply(String argument) {
				return makeLabel(argument);
			}
		};
	
	private static Function<Object, String> toString = new Function<Object, String>(){
		@Override
		public String apply(Object argument) {
			return argument.toString();
		}
	};
	
	public static void toggle(Variable var)
	{
		var.state.setValue(eval(var.state).toggle());
	}	
	
	public static Expression<BoundedColorisableGraphicalContent> getImage(Variable var)  {
		Function2<BoundedColorisableGraphicalContent, GraphicsAlignment, BoundedColorisableGraphicalContent> labelNamePositioner = new Function2<BoundedColorisableGraphicalContent, GraphicsAlignment, BoundedColorisableGraphicalContent> (){
			@Override
			public BoundedColorisableGraphicalContent apply(BoundedColorisableGraphicalContent image, GraphicsAlignment positioning) {
				return positionRelative(visualBox, positioning, image);
			}
		};
		
		final Expression<BoundedColorisableGraphicalContent> valueLabel = fmap(composition(toString, makeLabel, centerToZero), var.state);
		final Expression<BoundedColorisableGraphicalContent> nameLabel = fmap(labelNamePositioner, fmap(makeLabel, var.label), var.visualVar.labelPosition);
		final Expression<BoundedColorisableGraphicalContent> box = simpleColorisableRectangle(visualBox);
		return fmap(composeFunc, fmap(composeFunc, box, nameLabel), valueLabel);
	}
	
	public static Expression<BoundedColorisableGraphicalContent> simpleColorisableRectangle(final Rectangle2D rect) {
		return fmap(new Function2<Color, Color, BoundedColorisableGraphicalContent>(){

			@Override
			public BoundedColorisableGraphicalContent apply(final Color fillColor, final Color foreColor) {
				return new BoundedColorisableGraphicalContent(new ColorisableGraphicalContent(){

					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						Color colorisation = r.getColorisation().getColorisation();
						
						Shape shape = BoundingBoxHelper.expand(rect, -strokeWidth, -strokeWidth);

						g.setStroke(new BasicStroke(strokeWidth));

						g.setColor(Coloriser.colorise(fillColor, colorisation));
						g.fill(shape);
						g.setColor(Coloriser.colorise(foreColor, colorisation));
						g.draw(shape);
					}
				}, rect);
			}
		}, CommonVisualSettings.fillColor, CommonVisualSettings.foregroundColor);
	}

	public static Rectangle2D visualBox = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
}
*/
