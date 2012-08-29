package net.sf.ecl1.extensionpoint.collector.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import net.sf.ecl1.extensionpoint.collector.model.ExtensionPointInformation;

import org.eclipse.jdt.core.IType;
/**
 * Manager for collected extension points
 * 
 * @author keunecke
 */
public final class ExtensionPointManager {
	
	/** Map containing source extension name as key and the defined extension point */
    private static final Map<String, Map<String, Collection<ExtensionPointInformation>>> extensions = new HashMap<String, Map<String, Collection<ExtensionPointInformation>>>();
	
    private static final Collection<ExtensionPointManagerChangeListener> listeners = new LinkedList<ExtensionPointManagerChangeListener>();

	/**
	 * Add a discovered extension
	 * 
	 * @param extension
	 * @param epi
	 */
    public static final void addExtensions(String extension, IType type, Collection<ExtensionPointInformation> epis) {
        Map<String, Collection<ExtensionPointInformation>> pointsInProject = extensions.get(extension);
        if (pointsInProject == null) {
            pointsInProject = new HashMap<String, Collection<ExtensionPointInformation>>();
            extensions.put(extension, pointsInProject);
        }
        Collection<ExtensionPointInformation> collection = pointsInProject.get(type.getFullyQualifiedName());
        if (collection == null) {
            collection = new HashSet<ExtensionPointInformation>();
            pointsInProject.put(extension, collection);
        }
        collection.addAll(epis);
        updateListeners();
	}

    /**
     * Remove extension points from an extension
     * 
     * @param extension
     * @param epis
     */
    public static final void removeExtensions(String extension, IType type, Collection<ExtensionPointInformation> epis) {
        Map<String, Collection<ExtensionPointInformation>> pointsInProject = extensions.get(extension);
        Collection<ExtensionPointInformation> collection = pointsInProject.get(type.getFullyQualifiedName());
        if (collection != null) {
            collection.removeAll(epis);
        }
        updateListeners();
    }

    /**
     * Get all extension points
     * 
     * @return map with key extension and value collection of contained extension points
     */
    public static final Map<String, Map<String, Collection<ExtensionPointInformation>>> getExtensions() {
        return new HashMap<String, Map<String, Collection<ExtensionPointInformation>>>(extensions);
	}

    public static final void register(ExtensionPointManagerChangeListener l) {
        listeners.add(l);
    }

    private static final void updateListeners() {
        for (ExtensionPointManagerChangeListener listener : listeners) {
            listener.update();
        }
    }

}
