package br.com.yahoo.mau_mss.filetypeplugin;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusLineElementProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * Title: FileType
 * Description: Cria um item no Status Line para mostrar o tipo de arquivo
 * Date: Jun 25, 2015, 8:23:05 AM
 *
 * @author Mauricio Soares da Silva (mauricio.soares)
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 0)
public class FileType implements StatusLineElementProvider {
  private static final JLabel label = new JLabel("   ");
  private final JPanel panel = new JPanel(new FlowLayout());
  private final Map<String, String> lineEndings = new HashMap<>();
  private String lastDocPath = "";
  private static final String DEFAULT_ENCODING = "UTF-8";
  private static final Logger logger = Logger.getLogger(FileType.class.getName());

  /**
   * Create a new instance of <code>FileType</code>.
   */
  public FileType() {
    includeSeparator();
    loadLineEndings();
    createListener();
  }

  private void includeSeparator() {
    JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
    separator.setPreferredSize(new Dimension(2, 14));
    this.panel.add(separator);
    this.panel.add(FileType.label);
  }

  private void loadLineEndings() {
    this.lineEndings.put("\r", "Mac OS /"); // final com CR
    this.lineEndings.put("\r\n", "Windows /"); // final com CRLF
    this.lineEndings.put("\n", "Unix /"); // final com LF
  }

  private void createListener() {
    EditorRegistry.addPropertyChangeListener((PropertyChangeEvent evt) -> {
      updateComponent();
    });
  }

  private void updateComponent() {
    JTextComponent comp = EditorRegistry.focusedComponent();
    FileType.logger.log(Level.FINE, "========================= updateComponente =====================================");
    if (comp != null) {
      analyseSource(comp);
    } else {
      FileType.logger.log(Level.FINE, "Limpou label");
      FileType.label.setText("    ");
      this.lastDocPath = "";
    }
    FileType.logger.log(Level.FINE, "========================= updateComponente =====================================");
  }

  private void analyseSource(JTextComponent comp) {
    DataObject dataObject = NbEditorUtilities.getDataObject(comp.getDocument());
    if (dataObject == null) {
      return;
    }
    FileObject fileObject = dataObject.getPrimaryFile();
    if (fileObject == null) {
      return;
    }
    String currentDocPath = defaultString(fileObject.getPath());
    FileType.logger.log(Level.INFO, "Path corrente: {0}\nPath anterior: {1}", new Object[]{currentDocPath, this.lastDocPath});
    if (!this.lastDocPath.equals(currentDocPath)) {
      showLineEnd(comp.getDocument());
    }
    this.lastDocPath = currentDocPath;
  }

  private String defaultString(String str) {
    if (str == null) {
      return "";
    }
    return str;
  }

  private void showLineEnd(Document doc) {
    String dn = this.lineEndings.get(doc.getProperty(DefaultEditorKit.EndOfLineStringProperty)).
            trim() + " " + showEncode(doc);
    FileType.label.setText(dn);
  }

  protected String showEncode(Document doc) {
    String charsetName = "";
    try {
      String convertedPlainText = doc.getText(0, doc.getLength());
      try (InputStream is = convertStringToStream(convertedPlainText)) {
        CharsetMatch charsetMatch = new CharsetDetector().setText(is).detect();
        charsetName = charsetMatch.getName();
        charsetName = charsetName != null ? charsetName : "NULL";
        if (isPoorMatch(charsetMatch.getConfidence())) {
          charsetName = verifyPossibleUtf8(charsetName, is);
        }
        charsetName += showByteOfMark(is);
      }
    } catch (BadLocationException | IOException ex) {
      Exceptions.printStackTrace(ex);
    }
    return charsetName;
  }

  private InputStream convertStringToStream(String text) {
    return new ByteArrayInputStream(text.getBytes());
  }

  private boolean isPoorMatch(int confidence) {
    FileType.logger.log(Level.INFO, "N\u00edvel de cofian\u00e7a: {0}", confidence);
    return confidence < 50;
  }

  private String verifyPossibleUtf8(String charsetName, InputStream inputStream) throws IOException {
    if (!FileType.DEFAULT_ENCODING.equals(charsetName)
            && isValidUtf8(IOUtils.toByteArray(inputStream))) {
      FileType.logger.log(Level.INFO, "Vai mudar o encoding de {0} para o default", charsetName);
      return FileType.DEFAULT_ENCODING;
    }
    return charsetName;
  }

  private boolean isValidUtf8(byte[] bytes) {
    try {
      Charset.availableCharsets().get(FileType.DEFAULT_ENCODING).newDecoder().decode(ByteBuffer.wrap(bytes));
    } catch (CharacterCodingException e) {
      return false;
    }
    return true;
  }

  private String showByteOfMark(InputStream source) throws IOException {
    ByteOrderMark detectedBOM = new BOMInputStream(source).getBOM();
    if (detectedBOM == null) {
      return "";
    }
    String bom = detectedBOM.toString();
    FileType.logger.log(Level.INFO, "BOM: {0}", bom);
    return " w/ " + bom;
  }

  @Override
  public Component getStatusLineElement() {
    return this.panel;
  }
}
