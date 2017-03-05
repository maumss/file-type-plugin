package br.com.yahoo.mau_mss.filetypeplugin;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Title: FileTypeTest
 * Description:
 * Date: Jun 25, 2015, 8:29:18 PM
 *
 * @author Mauricio Soares da Silva (Mau)
 */
public class FileTypeTest {

  public FileTypeTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getStatusLineElement method, of class FileType.
   *
   * @see http://site.icu-project.org/
   */
  @Test
  public void testShowEncode() {
    System.out.println("testShowEncode");
    Properties properties = System.getProperties();
    String barra = properties.getProperty("file.separator");
    // deve retornar algo como "D:\\Documentos\\Java\\FileTypePlugin\\build\\test\\unit\\work"
    String dirWorkJunit = properties.getProperty("nbjunit.workdir");
    String path = dirWorkJunit.substring(0, dirWorkJunit.indexOf("build")) + barra
            + "src" + barra + "br" + barra + "com" + barra + "yahoo" + barra + "mau_mss" + barra
            + "filetypeplugin" + barra + "FileType.java";
    String result = "";
    try {
      File file = new File(path);
      PlainDocument plainDocument = new PlainDocument();
      try {
        String content = new Scanner(file).useDelimiter("\\Z").next();
        plainDocument.insertString(0, content, null);
      } catch (BadLocationException ex) {
        Exceptions.printStackTrace(ex);
      }
      FileType instance = new FileType();
      result = instance.showEncode(plainDocument);
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
    String expResult = "UTF-8"; // não deveria ser "UTF-8"?
    System.out.println("Encode do arquivo 'FileType.java': " + result);
    assertEquals(expResult, result);
  }

  private void checkTodosSystemProperties() {
    Properties properties = System.getProperties();
    Set<Object> sysPropertiesKeys = properties.keySet();
    sysPropertiesKeys.stream().forEach((key) -> {
      System.out.println(key + " =" + properties.getProperty((String) key));
    });
  }

}
