package net.sf.ecl1.classpath;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
/**
 * Classpath Container for working with HISinOne-Extensions
 * 
 * @author markus
 */
class ExtensionClassPathContainer implements
		IClasspathContainer {
	
	public static final String NET_SF_ECL1_ECL1_CONTAINER_ID = "net.sf.ecl1.ECL1_CONTAINER";

	public static final int K_EXTENSION = 99;
	
	public static final String EXTENSIONS_FOLDER = "qisserver/WEB-INF/extensions/";
	
	private final IJavaProject javaProject;

	/**
	 * @param javaProject
	 */
	public ExtensionClassPathContainer(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	@Override
	public IPath getPath() {
		return new Path(NET_SF_ECL1_ECL1_CONTAINER_ID);
	}

	@Override
	public int getKind() {
		return IClasspathEntry.CPE_CONTAINER;
	}

	@Override
	public String getDescription() {
		return "ecl1 Extensions Classpath Container";
	}

	@Override
	public IClasspathEntry[] getClasspathEntries() {
		Map<String, String> extensions = new HashMap<String, String>();
		
		scanForExtensionJars(javaProject, extensions);
		scanForExtensionProjects(javaProject, extensions);
		
		ArrayList<IClasspathEntry> result = new ArrayList<>();
		//uniquely register extensions either as jar or as project
		
		for (Map.Entry<String, String> extension : extensions.entrySet()) {
			if(extension.getValue().endsWith(".jar")) {
				IPath path = javaProject.getPath().append(EXTENSIONS_FOLDER).append(extension.getValue());
				//create a lib entry
				System.out.println("Creating new container entry for: " + path.toString());
				IClasspathEntry libraryEntry = JavaCore.newLibraryEntry(path, null, null);
				result.add(libraryEntry);
			} else {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(extension.getValue());
				System.out.println("Creating new container entry for project: " + project.getName());
				JavaCore.newProjectEntry(project.getLocation());
			}
		}
		return result.toArray(new IClasspathEntry[1]);
	}
	
	/**
	 * 
	 * @param javaProject
	 * @param extensions
	 */
	private void scanForExtensionProjects(IJavaProject javaProject,
			Map<String, String> extensions) {
		//scan workspace for extension projects
		IWorkspaceRoot ws = ResourcesPlugin.getWorkspace().getRoot();
		List<IProject> projects = Arrays.asList(ws.getProjects(0));
		for (IProject project : projects) {
			if(isExtensionProject(project)) {
				extensions.put(project.getName(), project.getName());
			}
		}
	}
	
	private boolean isExtensionProject(IProject project) {
		IFile file = project.getFile("extension.ant.properties");
		return file.exists();
	}

	/**
	 * Scan the java project for jar files in the extensions folder
	 * 
	 * @param javaProject
	 * @param extensions
	 */
	private void scanForExtensionJars(IJavaProject javaProject,
			Map<String, String> extensions) {
		//scan workspace for extension jars
		IFolder extensionsFolder = javaProject.getProject().getFolder(EXTENSIONS_FOLDER);
		if(extensionsFolder.exists()) {
			//if there is an extensions folder, scan it
			IPath rawLocation = extensionsFolder.getRawLocation();
			List<File> extensionJars = Arrays.asList(rawLocation.toFile().listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name != null && name.endsWith("jar");
				}
			}));
			for (File extensionJar : extensionJars) {
				extensions.put(extensionJar.getName().replace(".jar", ""), extensionJar.getName());
			}
		}
	}
}