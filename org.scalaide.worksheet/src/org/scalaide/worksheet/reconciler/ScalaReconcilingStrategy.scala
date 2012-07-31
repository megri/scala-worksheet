package org.scalaide.worksheet
package reconciler

import org.eclipse.jface.text.reconciler._
import org.eclipse.jface.text._
import org.eclipse.jface.text.source._
import org.eclipse.ui.texteditor._
import org.eclipse.ui.part.FileEditorInput

import org.eclipse.core.resources.IFile
import org.eclipse.jdt.core.compiler.IProblem

import scala.tools.eclipse.logging.HasLogger
import scala.tools.eclipse.{ ScalaPlugin, ScalaProject }

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider.ProblemAnnotation

class ScalaReconcilingStrategy(textEditor: ITextEditor) extends IReconcilingStrategy with HasLogger {
  private var document: IDocument = _
  private lazy val annotationModel = textEditor.getDocumentProvider.getAnnotationModel(textEditor.getEditorInput)

  lazy val scriptUnit = ScriptCompilationUnit.fromEditor(textEditor).get // we know the editor is a Scala Script editor

  def setDocument(doc: IDocument) {
    document = doc

    doc.addDocumentListener(reloader)
  }

  def reconcile(dirtyRegion: DirtyRegion, subRegion: IRegion) {
    logger.debug("Incremental reconciliation not implemented.")
  }

  def reconcile(partition: IRegion) {
    val errors = scriptUnit.reconcile(document.get)

    updateErrorAnnotations(errors)
  }

  private var previousAnnotations = List[ProblemAnnotation]()

  private def updateErrorAnnotations(errors: List[IProblem]) {
    def position(p: IProblem) = new Position(p.getSourceStart, p.getSourceEnd - p.getSourceStart + 1)

    previousAnnotations.foreach(annotationModel.removeAnnotation)

    for (e <- errors) {
      //      val annotation = new Annotation("org.scala-ide.sdt.core.problem", false, e.getMessage) // no compilation unit
      val annotation = new ProblemAnnotation(e, null) // no compilation unit
      annotationModel.addAnnotation(annotation, position(e))
      previousAnnotations ::= annotation
    }
  }

  /** Ask the underlying unit to reload on each document change event.
   * 
   *  This is certainly wasteful, but otherwise the AST trees are not up to date
   *  in the interval between the last keystroke and reconciliation (which has a delay of
   *  500ms usually). The user can be quick and ask for completions in this interval, and get
   *  wrong results.
   */
  private object reloader extends IDocumentListener {
    def documentChanged(event: DocumentEvent) {
      scriptUnit.askReload()
    }

    def documentAboutToBeChanged(event: DocumentEvent) {}

  }
}


