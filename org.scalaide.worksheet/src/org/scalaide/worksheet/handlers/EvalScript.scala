package org.scalaide.worksheet.handlers

import scala.tools.eclipse.logging.HasLogger
import scala.tools.eclipse.refactoring.EditorHelpers
import scala.tools.eclipse.util.EditorUtils

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.ui.handlers.HandlerUtil
import org.eclipse.ui.texteditor.ITextEditor

import org.scalaide.worksheet.editor.ScriptEditor

import org.eclipse.ui.IPropertyListener

class EvalScript extends AbstractHandler with HasLogger {

  override def execute(event: ExecutionEvent): AnyRef = {
    HandlerUtil.getActiveEditor(event) match {
      case editor: ScriptEditor => editor.runEvaluation()
      case editor => logger.debug("Expected ScriptEditor, found "+editor)
    }
    null
  }

  override def isEnabled: Boolean =
    EditorHelpers.withCurrentEditor { editor =>
      EditorUtils.getEditorScalaInput(editor) map { scu => scu.currentProblems.isEmpty }
    } getOrElse false
}