package de.his.cs.sys.extensions.wizards.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import de.his.cs.sys.extensions.wizards.utils.templates.TemplateManager;

/**
 * Simple Resource access support
 *
 * @author keunecke, brummermann
 */
public class ResourceSupport {
	
	private final Map<String, String> extensionAntPropertiesReplacements = new HashMap<String, String>();
	
	private final IProject project;

	/**
	 * ResourceSupport
	 *
	 * @param project eclipse project
	 */
	public ResourceSupport(IProject project) {
		this.project = project;
		extensionAntPropertiesReplacements.put("[name]", project.getName());
		extensionAntPropertiesReplacements.put("[version]", "0.0.1");
	}


	/**
	 * creates the spring bean configuration and other files for the extension
	 *
	 * @throws CoreException if the file creation fails
	 * @throws UnsupportedEncodingException
	 */
	public void createFiles() throws CoreException, UnsupportedEncodingException {
		InputStream is = ResourceSupport.class.getResourceAsStream("templates/src/java/extension.beans.spring.xml.template");
		writeProjectFile("/src/java/extension.beans.spring.xml", is);

		String content = new TemplateManager("extension.ant.properties", extensionAntPropertiesReplacements).getContent();
		writeProjectFile("/extension.ant.properties", new ByteArrayInputStream(content.getBytes()));
		
		String buildXml = new TemplateManager("build.xml").getContent();
		writeProjectFile("/build.xml", new ByteArrayInputStream(buildXml.getBytes()));
		
		is = new ByteArrayInputStream(("/bin" + System.getProperty("line.separator") + "/build").getBytes("UTF-8"));
		writeProjectFile("/.gitignore", is);
	}

	/**
	 * writes a file
	 *
	 * @param filename filename relative to project root
	 * @param is input stream with data to write
	 * @throws CoreException in case of an exception
	 */
	private void writeProjectFile(String filename, InputStream is) throws CoreException {
		IFile file = project.getFile(filename);
		try {
			file.create(is, true, null);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}