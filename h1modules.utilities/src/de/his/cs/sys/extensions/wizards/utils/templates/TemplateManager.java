/*
 * Copyright (c) 2012 HIS eG All Rights Reserved.
 *
 * $Id$
 *
 * $Log$
 *
 * Created on 21.06.2012 by keunecke
 */
package de.his.cs.sys.extensions.wizards.utils.templates;

import static net.sf.ecl1.utilities.preferences.ExtensionToolsPreferenceConstants.TEMPLATE_ROOT_URL;

import h1modules.utilities.utils.Activator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Maps;

/**
 * Replaces variables in templates given by name.
 *
 * Templates will be retrieved from http://ecl1.sourceforge.net/templates
 *
 * @author keunecke
 * @version $Revision$
 */
public class TemplateManager {

    private static final Map<String, String> nameReplacements = Maps.newHashMap();
    static {
        nameReplacements.put("settings", ".settings");
        nameReplacements.put(".template", "");
        nameReplacements.put("gitignore", ".gitignore");
    }

    private final Map<String, String> variables;

    private String templatePath;

    /**
     * Create a new template manager with a template and variables to replace in the template
     *
     * @param templatePath
     * @param variables
     */
    public TemplateManager(String templatePath, Map<String, String> variables) {
        this.templatePath = templatePath;
        this.variables = variables;
    }

    /**
     * Create a new template manager without variables to replace
     *
     * @param template the template path
     */
    public TemplateManager(String template) {
        this(template, new HashMap<String, String>());
    }

    /**
     * Do line-wise variable replacement on template
     *
     * @return result string with replaced variables
     */
    public String getContent() {
        StringBuilder result = new StringBuilder();
        String templateRootUrl = Activator.getPreferences().getString(TEMPLATE_ROOT_URL);
        String fullTemplateUrlString = templateRootUrl + "/" + templatePath;
        
        try (InputStream templateStream = DownloadHelper.getInputStreamFromUrlFollowingRedirects(fullTemplateUrlString);){
        	
            System.out.println("Loading template from: " + fullTemplateUrlString);
            BufferedReader br = new BufferedReader(new InputStreamReader(templateStream));
            String line = "";
            while((line = br.readLine() )!= null) {
                String temp = line;
                for (Entry<String, String> variableAssignment : this.variables.entrySet()) {
                    temp = temp.replace(variableAssignment.getKey(), variableAssignment.getValue());
                }
                result.append(temp + System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro fetching Template: " + e.getMessage());
            System.out.println("TemplatePath: " + this.templatePath);
            System.out.println("Variables: " + this.variables);
            System.err.println("Erro fetching Template: " + e.getMessage());
            System.err.println("TemplatePath: " + this.templatePath);
            System.err.println("Variables: " + this.variables);
        }
        return result.toString().trim();
    }

    /**
     * Writes out the content of the given template with replaced variables to the given project in the same path as the template path.
     * The suffix ".template" is removed on file creation.
     *
     * @param project
     */
    public void writeContent(IProject project) {
        IFile file = project.getFile(doFolderAndFileRenaming(this.templatePath));
        InputStream is = new ByteArrayInputStream(getContent().getBytes());
        try {
        	IContainer parent = file.getParent();
        	if(parent instanceof IFolder) {
        		prepareFolder((IFolder) parent);
        	}
            file.create(is, true, null);
        } catch (CoreException e) {
            System.out.println("Error creating file from template '" + this.templatePath + "': " + e.getMessage());
            System.err.println("Error creating file from template '" + this.templatePath + "': " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e1) {
                System.out.println("Error creating file from template '" + this.templatePath + "': " + e1.getMessage());
                System.err.println("Error creating file from template '" + this.templatePath + "': " + e1.getMessage());
                e1.printStackTrace();
            }
        }
    }

    private String doFolderAndFileRenaming(String path) {
        String replacedFileName = path;
        for (Map.Entry<String, String> replacement : nameReplacements.entrySet()) {
            replacedFileName = replacedFileName.replace(replacement.getKey(), replacement.getValue());
        }
        return replacedFileName;
    }
    
    private void prepareFolder(IFolder folder) throws CoreException {
      IContainer parent = folder.getParent();
      if (parent instanceof IFolder){
        prepareFolder((IFolder) parent);
      }
      if (!folder.exists()){
        folder.create(true, true, null);
      }
    }

}
